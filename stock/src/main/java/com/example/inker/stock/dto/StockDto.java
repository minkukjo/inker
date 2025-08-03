package com.example.inker.stock.dto;

import com.example.inker.stock.entity.Stock;
import java.time.LocalDateTime;

/**
 * 주식 생성 요청 DTO
 */
public class CreateStockRequest {
    private String symbol;
    private String companyName;
    private Double currentPrice;
    private Double previousPrice;
    private Long volume;
    private Double marketCap;
    private String sector;
    
    // 기본 생성자
    public CreateStockRequest() {}
    
    // 생성자
    public CreateStockRequest(String symbol, String companyName, Double currentPrice, Double previousPrice, 
                             Long volume, Double marketCap, String sector) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.previousPrice = previousPrice;
        this.volume = volume;
        this.marketCap = marketCap;
        this.sector = sector;
    }
    
    // Getter와 Setter
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
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
    
    public Double getMarketCap() {
        return marketCap;
    }
    
    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }
    
    public String getSector() {
        return sector;
    }
    
    public void setSector(String sector) {
        this.sector = sector;
    }
}

/**
 * 주식 수정 요청 DTO
 */
class UpdateStockRequest {
    private String symbol;
    private String companyName;
    private Double currentPrice;
    private Double previousPrice;
    private Long volume;
    private Double marketCap;
    private String sector;
    
    // 기본 생성자
    public UpdateStockRequest() {}
    
    // Getter와 Setter
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
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
    
    public Double getMarketCap() {
        return marketCap;
    }
    
    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }
    
    public String getSector() {
        return sector;
    }
    
    public void setSector(String sector) {
        this.sector = sector;
    }
}

/**
 * 주식 가격 업데이트 요청 DTO
 */
class UpdateStockPriceRequest {
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

/**
 * 주식 응답 DTO
 */
class StockResponse {
    private Long id;
    private String symbol;
    private String companyName;
    private Double currentPrice;
    private Double previousPrice;
    private Long volume;
    private Double marketCap;
    private String sector;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 기본 생성자
    public StockResponse() {}
    
    // 생성자
    public StockResponse(Long id, String symbol, String companyName, Double currentPrice, Double previousPrice, 
                        Long volume, Double marketCap, String sector, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.previousPrice = previousPrice;
        this.volume = volume;
        this.marketCap = marketCap;
        this.sector = sector;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // from 메서드
    public static StockResponse from(Stock stock) {
        return new StockResponse(
            stock.getId(),
            stock.getSymbol(),
            stock.getCompanyName(),
            stock.getCurrentPrice(),
            stock.getPreviousPrice(),
            stock.getVolume(),
            stock.getMarketCap(),
            stock.getSector(),
            stock.getCreatedAt(),
            stock.getUpdatedAt()
        );
    }
    
    // Getter와 Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
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
    
    public Double getMarketCap() {
        return marketCap;
    }
    
    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }
    
    public String getSector() {
        return sector;
    }
    
    public void setSector(String sector) {
        this.sector = sector;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 