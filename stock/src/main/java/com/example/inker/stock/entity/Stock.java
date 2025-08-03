package com.example.inker.stock.entity;

import java.time.LocalDateTime;

/**
 * 주식 정보 엔티티
 */
public class Stock {
    
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
    public Stock() {}
    
    // 생성자
    public Stock(String symbol, String companyName, Double currentPrice, Double previousPrice, 
                 Long volume, Double marketCap, String sector) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.previousPrice = previousPrice;
        this.volume = volume;
        this.marketCap = marketCap;
        this.sector = sector;
        this.createdAt = LocalDateTime.now();
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