package com.peolly.ordermicroservice.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.peolly.ordermicroservice.dto.CartDto;
import com.peolly.ordermicroservice.dto.PerformPaymentDto;
import com.peolly.ordermicroservice.exceptions.EmptyCartException;
import com.peolly.ordermicroservice.exceptions.ProductNotFoundException;
import com.peolly.ordermicroservice.external.ProductDto;
import com.peolly.ordermicroservice.models.Cart;
import com.peolly.ordermicroservice.models.Order;
import com.peolly.ordermicroservice.util.CartRestService;
import com.peolly.ordermicroservice.util.LocalDateTimeDeserializer;
import com.peolly.ordermicroservice.util.LocalDateTimeSerializer;
import com.peolly.ordermicroservice.util.OrderIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    @Value("${REDIS_HOST}")
    private String redisHost;

    @Value("${REDIS_PORT}")
    private int redisPort;

    private final CartRestService cartRestService;
    private final String CART_CACHE_KEY = "Cart:";

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .create();

    /**
     * Retrieves the products in the user's cart from Redis cache.
     *
     * @param customerId the unique identifier of the customer.
     * @return CartDto object containing the user's cart details including items and total cost.
     * @throws EmptyCartException if the cart is empty.
     */
    @Transactional(readOnly = true)
    public CartDto listCartProducts(UUID customerId) {
        String key = CART_CACHE_KEY + customerId;

        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            List<String> stringOrders = jedis.lrange(key, 0, -1);
            if (stringOrders.isEmpty()) {
                throw new EmptyCartException("Cart is Empty :(");
            }

            List<Order> allUserOrders = stringOrders.stream()
                    .map(orderJson -> GSON.fromJson(orderJson, Order.class))
                    .collect(Collectors.toList());

            List<ProductDto> updatedProducts = allUserOrders.stream()
                    .map(order -> {
                        try {
                            ProductDto updatedProduct = cartRestService.getProductInfo(order.getProductDto().id());
                            order.setProductDto(updatedProduct);
                            return updatedProduct;
                        } catch (ProductNotFoundException e) {
                            throw new RuntimeException("Error while getting product info: " + e.getMessage());
                        }
                    })
                    .toList();
            updateOrderInCache(customerId, allUserOrders);

            double totalCost = updatedProducts.stream()
                    .mapToDouble(ProductDto::price)
                    .sum();

            return returnCartList(totalCost, allUserOrders, customerId);
        }
    }

    /**
     * Constructs and returns a CartDto object with cart details.
     *
     * @param totalCost  the total cost of the products in the cart.
     * @param cartItems  the list of orders in the cart.
     * @param customerId the unique identifier of the customer.
     * @return CartDto object containing cart details including item count and total cost.
     */
    private CartDto returnCartList(double totalCost, List<Order> cartItems, UUID customerId) {
        Cart userCart = Cart.builder()
                .userId(customerId)
                .orders(cartItems)
                .build();
        CartDto cartToReturn = CartDto.builder()
                .cart(userCart)
                .itemsCount(cartItems.size())
                .totalCost(totalCost)
                .priceWithDiscount(totalCost)
                .build();
        return cartToReturn;
    }

    /**
     * Updates the user's cart in Redis cache with the latest order information.
     *
     * @param customerId    the unique identifier of the customer.
     * @param updatedOrders the list of updated orders to be stored in the cache.
     */
    private void updateOrderInCache(UUID customerId, List<Order> updatedOrders) {
        String key = CART_CACHE_KEY + customerId;

        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            jedis.del(key);
            for (Order order : updatedOrders) {
                jedis.rpush(key, GSON.toJson(order));
            }
        }
    }

    /**
     * Adds a product to the user's cart and updates the Redis cache.
     *
     * @param productId  the unique identifier of the product to be added.
     * @param customerId the unique identifier of the customer.
     */
    @Transactional
    public void addToCart(Long productId, UUID customerId) {
        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            ProductDto productInfo = cartRestService.getProductInfo(productId);

            Order newOrder = Order.builder()
                    .orderId(OrderIdGenerator.generate())
                    .productDto(productInfo)
                    .userId(customerId)
                    .createdAt(LocalDateTime.now())
                    .build();

            String orderJson = GSON.toJson(newOrder);
            jedis.lpush(CART_CACHE_KEY + customerId, orderJson);
        }
    }

    /**
     * Removes a product from the user's cart by its product ID and updates the Redis cache.
     *
     * @param productId  the unique identifier of the product to be removed.
     * @param customerId the unique identifier of the customer.
     */
    @Transactional
    public void removeFromCart(Long productId, UUID customerId) {
        String key = CART_CACHE_KEY + customerId;

        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            List<String> cartItems = jedis.lrange(key, 0, -1);

            List<String> updatedCartItems = cartItems.stream()
                    .map(orderJson -> GSON.fromJson(orderJson, Order.class))
                    .filter(order -> !Objects.equals(order.getProductDto().id(), productId))
                    .map(GSON::toJson)
                    .toList();

            jedis.del(key);

            if (!updatedCartItems.isEmpty()) {
                jedis.rpush(key, updatedCartItems.toArray(new String[0]));
            }
        }
    }

    @Transactional
    public void performPayment(PerformPaymentDto performPaymentDto, UUID userId) {
        // Создаем запись о заказе в таблице заказов (OrderMS)
        // резерв на складе -  /store/api/v1/reserve-products (OrderMS -> ProductMS)
        // валидация карты - http://localhost:8005/payment/api/v1/process_payment (OrderMS -> PaymentMS)
        // снимаем деньги (PaymentMS)
        // Создаем запись о платеже в таблице платежей + даем сюда номер заказа (PaymentMS)

        // чек в с3 (OrderMS -> S3)
        // емеил с чеком (S3 -> NotifMS)
        // Удаляем заказ (OrderMS -> Redis)
        // Удаляем n-товаров с магазина (OrderMS -> ProductMS)

    }
}
