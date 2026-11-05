package com.cinema.modules.operation.service;

public class CheckoutConfirmationRequiredException extends IllegalStateException {
    public CheckoutConfirmationRequiredException(String message) {
        super(message);
    }
}
