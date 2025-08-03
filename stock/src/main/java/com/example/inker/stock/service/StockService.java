package com.example.inker.stock.service;

import com.example.inker.stock.dto.CreateStockRequest;
import com.example.inker.stock.dto.UpdateStockRequest;
import com.example.inker.stock.dto.UpdateStockPriceRequest;
import com.example.inker.stock.dto.StockResponse;
import com.example.inker.stock.entity.Stock;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 주식 관련 서비스
 */
@Service
public class StockService {
    
    // 메모리 기반 저장소 (실제로는 데이터베이스를 사용)
    private final Map<Long, Stock> stocks = new ConcurrentHashMap<>();
    private long nextId = 1;
    
    /**
     * 모든 주식 조회
     */
    public List<StockResponse> getAllStocks() {
        return stocks.values().stream()
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * ID로 주식 조회
     */
    public StockResponse getStockById(Long id) {
        Stock stock = stocks.get(id);
        return stock != null ? StockResponse.from(stock) : null;
    }
    
    /**
     * 심볼로 주식 조회
     */
    public StockResponse getStockBySymbol(String symbol) {
        Stock stock = stocks.values().stream()
                .filter(s -> s.getSymbol().equals(symbol))
                .findFirst()
                .orElse(null);
        return stock != null ? StockResponse.from(stock) : null;
    }
    
    /**
     * 섹터별 주식 조회
     */
    public List<StockResponse> getStocksBySector(String sector) {
        return stocks.values().stream()
                .filter(s -> s.getSector().equals(sector))
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 주식 생성
     */
    public StockResponse createStock(CreateStockRequest request) {
        Stock stock = new Stock(
            request.getSymbol(),
            request.getCompanyName(),
            request.getCurrentPrice(),
            request.getPreviousPrice(),
            request.getVolume(),
            request.getMarketCap(),
            request.getSector()
        );
        
        stock.setId(nextId++);
        stock.setCreatedAt(LocalDateTime.now());
        
        stocks.put(stock.getId(), stock);
        return StockResponse.from(stock);
    }
    
    /**
     * 주식 수정
     */
    public StockResponse updateStock(Long id, UpdateStockRequest request) {
        Stock stock = stocks.get(id);
        if (stock == null) {
            return null;
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
        stocks.put(id, stock);
        
        return StockResponse.from(stock);
    }
    
    /**
     * 주식 가격 업데이트
     */
    public StockResponse updateStockPrice(Long id, UpdateStockPriceRequest request) {
        Stock stock = stocks.get(id);
        if (stock == null) {
            return null;
        }
        
        stock.setPreviousPrice(stock.getCurrentPrice());
        stock.setCurrentPrice(request.getCurrentPrice());
        stock.setVolume(request.getVolume());
        stock.setUpdatedAt(LocalDateTime.now());
        
        stocks.put(id, stock);
        return StockResponse.from(stock);
    }
    
    /**
     * 주식 삭제
     */
    public boolean deleteStock(Long id) {
        return stocks.remove(id) != null;
    }
    
    /**
     * 주식 검색 (회사명 또는 심볼로 검색)
     */
    public List<StockResponse> searchStocks(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return stocks.values().stream()
                .filter(s -> s.getCompanyName().toLowerCase().contains(lowerKeyword) ||
                           s.getSymbol().toLowerCase().contains(lowerKeyword))
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 가격 변동률이 높은 주식 조회 (상위 10개)
     */
    public List<StockResponse> getTopGainers() {
        return stocks.values().stream()
                .filter(s -> s.getPreviousPrice() > 0)
                .sorted((s1, s2) -> {
                    double change1 = (s1.getCurrentPrice() - s1.getPreviousPrice()) / s1.getPreviousPrice();
                    double change2 = (s2.getCurrentPrice() - s2.getPreviousPrice()) / s2.getPreviousPrice();
                    return Double.compare(change2, change1); // 내림차순
                })
                .limit(10)
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 시가총액 기준 상위 주식 조회
     */
    public List<StockResponse> getTopByMarketCap() {
        return stocks.values().stream()
                .sorted((s1, s2) -> Double.compare(s2.getMarketCap(), s1.getMarketCap()))
                .limit(10)
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }
} 