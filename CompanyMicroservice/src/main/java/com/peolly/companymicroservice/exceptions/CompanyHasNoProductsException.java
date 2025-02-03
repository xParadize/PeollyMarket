package com.peolly.companymicroservice.exceptions;

import org.apache.kafka.common.errors.ResourceNotFoundException;

public class CompanyHasNoProductsException extends ResourceNotFoundException {
    public CompanyHasNoProductsException(String message) {
        super(message);
    }
}
