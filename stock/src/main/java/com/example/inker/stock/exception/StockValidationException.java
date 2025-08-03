package com.example.inker.stock.exception;

public class StockValidationException extends RuntimeException {
    public StockValidationException() {
        super("재고 데이터 검증에 실패했습니다");
    }
    
    public StockValidationException(String message) {
        super(message);
    }
} 