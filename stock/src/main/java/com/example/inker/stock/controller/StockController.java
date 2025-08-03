package com.example.inker.stock.controller;

import com.example.inker.stock.dto.CreateStockRequest;
import com.example.inker.stock.dto.StockResponse;
import com.example.inker.stock.dto.UpdateStockPriceRequest;
import com.example.inker.stock.dto.UpdateStockRequest;
import com.example.inker.stock.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 주식 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/stocks")
public class StockController {
    
    private final StockService stockService;
    
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }
    
    /**
     * 모든 주식 조회
     */
    @GetMapping
    public ResponseEntity<List<StockResponse>> getAllStocks() {
        List<StockResponse> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }
    
    /**
     * ID로 주식 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<StockResponse> getStockById(@PathVariable Long id) {
        StockResponse stock = stockService.getStockById(id);
        return stock != null ? ResponseEntity.ok(stock) : ResponseEntity.notFound().build();
    }
    
    /**
     * 심볼로 주식 조회
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<StockResponse> getStockBySymbol(@PathVariable String symbol) {
        StockResponse stock = stockService.getStockBySymbol(symbol);
        return stock != null ? ResponseEntity.ok(stock) : ResponseEntity.notFound().build();
    }
    
    /**
     * 섹터별 주식 조회
     */
    @GetMapping("/sector/{sector}")
    public ResponseEntity<List<StockResponse>> getStocksBySector(@PathVariable String sector) {
        List<StockResponse> stocks = stockService.getStocksBySector(sector);
        return ResponseEntity.ok(stocks);
    }
    
    /**
     * 주식 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<StockResponse>> searchStocks(@RequestParam String keyword) {
        List<StockResponse> stocks = stockService.searchStocks(keyword);
        return ResponseEntity.ok(stocks);
    }
    
    /**
     * 상위 상승주 조회
     */
    @GetMapping("/top-gainers")
    public ResponseEntity<List<StockResponse>> getTopGainers() {
        List<StockResponse> stocks = stockService.getTopGainers();
        return ResponseEntity.ok(stocks);
    }
    
    /**
     * 시가총액 상위 주식 조회
     */
    @GetMapping("/top-market-cap")
    public ResponseEntity<List<StockResponse>> getTopByMarketCap() {
        List<StockResponse> stocks = stockService.getTopByMarketCap();
        return ResponseEntity.ok(stocks);
    }
    
    /**
     * 주식 생성
     */
    @PostMapping
    public ResponseEntity<StockResponse> createStock(@RequestBody CreateStockRequest request) {
        StockResponse createdStock = stockService.createStock(request);
        return ResponseEntity.status(201).body(createdStock);
    }
    
    /**
     * 주식 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<StockResponse> updateStock(
            @PathVariable Long id,
            @RequestBody UpdateStockRequest request) {
        StockResponse updatedStock = stockService.updateStock(id, request);
        return updatedStock != null ? ResponseEntity.ok(updatedStock) : ResponseEntity.notFound().build();
    }
    
    /**
     * 주식 가격 업데이트
     */
    @PatchMapping("/{id}/price")
    public ResponseEntity<StockResponse> updateStockPrice(
            @PathVariable Long id,
            @RequestBody UpdateStockPriceRequest request) {
        StockResponse updatedStock = stockService.updateStockPrice(id, request);
        return updatedStock != null ? ResponseEntity.ok(updatedStock) : ResponseEntity.notFound().build();
    }
    
    /**
     * 주식 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        boolean deleted = stockService.deleteStock(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
} 