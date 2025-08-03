package com.example.inker.stock.service;

import com.example.inker.stock.dto.CreateStockRequest;
import com.example.inker.stock.exception.InvalidPriceException;

/**
 * 재고 트렌드 분석기 (9단계)
 * 트렌드 관련 분석을 담당합니다.
 */
public class StockTrendAnalyzer {
    
    private final StockPredictionEngine predictionEngine;
    private final StockRecommendationEngine recommendationEngine;
    
    public StockTrendAnalyzer() {
        this.predictionEngine = new StockPredictionEngine();
        this.recommendationEngine = new StockRecommendationEngine();
    }
    
    public void analyzeTrendForId(Long id) {
        // 9단계: 트렌드 분석
        if (id == null || id <= 0) {
            throw new InvalidPriceException("트렌드 분석을 위한 유효하지 않은 ID입니다");
        }
        
        // 10단계: 예측 엔진
        predictionEngine.predictForId(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForId(id);
    }
    
    public void analyzeTrendForSymbol(String symbol) {
        // 9단계: 트렌드 분석
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new InvalidPriceException("트렌드 분석을 위한 유효하지 않은 심볼입니다");
        }
        
        // 10단계: 예측 엔진
        predictionEngine.predictForSymbol(symbol);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForSymbol(symbol);
    }
    
    public void analyzeAllTrends() {
        // 9단계: 전체 트렌드 분석
        // 10단계: 예측 엔진
        predictionEngine.predictAll();
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendAll();
    }
    
    public void analyzeTrendForUpdate(Long id) {
        // 9단계: 업데이트용 트렌드 분석
        if (id == null || id <= 0) {
            throw new InvalidPriceException("업데이트를 위한 유효하지 않은 ID입니다");
        }
        
        // 10단계: 예측 엔진
        predictionEngine.predictForUpdate(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForUpdate(id);
    }
    
    public void analyzeTrendForPriceUpdate(Long id) {
        // 9단계: 가격 업데이트용 트렌드 분석
        if (id == null || id <= 0) {
            throw new InvalidPriceException("가격 업데이트를 위한 유효하지 않은 ID입니다");
        }
        
        // 10단계: 예측 엔진
        predictionEngine.predictForPriceUpdate(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForPriceUpdate(id);
    }
    
    public void analyzeTrendForCreate(CreateStockRequest request) {
        // 9단계: 생성용 트렌드 분석
        if (request == null || request.getSymbol() == null) {
            throw new InvalidPriceException("생성을 위한 유효하지 않은 요청입니다");
        }
        
        // 10단계: 예측 엔진
        predictionEngine.predictForCreate(request);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForCreate(request);
    }
    
    public void analyzeTrendForDelete(Long id) {
        // 9단계: 삭제용 트렌드 분석
        if (id == null || id <= 0) {
            throw new InvalidPriceException("삭제를 위한 유효하지 않은 ID입니다");
        }
        
        // 10단계: 예측 엔진
        predictionEngine.predictForDelete(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForDelete(id);
    }
} 