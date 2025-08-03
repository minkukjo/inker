package com.example.inker.stock.exception;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException() {
        super("재고를 찾을 수 없습니다");
    }
    
    public StockNotFoundException(String message) {
        super(message);
    }
} 