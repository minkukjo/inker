package com.example.inker.stock.service;

import com.example.inker.stock.entity.Stock;
import com.example.inker.stock.dto.CreateStockRequest;
import com.example.inker.stock.exception.StockNotFoundException;
import com.example.inker.stock.exception.StockValidationException;
import com.example.inker.stock.exception.InvalidPriceException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 재고 관리 서비스
 * 10단계 깊이의 비즈니스 로직을 포함합니다.
 */
@Service
public class StockService {
    
    private final StockAnalysisOrchestrator orchestrator;
    private final StockDataProcessor dataProcessor;
    private final StockPriceAnalyzer priceAnalyzer;
    private final StockMarketAnalyzer marketAnalyzer;
    private final StockVolumeAnalyzer volumeAnalyzer;
    private final StockRiskAnalyzer riskAnalyzer;
    private final StockPerformanceAnalyzer performanceAnalyzer;
    private final StockTrendAnalyzer trendAnalyzer;
    private final StockPredictionEngine predictionEngine;
    private final StockRecommendationEngine recommendationEngine;
    
    // 임시 데이터 저장소
    private final List<Stock> stocks = new ArrayList<>();
    
    public StockService() {
        this.orchestrator = new StockAnalysisOrchestrator();
        this.dataProcessor = new StockDataProcessor();
        this.priceAnalyzer = new StockPriceAnalyzer();
        this.marketAnalyzer = new StockMarketAnalyzer();
        this.volumeAnalyzer = new StockVolumeAnalyzer();
        this.riskAnalyzer = new StockRiskAnalyzer();
        this.performanceAnalyzer = new StockPerformanceAnalyzer();
        this.trendAnalyzer = new StockTrendAnalyzer();
        this.predictionEngine = new StockPredictionEngine();
        this.recommendationEngine = new StockRecommendationEngine();
        
        // 샘플 데이터 초기화
        initializeSampleData();
    }
    
    private void initializeSampleData() {
        Stock stock1 = new Stock("AAPL", "Apple Inc.", 150.0, 145.0, 1000000L, 2500000000000.0, "Technology");
        stock1.setId(1L);
        stocks.add(stock1);
        
        Stock stock2 = new Stock("GOOGL", "Alphabet Inc.", 2800.0, 2750.0, 500000L, 1800000000000.0, "Technology");
        stock2.setId(2L);
        stocks.add(stock2);
        
        Stock stock3 = new Stock("MSFT", "Microsoft Corporation", 300.0, 295.0, 800000L, 2200000000000.0, "Technology");
        stock3.setId(3L);
        stocks.add(stock3);
    }
    
    /**
     * 재고 ID로 조회 (1단계)
     */
    public StockResponse getStockById(Long id) {
        // 1단계: 기본 검증
        if (id == null || id <= 0) {
            throw new StockValidationException("유효하지 않은 재고 ID입니다");
        }
        
        // 2단계: 데이터 처리
        dataProcessor.validateStockId(id);
        
        // 3단계: 가격 분석
        priceAnalyzer.analyzePriceForId(id);
        
        // 4단계: 시장 분석
        marketAnalyzer.analyzeMarketForId(id);
        
        // 5단계: 거래량 분석
        volumeAnalyzer.analyzeVolumeForId(id);
        
        // 6단계: 리스크 분석
        riskAnalyzer.analyzeRiskForId(id);
        
        // 7단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForId(id);
        
        // 8단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForId(id);
        
        // 9단계: 예측 엔진
        predictionEngine.predictForId(id);
        
        // 10단계: 추천 엔진
        recommendationEngine.recommendForId(id);
        
        Optional<Stock> stock = stocks.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
                
        if (stock.isEmpty()) {
            throw new StockNotFoundException("재고 ID " + id + "를 찾을 수 없습니다");
        }
        
        return StockResponse.from(stock.get());
    }
    
    /**
     * 재고 심볼로 조회 (1단계)
     */
    public StockResponse getStockBySymbol(String symbol) {
        // 1단계: 기본 검증
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new StockValidationException("유효하지 않은 재고 심볼입니다");
        }
        
