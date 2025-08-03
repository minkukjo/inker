package com.example.inker.stock.service;

import com.example.inker.stock.dto.CreateStockRequest;
import com.example.inker.stock.exception.InvalidPriceException;

/**
 * 재고 리스크 분석기 (7단계)
 * 리스크 관련 분석을 담당합니다.
 */
public class StockRiskAnalyzer {
    
    private final StockPerformanceAnalyzer performanceAnalyzer;
    private final StockTrendAnalyzer trendAnalyzer;
    private final StockPredictionEngine predictionEngine;
    private final StockRecommendationEngine recommendationEngine;
    
    public StockRiskAnalyzer() {
        this.performanceAnalyzer = new StockPerformanceAnalyzer();
        this.trendAnalyzer = new StockTrendAnalyzer();
        this.predictionEngine = new StockPredictionEngine();
        this.recommendationEngine = new StockRecommendationEngine();
    }
    
    public void analyzeRiskForId(Long id) {
        // 7단계: 리스크 분석
        if (id == null || id <= 0) {
            throw new InvalidPriceException("리스크 분석을 위한 유효하지 않은 ID입니다");
        }
        
        // 8단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForId(id);
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForId(id);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForId(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForId(id);
    }
    
    public void analyzeRiskForSymbol(String symbol) {
        // 7단계: 리스크 분석
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new InvalidPriceException("리스크 분석을 위한 유효하지 않은 심볼입니다");
        }
        
        // 8단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForSymbol(symbol);
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForSymbol(symbol);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForSymbol(symbol);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForSymbol(symbol);
    }
    
    public void analyzeAllRisks() {
        // 7단계: 전체 리스크 분석
        // 8단계: 성과 분석
        performanceAnalyzer.analyzeAllPerformances();
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeAllTrends();
        
        // 10단계: 예측 엔진
        predictionEngine.predictAll();
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendAll();
    }
    
    public void analyzeRiskForUpdate(Long id) {
        // 7단계: 업데이트용 리스크 분석
        if (id == null || id <= 0) {
            throw new InvalidPriceException("업데이트를 위한 유효하지 않은 ID입니다");
        }
        
        // 8단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForUpdate(id);
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForUpdate(id);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForUpdate(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForUpdate(id);
    }
    
    public void analyzeRiskForPriceUpdate(Long id) {
        // 7단계: 가격 업데이트용 리스크 분석
        if (id == null || id <= 0) {
            throw new InvalidPriceException("가격 업데이트를 위한 유효하지 않은 ID입니다");
        }
        
        // 8단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForPriceUpdate(id);
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForPriceUpdate(id);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForPriceUpdate(id);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForPriceUpdate(id);
    }
    
    public void analyzeRiskForCreate(CreateStockRequest request) {
        // 7단계: 생성용 리스크 분석
        if (request == null || request.getSymbol() == null) {
            throw new InvalidPriceException("생성을 위한 유효하지 않은 요청입니다");
        }
        
        // 8단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForCreate(request);
        
        // 9단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForCreate(request);
        
        // 10단계: 예측 엔진
        predictionEngine.predictForCreate(request);
        
        // 11단계: 추천 엔진
        recommendationEngine.recommendForCreate(request);
    }
    
    public void analyzeRiskForDelete(Long id) {
        // 7단계: 삭제용 리스크 분석
        if (id == null || id <= 0) {
            throw new InvalidPriceException("삭제를 위한 유효하지 않은 ID입니다");
        }
        
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