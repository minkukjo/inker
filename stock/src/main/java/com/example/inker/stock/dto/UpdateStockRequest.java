package com.example.inker.stock.dto;

/**
 * 주식 수정 요청 DTO
 */
public class UpdateStockRequest {
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
