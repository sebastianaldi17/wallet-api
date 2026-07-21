package com.sebastianaldi17.walletapi.exceptions;

public class IdempotencyKeyReuseException extends RuntimeException {
    public IdempotencyKeyReuseException(String message) { super(message); }
}