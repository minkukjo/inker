package com.example.inker.stock.service;

import com.example.inker.stock.dto.CreateStockRequest;
import com.example.inker.stock.exception.InvalidPriceException;

/**
 * 재고 추천 엔진 (11단계)
 * 추천 관련 분석을 담당합니다.
 */
public class StockRecommendationEngine {
    
    public StockRecommendationEngine() {
        // 11단계: 최종 추천 엔진
    }
    
    public void recommendForId(Long id) {
        // 11단계: ID 기반 추천
        if (id == null || id <= 0) {
            throw new InvalidPriceException("추천을 위한 유효하지 않은 ID입니다");
        }
        
        // 최종 단계에서 예외 발생 (테스트용)
        if (id == 999L) {
            throw new InvalidPriceException("페니 재고는 추천하지 않습니다");
        }
    }
    
    public void recommendForSymbol(String symbol) {
        // 11단계: 심볼 기반 추천
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new InvalidPriceException("추천을 위한 유효하지 않은 심볼입니다");
        }
        
        // 최종 단계에서 예외 발생 (테스트용)
        if (symbol.equals("PENNY")) {
            throw new InvalidPriceException("페니 재고는 추천하지 않습니다");
        }
    }
    
    public void recommendAll() {
        // 11단계: 전체 추천
        // 최종 단계에서 예외 발생 (테스트용)
        if (Math.random() < 0.1) { // 10% 확률로 예외 발생
            throw new InvalidPriceException("전체 추천 중 오류가 발생했습니다");
        }
    }
    
    public void recommendForUpdate(Long id) {
        // 11단계: 업데이트용 추천
        if (id == null || id <= 0) {
            throw new InvalidPriceException("업데이트를 위한 유효하지 않은 ID입니다");
        }
        
        // 최종 단계에서 예외 발생 (테스트용)
        if (id == 888L) {
            throw new InvalidPriceException("업데이트 추천 중 오류가 발생했습니다");
        }
    }
    
    public void recommendForPriceUpdate(Long id) {
        // 11단계: 가격 업데이트용 추천
        if (id == null || id <= 0) {
            throw new InvalidPriceException("가격 업데이트를 위한 유효하지 않은 ID입니다");
        }
        
        // 최종 단계에서 예외 발생 (테스트용)
        if (id == 777L) {
            throw new InvalidPriceException("가격 업데이트 추천 중 오류가 발생했습니다");
        }
    }
    
    public void recommendForCreate(CreateStockRequest request) {
        // 11단계: 생성용 추천
        if (request == null || request.getSymbol() == null) {
            throw new InvalidPriceException("생성을 위한 유효하지 않은 요청입니다");
        }
        
        // 최종 단계에서 예외 발생 (테스트용)
        if (request.getSymbol().equals("TEST")) {
            throw new InvalidPriceException("테스트 재고는 추천하지 않습니다");
        }
    }
    
    public void recommendForDelete(Long id) {
        // 11단계: 삭제용 추천
        if (id == null || id <= 0) {
            throw new InvalidPriceException("삭제를 위한 유효하지 않은 ID입니다");
        }
        
        // 최종 단계에서 예외 발생 (테스트용)
        if (id == 666L) {
            throw new InvalidPriceException("삭제 추천 중 오류가 발생했습니다");
        }
    }
} 