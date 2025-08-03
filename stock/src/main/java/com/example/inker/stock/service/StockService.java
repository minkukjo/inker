package com.example.inker.stock.service;

import com.example.inker.stock.dto.CreateStockRequest;
import com.example.inker.stock.dto.UpdateStockRequest;
import com.example.inker.stock.dto.UpdateStockPriceRequest;
import com.example.inker.stock.dto.StockResponse;
import com.example.inker.stock.entity.Stock;
import com.example.inker.stock.exception.StockNotFoundException;
import com.example.inker.stock.exception.InvalidPriceException;
import com.example.inker.stock.exception.StockValidationException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 재고 관련 서비스
 */
@Service
public class StockService {
    
    // 메모리 기반 저장소 (실제로는 데이터베이스를 사용)
    private final Map<Long, Stock> stocks = new ConcurrentHashMap<>();
    private long nextId = 1;
    
    // 깊은 분석 클래스들
    private final StockAnalysisOrchestrator orchestrator = new StockAnalysisOrchestrator();
    private final StockDataProcessor dataProcessor = new StockDataProcessor();
    private final StockPriceAnalyzer priceAnalyzer = new StockPriceAnalyzer();

    /**
     * 모든 재고 조회
     */
    public List<StockResponse> getAllStocks() {
        return stocks.values().stream()
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * ID로 재고 조회
     */
    public StockResponse getStockById(Long id) {
        Stock stock = stocks.get(id);
        if (stock == null) {
            throw new StockNotFoundException("ID " + id + "에 해당하는 재고를 찾을 수 없습니다");
        }
        return StockResponse.from(stock);
    }
    
    /**
     * 심볼로 재고 조회
     */
    public StockResponse getStockBySymbol(String symbol) {
        Stock stock = stocks.values().stream()
                .filter(s -> s.getSymbol().equals(symbol))
                .findFirst()
                .orElse(null);
        if (stock == null) {
            throw new StockNotFoundException("심볼 " + symbol + "에 해당하는 재고를 찾을 수 없습니다");
        }
        return StockResponse.from(stock);
    }
    
    /**
     * 섹터별 재고 조회
     */
    public List<StockResponse> getStocksBySector(String sector) {
        return stocks.values().stream()
                .filter(s -> s.getSector().equals(sector))
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 재고 생성
     */
    public StockResponse createStock(CreateStockRequest request) {
        // 깊은 데이터 검증
        Stock stock = new Stock(
            request.getSymbol(),
            request.getCompanyName(),
            request.getCurrentPrice(),
            request.getPreviousPrice(),
            request.getVolume(),
            request.getMarketCap(),
            request.getSector()
        );
        
        // 데이터 검증 수행
        dataProcessor.validateStockData(stock);
        
        stock.setId(nextId++);
        stock.setCreatedAt(LocalDateTime.now());
        
        stocks.put(stock.getId(), stock);
        return StockResponse.from(stock);
    }
    
    /**
     * 재고 수정
     */
    public StockResponse updateStock(Long id, UpdateStockRequest request) {
        Stock stock = stocks.get(id);
        if (stock == null) {
            throw new StockNotFoundException("ID " + id + "에 해당하는 재고를 찾을 수 없습니다");
        }
        
        if (request.getSymbol() != null) {
            stock.setSymbol(request.getSymbol());
        }
        if (request.getCompanyName() != null) {
            stock.setCompanyName(request.getCompanyName());
        }
        if (request.getCurrentPrice() != null) {
            stock.setCurrentPrice(request.getCurrentPrice());
        }
        if (request.getPreviousPrice() != null) {
            stock.setPreviousPrice(request.getPreviousPrice());
        }
        if (request.getVolume() != null) {
            stock.setVolume(request.getVolume());
        }
        if (request.getMarketCap() != null) {
            stock.setMarketCap(request.getMarketCap());
        }
        if (request.getSector() != null) {
            stock.setSector(request.getSector());
        }
        stock.setUpdatedAt(LocalDateTime.now());
        return StockResponse.from(stock);
    }
    
    /**
     * 재고 가격 업데이트
     */
    public StockResponse updateStockPrice(Long id, UpdateStockPriceRequest request) {
        Stock stock = stocks.get(id);
        if (stock == null) {
            throw new StockNotFoundException("ID " + id + "에 해당하는 재고를 찾을 수 없습니다");
        }
        // 깊은 가격 분석 수행
        priceAnalyzer.analyzePriceChange(stock.getCurrentPrice(), request.getNewPrice());
        double oldPrice = stock.getCurrentPrice();
        stock.setCurrentPrice(request.getNewPrice());
        stock.setPreviousPrice(oldPrice);
        stock.setUpdatedAt(LocalDateTime.now());
        return StockResponse.from(stock);
    }
    
    /**
     * 재고 삭제
     */
    public boolean deleteStock(Long id) {
        return stocks.remove(id) != null;
    }
    
    /**
     * 재고 검색 (회사명 또는 심볼로 검색)
     */
    public List<StockResponse> searchStocks(String keyword) {
        return stocks.values().stream()
                .filter(s -> s.getCompanyName().contains(keyword) || s.getSymbol().contains(keyword))
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 가격 변동률이 높은 재고 조회 (상위 10개)
     */
    public List<StockResponse> getTopGainers() {
        return stocks.values().stream()
                .sorted((a, b) -> Double.compare(
                        (b.getCurrentPrice() - b.getPreviousPrice()) / b.getPreviousPrice(),
                        (a.getCurrentPrice() - a.getPreviousPrice()) / a.getPreviousPrice()
                ))
                .limit(10)
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 시가총액 기준 상위 재고 조회
     */
    public List<StockResponse> getTopByMarketCap() {
        return stocks.values().stream()
                .sorted((a, b) -> Long.compare(b.getMarketCap(), a.getMarketCap()))
                .limit(10)
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }
} 