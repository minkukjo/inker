package com.example.inker.stock.service;

import com.example.inker.stock.dto.CreateStockRequest;
import com.example.inker.stock.exception.StockValidationException;

/**
 * 재고 거래량 분석기 (6단계)
 * 거래량 관련 분석을 담당합니다.
 */
public class StockVolumeAnalyzer {
    
    private final StockRiskAnalyzer riskAnalyzer;
    private final StockPerformanceAnalyzer performanceAnalyzer;
    private final StockTrendAnalyzer trendAnalyzer;
    private final StockPredictionEngine predictionEngine;
    private final StockRecommendationEngine recommendationEngine;
    
    public StockVolumeAnalyzer() {
        this.riskAnalyzer = new StockRiskAnalyzer();
        this.performanceAnalyzer = new StockPerformanceAnalyzer();
        this.trendAnalyzer = new StockTrendAnalyzer();
        this.predictionEngine = new StockPredictionEngine();
        this.recommendationEngine = new StockRecommendationEngine();
    }
    
    public void analyzeVolumeForId(Long id) {
        // 6단계: 거래량 분석
        if (id == null || id <= 0) {
            throw new StockValidationException("거래량 분석을 위한 유효하지 않은 ID입니다");
        }
        
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
    
    public void analyzeVolumeForSymbol(String symbol) {
        // 6단계: 거래량 분석
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new StockValidationException("거래량 분석을 위한 유효하지 않은 심볼입니다");
        }
        
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
    
    public void analyzeAllVolumes() {
        // 6단계: 전체 거래량 분석
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
    
    public void analyzeVolumeForUpdate(Long id) {
        // 6단계: 업데이트용 거래량 분석
        if (id == null || id <= 0) {
            throw new StockValidationException("업데이트를 위한 유효하지 않은 ID입니다");
        }
        
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
    
    public void analyzeVolumeForPriceUpdate(Long id) {
        // 6단계: 가격 업데이트용 거래량 분석
        if (id == null || id <= 0) {
            throw new StockValidationException("가격 업데이트를 위한 유효하지 않은 ID입니다");
        }
        
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
    
    public void analyzeVolumeForCreate(CreateStockRequest request) {
        // 6단계: 생성용 거래량 분석
        if (request == null || request.getSymbol() == null) {
            throw new StockValidationException("생성을 위한 유효하지 않은 요청입니다");
        }
        
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
    
    public void analyzeVolumeForDelete(Long id) {
        // 6단계: 삭제용 거래량 분석
        if (id == null || id <= 0) {
            throw new StockValidationException("삭제를 위한 유효하지 않은 ID입니다");
        }
        
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