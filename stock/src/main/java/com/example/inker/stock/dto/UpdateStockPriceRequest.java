package com.example.inker.stock.dto;

/**
 * 주식 가격 업데이트 요청 DTO
 */
public class UpdateStockPriceRequest {
    private Double currentPrice;
    private Double previousPrice;
    private Long volume;
    
    // 기본 생성자
    public UpdateStockPriceRequest() {}
    
    // 생성자
    public UpdateStockPriceRequest(Double currentPrice, Double previousPrice, Long volume) {
        this.currentPrice = currentPrice;
        this.previousPrice = previousPrice;
        this.volume = volume;
    }
    
    // Getter와 Setter
    public Double getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public Double getPreviousPrice() {
        return previousPrice;
    }
    
    public void setPreviousPrice(Double previousPrice) {
        this.previousPrice = previousPrice;
    }
    
    public Long getVolume() {
        return volume;
    }
    
    public void setVolume(Long volume) {
        this.volume = volume;
    }
}
