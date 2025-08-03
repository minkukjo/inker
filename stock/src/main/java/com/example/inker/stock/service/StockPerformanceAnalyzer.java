package com.example.inker.stock.service;

import com.example.inker.stock.dto.CreateStockRequest;
import com.example.inker.stock.exception.StockValidationException;

/**
 * 재고 성과 분석기 (8단계)
 * 성과 관련 분석을 담당합니다.
 */
public class StockPerformanceAnalyzer {
    
    private final StockTrendAnalyzer trendAnalyzer;
    private final StockPredictionEngine predictionEngine;
    private final StockRecommendationEngine recommendationEngine;
    
    public StockPerformanceAnalyzer() {
        this.trendAnalyzer = new StockTrendAnalyzer();
        this.predictionEngine = new StockPredictionEngine();
        this.recommendationEngine = new StockRecommendationEngine();
    }
    
    public void analyzePerformanceForId(Long id) {
        // 8단계: 성과 분석
        if (id == null || id <= 0) {
            throw new StockValidationException("성과 분석을 위한 유효하지 않은 ID입니다");
        }
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForId(id);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForId(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForId(id);
    }
    
    public void analyzePerformanceForSymbol(String symbol) {
        // 8단계: 성과 분석
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new StockValidationException("성과 분석을 위한 유효하지 않은 심볼입니다");
        }
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForSymbol(symbol);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForSymbol(symbol);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForSymbol(symbol);
    }
    
    public void analyzeAllPerformances() {
        // 8단계: 전체 성과 분석
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeAllTrends();
        
        // 10단계: 예측 엔진
        predictionEngine.predictAll();
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendAll();
    }
    
    public void analyzePerformanceForUpdate(Long id) {
        // 8단계: 업데이트용 성과 분석
        if (id == null || id <= 0) {
            throw new StockValidationException("업데이트를 위한 유효하지 않은 ID입니다");
        }
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForUpdate(id);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForUpdate(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForUpdate(id);
    }
    
    public void analyzePerformanceForPriceUpdate(Long id) {
        // 8단계: 가격 업데이트용 성과 분석
        if (id == null || id <= 0) {
            throw new StockValidationException("가격 업데이트를 위한 유효하지 않은 ID입니다");
        }
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForPriceUpdate(id);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForPriceUpdate(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForPriceUpdate(id);
    }
    
    public void analyzePerformanceForCreate(CreateStockRequest request) {
        // 8단계: 생성용 성과 분석
        if (request == null || request.getSymbol() == null) {
            throw new StockValidationException("생성을 위한 유효하지 않은 요청입니다");
        }
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForCreate(request);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForCreate(request);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForCreate(request);
    }
    
    public void analyzePerformanceForDelete(Long id) {
        // 8단계: 삭제용 성과 분석
        if (id == null || id <= 0) {
            throw new StockValidationException("삭제를 위한 유효하지 않은 ID입니다");
        }
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForDelete(id);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForDelete(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForDelete(id);
    }
} 