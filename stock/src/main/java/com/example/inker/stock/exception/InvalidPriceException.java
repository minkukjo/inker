package com.example.inker.stock.exception;

public class InvalidPriceException extends RuntimeException {
    public InvalidPriceException() {
        super("유효하지 않은 가격입니다");
    }
    
    public InvalidPriceException(String message) {
        super(message);
    }
} 