package com.example.inker.booking.entity

import java.time.LocalDateTime

/**
 * 예약 엔티티
 */
data class Booking(
    val id: Long? = null,
    val userId: Long,
    val serviceName: String,
    val bookingDate: LocalDateTime,
    val durationMinutes: Int,
    val notes: String? = null,
    val status: BookingStatus = BookingStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null
)

/**
 * 예약 상태 열거형
 */
enum class BookingStatus {
    PENDING,    // 대기중
    CONFIRMED,  // 확정
    CANCELLED,  // 취소
    COMPLETED   // 완료
} 