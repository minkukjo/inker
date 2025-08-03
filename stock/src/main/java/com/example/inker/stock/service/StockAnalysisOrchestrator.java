package com.example.inker.stock.service;

import com.example.inker.stock.dto.CreateStockRequest;
import com.example.inker.stock.exception.StockNotFoundException;
import com.example.inker.stock.exception.StockValidationException;

/**
 * 재고 분석 오케스트레이터 (2단계)
 * 다른 분석 클래스들을 조율합니다.
 */
public class StockAnalysisOrchestrator {
    
    private final StockDataProcessor dataProcessor;
    private final StockPriceAnalyzer priceAnalyzer;
    private final StockMarketAnalyzer marketAnalyzer;
    private final StockVolumeAnalyzer volumeAnalyzer;
    private final StockRiskAnalyzer riskAnalyzer;
    private final StockPerformanceAnalyzer performanceAnalyzer;
    private final StockTrendAnalyzer trendAnalyzer;
    private final StockPredictionEngine predictionEngine;
    private final StockRecommendationEngine recommendationEngine;
    
    public StockAnalysisOrchestrator() {
        this.dataProcessor = new StockDataProcessor();
        this.priceAnalyzer = new StockPriceAnalyzer();
        this.marketAnalyzer = new StockMarketAnalyzer();
        this.volumeAnalyzer = new StockVolumeAnalyzer();
        this.riskAnalyzer = new StockRiskAnalyzer();
        this.performanceAnalyzer = new StockPerformanceAnalyzer();
        this.trendAnalyzer = new StockTrendAnalyzer();
        this.predictionEngine = new StockPredictionEngine();
        this.recommendationEngine = new StockRecommendationEngine();
    }
    
    public void orchestrateAnalysis(Long id) {
        // 2단계: 오케스트레이션 시작
        if (id == null || id <= 0) {
            throw new StockValidationException("재고 분석을 위한 유효하지 않은 ID입니다");
        }
        
        // 3단계: 데이터 처리
        dataProcessor.validateStockId(id);
        
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
    
    public void orchestrateAnalysis(String symbol) {
        // 2단계: 오케스트레이션 시작
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new StockValidationException("재고 분석을 위한 유효하지 않은 심볼입니다");
        }
        
        // 3단계: 데이터 처리
        dataProcessor.validateStockSymbol(symbol);
        
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
} 