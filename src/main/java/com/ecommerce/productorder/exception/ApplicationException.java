package com.ecommerce.productorder.exception;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

    public ApplicationException(String message) {
        super(message);
    }
}
