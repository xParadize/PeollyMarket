package com.peolly.ordermicroservice.services;

import com.peolly.ordermicroservice.dto.AddToCartDto;
import com.peolly.ordermicroservice.external.ProductDto;
import com.peolly.ordermicroservice.models.Order;
import com.peolly.ordermicroservice.util.CartRestService;
import com.peolly.ordermicroservice.util.OrderIdGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    @Value("${REDIS_HOST}")
    private String redisHost;

    @Value("${REDIS_PORT}")
    private int redisPort;

    private final CartRestService cartRestService;

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
    public void addToCart(AddToCartDto addToCartDto, UUID customerId) {
        Jedis jedis = new Jedis(redisHost, redisPort);
        ProductDto productInfo = cartRestService.getProductInfo(addToCartDto.productId());
        Order newOrder = Order.builder()
                .orderId(OrderIdGenerator.generate())
                .productDto(productInfo)
                .userId(customerId)
                .createdAt(LocalDateTime.now())
                .build();
        jedis.lpush("Cart:" + customerId, String.valueOf(newOrder));
        jedis.close();
    }

//    @Transactional
//    public void deleteItem(Long itemNumber, UUID userId) {
//
//        String key = "Cart:" + userId;
//        Jedis jedis = new Jedis();
//
//        // Receive cart by key
//        List<String> result = jedis.lrange(key, 0, -1);
//
//        // Get order list in json format
//        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
//
//        // Find item with requested number and delete it
//        for (String resultElement : result) {
//
//            Order order = gsonBuilder.create().fromJson(resultElement, Order.class);
//
//            if (Objects.equals(order.getCartProductDto().getItemNumber(), itemNumber)) {
//                result.remove(resultElement);
//                break;
//            }
//        }
//
//        // Delete previous list from redis
//        jedis.del(key);
//
//        // Save new list with updated cart
//        for (String updatedElement : result) {
//            jedis.rpush(key, updatedElement);
//        }
//
//        jedis.close();
//    }

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
