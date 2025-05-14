package com.peolly.ordermicroservice.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itextpdf.text.DocumentException;
import com.peolly.ordermicroservice.dto.AddToCartDto;
import com.peolly.ordermicroservice.dto.CartDto;
import com.peolly.ordermicroservice.dto.PerformPaymentDto;
import com.peolly.ordermicroservice.exceptions.*;
import com.peolly.ordermicroservice.external.*;
import com.peolly.ordermicroservice.models.Cart;
import com.peolly.ordermicroservice.models.OrderElement;
import com.peolly.ordermicroservice.util.CartRestService;
import com.peolly.ordermicroservice.util.LocalDateTimeDeserializer;
import com.peolly.ordermicroservice.util.LocalDateTimeSerializer;
import com.peolly.ordermicroservice.util.OrderIdGenerator;
import com.peolly.schemaregistry.FileCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final OrderService orderService;
    private final ECheckGenerator eCheckGenerator;

    private final String CART_CACHE_KEY = "Cart:";
    private final RestClient restClient = RestClient.create();
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
    public CartDto listCartItems(UUID customerId) {
        String key = CART_CACHE_KEY + customerId;

        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            List<String> stringOrders = jedis.lrange(key, 0, -1);
            if (stringOrders.isEmpty()) {
                throw new EmptyCartException("Cart is empty.");
            }

            List<OrderElement> allUserOrderElements = stringOrders.stream()
                    .map(orderJson -> GSON.fromJson(orderJson, OrderElement.class))
                    .collect(Collectors.toList());

            List<ItemDto> updatedProducts = allUserOrderElements.stream()
                    .map(orderElement -> {
                        try {
                            ItemDto updatedProduct = cartRestService.getItemInfo(orderElement.getItemDto().id());
                            orderElement.setItemDto(updatedProduct);
                            return updatedProduct;
                        } catch (ItemNotFoundException e) {
                            throw new RuntimeException("Error while getting item info: " + e.getMessage());
                        }
                    })
                    .toList();
            updateOrderInCache(customerId, allUserOrderElements);

            double totalCost = allUserOrderElements.stream()
                    .mapToDouble(el -> el.getItemDto().price() * el.getQuantity())
                    .sum();

            return returnCartList(totalCost, allUserOrderElements, customerId);
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
    private CartDto returnCartList(double totalCost, List<OrderElement> cartItems, UUID customerId) {
        Cart userCart = Cart.builder()
                .userId(customerId)
                .orderElements(cartItems)
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
     * @param updatedOrderElements the list of updated orders to be stored in the cache.
     */
    private void updateOrderInCache(UUID customerId, List<OrderElement> updatedOrderElements) {
        String key = CART_CACHE_KEY + customerId;

        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            jedis.del(key);
            for (OrderElement orderElement : updatedOrderElements) {
                jedis.rpush(key, GSON.toJson(orderElement));
            }
        }
    }

    /**
     * Adds a product to the user's cart and updates the Redis cache.
     *
     * @param addToCartDto  the unique identifier of the product to be added.
     * @param customerId the unique identifier of the customer.
     */
    @Transactional
    public void addToCart(AddToCartDto addToCartDto, UUID customerId) {
        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            ItemDto itemDto = cartRestService.getItemInfo(addToCartDto.itemId());

            OrderElement newOrderElement = OrderElement.builder()
                    .orderId(OrderIdGenerator.generate())
                    .itemDto(itemDto)
                    .userId(customerId)
                    .createdAt(LocalDateTime.now())
                    .quantity(addToCartDto.quantity())
                    .build();

            String orderJson = GSON.toJson(newOrderElement);
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
                    .map(orderJson -> GSON.fromJson(orderJson, OrderElement.class))
                    .filter(orderElement -> !Objects.equals(orderElement.getItemDto().id(), productId))
                    .map(GSON::toJson)
                    .toList();

            jedis.del(key);

            if (!updatedCartItems.isEmpty()) {
                jedis.rpush(key, updatedCartItems.toArray(new String[0]));
            }
        }
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> getCartItemQuantities(UUID userId) {
        CartDto cartDto = listCartItems(userId);
        return calculateItemQuantities(cartDto);
    }

    @Transactional(readOnly = true)
    public List<PricesRefreshRequest> getCartItemsForPriceCheck(UUID userId) {
        CartDto cartDto = listCartItems(userId);
        Map<Long, Integer> itemQuantities = calculateItemQuantities(cartDto);

        return cartDto.getCart().getOrderElements().stream()
                .map(orderElement -> new PricesRefreshRequest(
                        orderElement.getItemDto().id(),
                        itemQuantities.get(orderElement.getItemDto().id()),
                        orderElement.getItemDto().price()
                ))
                .toList();
    }

    /**
     * Подсчитывает количество каждого товара в корзине
     */
    private Map<Long, Integer> calculateItemQuantities(CartDto cartDto) {
        return cartDto.getCart().getOrderElements().stream()
                .collect(Collectors.toMap(
                        orderElement -> orderElement.getItemDto().id(),
                        OrderElement::getQuantity,
                        Integer::sum
                ));
    }

    @Transactional
    public void processOrder(PerformPaymentDto performPaymentDto, UUID userId, String email) throws DocumentException {

        // TODO: тут нужно проверить случай, когда во время заказа товар заканчивается => код должен выдать ошибку
        //  404. Клиент должен обновить страницу и подгрузить товары => получить новый список
        // Проверяем наличие товаров на складе
        Map<Long, Integer> cartItemsToCheckAvailability = getCartItemQuantities(userId);


        Boolean itemsAvailabilityCheckResult = restClient.post()
                .uri("http://localhost:8031/api/v1/storage/check-availability")
                .body(cartItemsToCheckAvailability)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, ((request, response) -> {
                    throw new InsufficientStockException("Not enough stock for item. Please, refresh the page.");
                }))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, ((request, response) -> {
                    throw new InternalServerErrorException("Internal Server Error.");
                }))
                .onStatus(HttpStatus.SERVICE_UNAVAILABLE::equals, ((request, response) -> {
                    throw new ServiceUnavailableException("Service temporarily unavailable.");
                }))
                .onStatus(HttpStatus.GATEWAY_TIMEOUT::equals, ((request, response) -> {
                    throw new GatewayTimeoutException("Gateway Time-out.");
                }))
                .body(Boolean.class);

        // Получаем обновленные цены
        List<PricesRefreshRequest> priceRequests = getCartItemsForPriceCheck(userId);
        List<PricesRefreshResponse> updatedPrices = restClient.post()
                .uri("http://localhost:8032/api/v1/pricing/get-price")
                .body(priceRequests)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, (request, response) -> {
                    throw new InsufficientStockException("Price not found.");
                })
                .onStatus(HttpStatus.BAD_REQUEST::equals, (request, response) -> {
                    throw new IllegalArgumentException("Invalid product data in cart. Please, refresh the page.");
                })
                .onStatus(HttpStatus.CONFLICT::equals, (request, response) -> {
                    throw new PriceMismatchException("Prices have changed. Please, refresh the page.");
                })
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, ((request, response) -> {
                    throw new InternalServerErrorException("Internal Server Error.");
                }))
                .onStatus(HttpStatus.SERVICE_UNAVAILABLE::equals, ((request, response) -> {
                    throw new ServiceUnavailableException("Service temporarily unavailable.");
                }))
                .onStatus(HttpStatus.GATEWAY_TIMEOUT::equals, ((request, response) -> {
                    throw new GatewayTimeoutException("Gateway Time-out.");
                }))
                .body(new ParameterizedTypeReference<>() {});

        // проходимся циклом по корзине и меняем цены на новые (если есть) => получаем список с товарами которые точно есть и свежими ценами
        CartDto finalCartList = listCartItems(userId);

        for (OrderElement element : finalCartList.getCart().getOrderElements()) {
            Long productId = element.getItemDto().id();

            PricesRefreshResponse matchedPrice = updatedPrices.stream()
                    .filter(resp -> Objects.equals(resp.itemId(), productId))
                    .findFirst()
                    .orElse(null);

            if (matchedPrice != null) {
                ItemDto oldProduct = element.getItemDto();
                ItemDto updatedProduct = new ItemDto(
                        oldProduct.id(),
                        oldProduct.name(),
                        oldProduct.description(),
                        oldProduct.image(),
                        matchedPrice.updatedPrice(),
                        oldProduct.inStockQuantity()
                );
                element.setItemDto(updatedProduct);
            }
        }

        // Создаем запись о заказе в таблице заказов (OrderMS)
        Long futureOrderId = orderService.createOrder(
                userId,
                finalCartList.getItemsCount(),
                BigDecimal.valueOf(finalCartList.getTotalCost()),
                performPaymentDto.cardNumber()
        );

        // Резервируем товары на складе (OrderMS -> StorageMS)
        List<Long> itemsIdsToReserve = finalCartList.getCart().getOrderElements().stream()
                .map(orderElement -> orderElement.getItemDto().id())
                .toList();
        restClient.post()
                .uri("http://localhost:8031/api/v1/storage/reserve-item")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReserveItemsRequest(itemsIdsToReserve, userId))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, (request, response) -> {
                    throw new ItemReservationException("Error while reserving an item.");
                })
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, ((request, response) -> {
                    throw new InternalServerErrorException("Internal Server Error.");
                }))
                .onStatus(HttpStatus.SERVICE_UNAVAILABLE::equals, ((request, response) -> {
                    throw new ServiceUnavailableException("Service temporarily unavailable.");
                }))
                .onStatus(HttpStatus.GATEWAY_TIMEOUT::equals, ((request, response) -> {
                    throw new GatewayTimeoutException("Gateway Time-out.");
                }))
                .toBodilessEntity();

        // Проводим валидацию карты + снимаем деньги, если нет ошибки (OrderMS -> PaymentMS)
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(
                performPaymentDto.cardNumber(),
                userId,
                finalCartList.getTotalCost(),
                futureOrderId
        );
        restClient.post()
                .uri("http://localhost:8005/payment/api/v1/process-payment")
                .contentType(MediaType.APPLICATION_JSON)
                .body(paymentRequestDto)
                .retrieve()
                .onStatus(HttpStatus.BAD_REQUEST::equals, (request, response) -> {
                    throw new PaymentException("Invalid card data or insufficient funds.");
                })
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, ((request, response) -> {
                    throw new InternalServerErrorException("Internal Server Error.");
                }))
                .onStatus(HttpStatus.SERVICE_UNAVAILABLE::equals, ((request, response) -> {
                    throw new ServiceUnavailableException("Service temporarily unavailable.");
                }))
                .onStatus(HttpStatus.GATEWAY_TIMEOUT::equals, ((request, response) -> {
                    throw new GatewayTimeoutException("Gateway Time-out.");
                }))
                .toBodilessEntity();

        // чек
        byte[] pdfData = eCheckGenerator.generateECheck(userId, finalCartList);
        String filename = eCheckGenerator.getECheckName();

        ByteArrayResource pdfResource = new ByteArrayResource(pdfData) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", pdfResource);
        parts.add("email", email);
        parts.add("fileCategory", FileCategory.ECHECK.name());

        restClient.post()
                .uri("http://localhost:8010/s3/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve()
                .onStatus(HttpStatus.BAD_GATEWAY::equals, (request, response) -> {
                    throw new ECheckSavingException("Error while saving ECheck.");
                })
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, ((request, response) -> {
                    throw new InternalServerErrorException("Internal Server Error.");
                }))
                .onStatus(HttpStatus.SERVICE_UNAVAILABLE::equals, ((request, response) -> {
                    throw new ServiceUnavailableException("Service temporarily unavailable.");
                }))
                .onStatus(HttpStatus.GATEWAY_TIMEOUT::equals, ((request, response) -> {
                    throw new GatewayTimeoutException("Gateway Time-out.");
                }))
                .toBodilessEntity();

        // Удаляем заказ (OrderMS -> Redis)
        String key = CART_CACHE_KEY + userId;
        Jedis jedis = new Jedis(redisHost, redisPort);
        jedis.del(key);
        jedis.close();

        // Удаляем резервы товаров
        restClient.method(HttpMethod.DELETE)
                .uri("http://localhost:8031/api/v1/storage/reserve-item")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReserveItemsRequest(itemsIdsToReserve, userId))
                .retrieve()
                .onStatus(HttpStatus.BAD_GATEWAY::equals, (request, response) -> {
                    throw new ItemReservationException("Error while deleting reserved items.");
                })
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, ((request, response) -> {
                    throw new InternalServerErrorException("Internal Server Error.");
                }))
                .onStatus(HttpStatus.SERVICE_UNAVAILABLE::equals, ((request, response) -> {
                    throw new ServiceUnavailableException("Service temporarily unavailable.");
                }))
                .onStatus(HttpStatus.GATEWAY_TIMEOUT::equals, ((request, response) -> {
                    throw new GatewayTimeoutException("Gateway Time-out.");
                }))
                .toBodilessEntity();

        // после оплаты должен статус стать "PAID" и дата оплаты актуальная
        orderService.finishOrder(futureOrderId, userId);
    }
}
