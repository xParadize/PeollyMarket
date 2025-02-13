package com.peolly.ordermicroservice.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.peolly.ordermicroservice.external.ProductDto;
import com.peolly.ordermicroservice.models.Order;
import com.peolly.ordermicroservice.util.CartRestService;
import com.peolly.ordermicroservice.util.LocalDateTimeDeserializer;
import com.peolly.ordermicroservice.util.LocalDateTimeSerializer;
import com.peolly.ordermicroservice.util.OrderIdGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

//    @Transactional(readOnly = true)
//    public CartDto listCartProducts(UUID userId) {
//
//        Jedis jedis = new Jedis("localhost", 6379);
//
//        List<String> stringOrders = jedis.lrange("Cart:" + userId, 0, -1);
//        if (stringOrders.isEmpty()) throw new EmptyCartException();
//        List<Order> allUserOrders = new ArrayList<>();
//
//        double totalcost = 0.0;
//
//        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
//
//        for (String strOrder : stringOrders) {
//
//            Order order = gsonBuilder.create().fromJson(strOrder, Order.class);
//            allUserOrders.add(order);
//
//            Product orderProduct = productsRepository.findById(order.getCartProductDto().getItemNumber()).orElse(null);
//            totalcost += orderProduct.getPrice();
//        }
//
//        jedis.close();
//
//        return returnCartList(totalcost, allUserOrders, userId);
//    }

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

//    private CartDto returnCartList(double totalCost, List<Order> cartItems, UUID userId) {
//        Cart userCart = Cart.builder()
//                .userId(userId)
//                .orders(cartItems)
//                // TODO: sales and discounts
//                .build();
//
//        CartDto cartToReturn = CartDto.builder()
//                .cart(userCart)
//                .itemsCount(cartItems.size())
//                .totalCost(totalCost)
//                // TODO: sales and discounts
//                .priceWithDiscount(totalCost)
//                .build();
//
//        return cartToReturn;
//    }
//
//    public CartProductDto convertProductToCartProductDto(Product product) {
//        CartProductDto dtoToSave = CartProductDto.builder()
//                .itemNumber(product.getId())
//                .itemName(product.getName())
//                .itemDescription(product.getDescription())
//                .image(product.getImage())
//                .organizationName(product.getOrganization().getName())
//                .itemPrice(product.getPrice())
//                .build();
//        return dtoToSave;
//    }
}
