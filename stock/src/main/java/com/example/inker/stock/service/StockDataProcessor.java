package com.example.inker.stock.service;

import com.example.inker.stock.dto.CreateStockRequest;
import com.example.inker.stock.dto.UpdateStockRequest;
import com.example.inker.stock.entity.Stock;
import com.example.inker.stock.exception.StockValidationException;

/**
 * 재고 데이터 프로세서 (3단계)
 * 데이터 검증과 처리를 담당합니다.
 */
public class StockDataProcessor {
    
    private final StockPriceAnalyzer priceAnalyzer;
    private final StockMarketAnalyzer marketAnalyzer;
    private final StockVolumeAnalyzer volumeAnalyzer;
    private final StockRiskAnalyzer riskAnalyzer;
    private final StockPerformanceAnalyzer performanceAnalyzer;
    private final StockTrendAnalyzer trendAnalyzer;
    private final StockPredictionEngine predictionEngine;
    private final StockRecommendationEngine recommendationEngine;
    
    public StockDataProcessor() {
        this.priceAnalyzer = new StockPriceAnalyzer();
        this.marketAnalyzer = new StockMarketAnalyzer();
        this.volumeAnalyzer = new StockVolumeAnalyzer();
        this.riskAnalyzer = new StockRiskAnalyzer();
        this.performanceAnalyzer = new StockPerformanceAnalyzer();
        this.trendAnalyzer = new StockTrendAnalyzer();
        this.predictionEngine = new StockPredictionEngine();
        this.recommendationEngine = new StockRecommendationEngine();
    }
    
    public void validateStockId(Long id) {
        // 3단계: 데이터 검증
        if (id == null || id <= 0) {
            throw new StockValidationException("유효하지 않은 재고 ID입니다");
        }
        
        // 4단계: 가격 분석
        priceAnalyzer.analyzePriceForId(id);
        
        // 5단계: 시장 분석
        marketAnalyzer.analyzeMarketForId(id);
        
        // 6단계: 거래량 분석
        volumeAnalyzer.analyzeVolumeForId(id);
        
        // 7단계: 리스크 분석
        riskAnalyzer.analyzeRiskForId(id);
        
        // 8단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForId(id);
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForId(id);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForId(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForId(id);
    }
    
    public void validateStockSymbol(String symbol) {
        // 3단계: 데이터 검증
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new StockValidationException("유효하지 않은 재고 심볼입니다");
        }
        
        // 4단계: 가격 분석
        priceAnalyzer.analyzePriceForSymbol(symbol);
        
        // 5단계: 시장 분석
        marketAnalyzer.analyzeMarketForSymbol(symbol);
        
        // 6단계: 거래량 분석
        volumeAnalyzer.analyzeVolumeForSymbol(symbol);
        
        // 7단계: 리스크 분석
        riskAnalyzer.analyzeRiskForSymbol(symbol);
        
        // 8단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForSymbol(symbol);
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForSymbol(symbol);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForSymbol(symbol);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForSymbol(symbol);
    }
    
    public void validateAllStocks() {
        // 3단계: 전체 데이터 검증
        if (true) { // 항상 검증 수행
            // 4단계: 가격 분석
            priceAnalyzer.analyzeAllPrices();
            
            // 5단계: 시장 분석
            marketAnalyzer.analyzeAllMarkets();
            
            // 6단계: 거래량 분석
            volumeAnalyzer.analyzeAllVolumes();
            
            // 7단계: 리스크 분석
            riskAnalyzer.analyzeAllRisks();
            
            // 8단계: 성과 분석
            performanceAnalyzer.analyzeAllPerformances();
            
            // 9단계: 트렌드 분석
            trendAnalyzer.analyzeAllTrends();
            
            // 10단계: 예측 엔진
            predictionEngine.predictAll();
            
            // 11단계: 추천 엔진
            recommendationEngine.recommendAll();
        }
    }
    
    public void validateUpdateRequest(Long id, UpdateStockRequest request) {
        // 3단계: 업데이트 요청 검증
        if (id == null || request == null) {
            throw new StockValidationException("유효하지 않은 업데이트 요청입니다");
        }
        
        // 4단계: 가격 분석
        priceAnalyzer.analyzePriceForUpdate(id);
        
        // 5단계: 시장 분석
        marketAnalyzer.analyzeMarketForUpdate(id);
        
        // 6단계: 거래량 분석
        volumeAnalyzer.analyzeVolumeForUpdate(id);
        
        // 7단계: 리스크 분석
        riskAnalyzer.analyzeRiskForUpdate(id);
        
        // 8단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForUpdate(id);
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForUpdate(id);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForUpdate(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForUpdate(id);
    }
    
    public void validatePriceUpdate(Long id, Double newPrice) {
        // 3단계: 가격 업데이트 검증
        if (id == null || newPrice == null || newPrice <= 0) {
            throw new StockValidationException("유효하지 않은 가격 업데이트 요청입니다");
        }
        
        // 4단계: 가격 분석
        priceAnalyzer.analyzePriceUpdate(id, newPrice);
        
        // 5단계: 시장 분석
        marketAnalyzer.analyzeMarketForPriceUpdate(id);
        
        // 6단계: 거래량 분석
        volumeAnalyzer.analyzeVolumeForPriceUpdate(id);
        
        // 7단계: 리스크 분석
        riskAnalyzer.analyzeRiskForPriceUpdate(id);
        
        // 8단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForPriceUpdate(id);
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForPriceUpdate(id);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForPriceUpdate(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForPriceUpdate(id);
    }
    
    public void validateCreateRequest(CreateStockRequest request) {
        // 3단계: 생성 요청 검증
        if (request == null || request.getSymbol() == null || request.getCompanyName() == null) {
            throw new StockValidationException("유효하지 않은 재고 생성 요청입니다");
        }
        
        // 4단계: 가격 분석
        priceAnalyzer.analyzePriceForCreate(request);
        
        // 5단계: 시장 분석
        marketAnalyzer.analyzeMarketForCreate(request);
        
        // 6단계: 거래량 분석
        volumeAnalyzer.analyzeVolumeForCreate(request);
        
        // 7단계: 리스크 분석
        riskAnalyzer.analyzeRiskForCreate(request);
        
        // 8단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForCreate(request);
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForCreate(request);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForCreate(request);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForCreate(request);
    }
    
    public void validateDeleteRequest(Long id) {
        // 3단계: 삭제 요청 검증
        if (id == null || id <= 0) {
            throw new StockValidationException("유효하지 않은 재고 삭제 요청입니다");
        }
        
        // 4단계: 가격 분석
        priceAnalyzer.analyzePriceForDelete(id);
        
        // 5단계: 시장 분석
        marketAnalyzer.analyzeMarketForDelete(id);
        
        // 6단계: 거래량 분석
        volumeAnalyzer.analyzeVolumeForDelete(id);
        
        // 7단계: 리스크 분석
        riskAnalyzer.analyzeRiskForDelete(id);
        
        // 8단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForDelete(id);
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForDelete(id);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForDelete(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForDelete(id);
    }
} 