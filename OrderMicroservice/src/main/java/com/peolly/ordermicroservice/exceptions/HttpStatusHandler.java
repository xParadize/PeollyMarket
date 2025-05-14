package com.peolly.ordermicroservice.exceptions;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class HttpStatusHandler {

    public static Mono<? extends Throwable> handle(HttpRequest httpRequest, ClientHttpResponse clientHttpResponse) throws IOException {
        HttpStatus status = (HttpStatus) clientHttpResponse.getStatusCode();

        return switch (status) {
            case NOT_FOUND -> Mono.error(new InsufficientStockException("Not found. Please refresh the page."));
            case BAD_REQUEST -> Mono.error(new IllegalArgumentException("Invalid data in cart."));
            case CONFLICT -> Mono.error(new PriceMismatchException("Price mismatch. Refresh the page."));
            case INTERNAL_SERVER_ERROR -> Mono.error(new InternalServerErrorException("Server error."));
            case SERVICE_UNAVAILABLE -> Mono.error(new ServiceUnavailableException("Service is down. Try later."));
            case GATEWAY_TIMEOUT -> Mono.error(new GatewayTimeoutException("Request timed out."));
            default -> Mono.error(new UnexpectedHttpException("Unexpected error with status: " + status));
        };
    }
}