        // 2단계: 데이터 처리
        dataProcessor.validateStockSymbol(symbol);
        
        // 3단계: 가격 분석
        priceAnalyzer.analyzePriceForSymbol(symbol);
        
        // 4단계: 시장 분석
        marketAnalyzer.analyzeMarketForSymbol(symbol);
        
        // 5단계: 거래량 분석
        volumeAnalyzer.analyzeVolumeForSymbol(symbol);
        
        // 6단계: 리스크 분석
        riskAnalyzer.analyzeRiskForSymbol(symbol);
        
        // 7단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForSymbol(symbol);
        
        // 8단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForSymbol(symbol);
        
        // 9단계: 예측 엔진
        predictionEngine.predictForSymbol(symbol);
        
        // 10단계: 추천 엔진
        recommendationEngine.recommendForSymbol(symbol);
        
        Optional<Stock> stock = stocks.stream()
                .filter(s -> s.getSymbol().equalsIgnoreCase(symbol))
                .findFirst();
                
        if (stock.isEmpty()) {
            throw new StockNotFoundException("재고 심볼 " + symbol + "를 찾을 수 없습니다");
        }
        
        return StockResponse.from(stock.get());
    }
    
    /**
     * 모든 재고 조회 (1단계)
     */
    public List<StockResponse> getAllStocks() {
        // 1단계: 기본 검증
        if (stocks.isEmpty()) {
            throw new StockValidationException("재고 데이터가 없습니다");
        }
        
        // 2단계: 데이터 처리
        dataProcessor.validateAllStocks();
        
        // 3단계: 가격 분석
        priceAnalyzer.analyzeAllPrices();
        
        // 4단계: 시장 분석
        marketAnalyzer.analyzeAllMarkets();
        
        // 5단계: 거래량 분석
        volumeAnalyzer.analyzeAllVolumes();
        
        // 6단계: 리스크 분석
        riskAnalyzer.analyzeAllRisks();
        
        // 7단계: 성과 분석
        performanceAnalyzer.analyzeAllPerformances();
        
        // 8단계: 트렌드 분석
        trendAnalyzer.analyzeAllTrends();
        
        // 9단계: 예측 엔진
        predictionEngine.predictAll();
        
        // 10단계: 추천 엔진
        recommendationEngine.recommendAll();
        
        return stocks.stream()
                .map(StockResponse::from)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 재고 업데이트 (1단계)
     */
    public StockResponse updateStock(Long id, UpdateStockRequest request) {
        // 1단계: 기본 검증
        if (id == null || updatedStock == null) {
            throw new StockValidationException("유효하지 않은 업데이트 요청입니다");
        }
        
        // 2단계: 데이터 처리
        dataProcessor.validateUpdateRequest(id, updatedStock);
        
        // 3단계: 가격 분석
        priceAnalyzer.analyzePriceForUpdate(id);
        
        // 4단계: 시장 분석
        marketAnalyzer.analyzeMarketForUpdate(id);
        
        // 5단계: 거래량 분석
        volumeAnalyzer.analyzeVolumeForUpdate(id);
        
        // 6단계: 리스크 분석
        riskAnalyzer.analyzeRiskForUpdate(id);
        
        // 7단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForUpdate(id);
        
        // 8단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForUpdate(id);
        
        // 9단계: 예측 엔진
        predictionEngine.predictForUpdate(id);
        
        // 10단계: 추천 엔진
        recommendationEngine.recommendForUpdate(id);
        
        for (int i = 0; i < stocks.size(); i++) {
            if (stocks.get(i).getId().equals(id)) {
                updatedStock.setId(id);
                stocks.set(i, updatedStock);
                return updatedStock;
            }
        }
        
        throw new StockNotFoundException("재고 ID " + id + "를 찾을 수 없습니다");
    }
    
    /**
     * 재고 가격 업데이트 (1단계)
     */
    public Stock updateStockPrice(Long id, Double newPrice) {
        // 1단계: 기본 검증
        if (id == null || newPrice == null || newPrice <= 0) {
            throw new StockValidationException("유효하지 않은 가격 업데이트 요청입니다");
        }
        
        // 2단계: 데이터 처리
        dataProcessor.validatePriceUpdate(id, newPrice);
        
        // 3단계: 가격 분석
        priceAnalyzer.analyzePriceUpdate(id, newPrice);
        
        // 4단계: 시장 분석
        marketAnalyzer.analyzeMarketForPriceUpdate(id);
        
        // 5단계: 거래량 분석
        volumeAnalyzer.analyzeVolumeForPriceUpdate(id);
        
        // 6단계: 리스크 분석
        riskAnalyzer.analyzeRiskForPriceUpdate(id);
        
        // 7단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForPriceUpdate(id);
        
        // 8단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForPriceUpdate(id);
        
        // 9단계: 예측 엔진
        predictionEngine.predictForPriceUpdate(id);
        
        // 10단계: 추천 엔진
        recommendationEngine.recommendForPriceUpdate(id);
        
        for (Stock stock : stocks) {
            if (stock.getId().equals(id)) {
                stock.setPrice(newPrice);
                return stock;
            }
        }
        
        throw new StockNotFoundException("재고 ID " + id + "를 찾을 수 없습니다");
    }
    
    /**
     * 새 재고 생성 (1단계)
     */
    public Stock createStock(CreateStockRequest request) {
        // 1단계: 기본 검증
        if (request == null || request.getSymbol() == null || request.getName() == null) {
            throw new StockValidationException("유효하지 않은 재고 생성 요청입니다");
        }
        
        // 2단계: 데이터 처리
        dataProcessor.validateCreateRequest(request);
        
        // 3단계: 가격 분석
        priceAnalyzer.analyzePriceForCreate(request);
        
        // 4단계: 시장 분석
        marketAnalyzer.analyzeMarketForCreate(request);
        
        // 5단계: 거래량 분석
        volumeAnalyzer.analyzeVolumeForCreate(request);
        
        // 6단계: 리스크 분석
        riskAnalyzer.analyzeRiskForCreate(request);
        
        // 7단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForCreate(request);
        
        // 8단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForCreate(request);
        
        // 9단계: 예측 엔진
        predictionEngine.predictForCreate(request);
        
        // 10단계: 추천 엔진
        recommendationEngine.recommendForCreate(request);
        
        Long newId = stocks.stream()
                .mapToLong(Stock::getId)
                .max()
                .orElse(0L) + 1;
                
        Stock newStock = new Stock(newId, request.getSymbol(), request.getName(), 
                request.getPrice(), request.getQuantity());
        stocks.add(newStock);
        
        return newStock;
    }
    
    /**
     * 재고 삭제 (1단계)
     */
    public void deleteStock(Long id) {
        // 1단계: 기본 검증
        if (id == null || id <= 0) {
            throw new StockValidationException("유효하지 않은 재고 삭제 요청입니다");
        }
        
        // 2단계: 데이터 처리
        dataProcessor.validateDeleteRequest(id);
        
        // 3단계: 가격 분석
        priceAnalyzer.analyzePriceForDelete(id);
        
        // 4단계: 시장 분석
        marketAnalyzer.analyzeMarketForDelete(id);
        
        // 5단계: 거래량 분석
        volumeAnalyzer.analyzeVolumeForDelete(id);
        
        // 6단계: 리스크 분석
        riskAnalyzer.analyzeRiskForDelete(id);
        
        // 7단계: 성과 분석
        performanceAnalyzer.analyzePerformanceForDelete(id);
        
        // 8단계: 트렌드 분석
        trendAnalyzer.analyzeTrendForDelete(id);
        
        // 9단계: 예측 엔진
        predictionEngine.predictForDelete(id);
        
        // 10단계: 추천 엔진
        recommendationEngine.recommendForDelete(id);
        
        boolean removed = stocks.removeIf(stock -> stock.getId().equals(id));
        if (!removed) {
            throw new StockNotFoundException("재고 ID " + id + "를 찾을 수 없습니다");
        }
    }
} 