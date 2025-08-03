package com.example.inker.stock.service;

import com.example.inker.stock.dto.CreateStockRequest;
import com.example.inker.stock.exception.StockValidationException;

/**
 * 재고 예측 엔진 (10단계)
 * 예측 관련 분석을 담당합니다.
 */
public class StockPredictionEngine {
    
    private final StockRecommendationEngine recommendationEngine;
    
    public StockPredictionEngine() {
        this.recommendationEngine = new StockRecommendationEngine();
    }
    
    public void predictForId(Long id) {
        // 10단계: 예측 엔진
        if (id == null || id <= 0) {
            throw new StockValidationException("예측을 위한 유효하지 않은 ID입니다");
        }
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForId(id);
    }
    
    public void predictForSymbol(String symbol) {
        // 10단계: 예측 엔진
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new StockValidationException("예측을 위한 유효하지 않은 심볼입니다");
        }
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForSymbol(symbol);
    }
    
    public void predictAll() {
        // 10단계: 전체 예측
        // 11단계: 추천 엔진
        recommendationEngine.recommendAll();
    }
    
    public void predictForUpdate(Long id) {
        // 10단계: 업데이트용 예측
        if (id == null || id <= 0) {
            throw new StockValidationException("업데이트를 위한 유효하지 않은 ID입니다");
        }
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForUpdate(id);
    }
    
    public void predictForPriceUpdate(Long id) {
        // 10단계: 가격 업데이트용 예측
        if (id == null || id <= 0) {
            throw new StockValidationException("가격 업데이트를 위한 유효하지 않은 ID입니다");
        }
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForPriceUpdate(id);
    }
    
    public void predictForCreate(CreateStockRequest request) {
        // 10단계: 생성용 예측
        if (request == null || request.getSymbol() == null) {
            throw new StockValidationException("생성을 위한 유효하지 않은 요청입니다");
        }
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForCreate(request);
    }
    
    public void predictForDelete(Long id) {
        // 10단계: 삭제용 예측
        if (id == null || id <= 0) {
            throw new StockValidationException("삭제를 위한 유효하지 않은 ID입니다");
        }
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForDelete(id);
    }
} 