package com.example.inker.booking.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 예약 엔티티
 */
@Entity
@Table(name = "bookings")
data class Booking(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    
    @Column(name = "service_name", nullable = false, length = 100)
    val serviceName: String,
    
    @Column(name = "booking_date", nullable = false)
    val bookingDate: LocalDateTime,
    
    @Column(name = "duration_minutes", nullable = false)
    val durationMinutes: Int,
    
    @Column(length = 500)
    val notes: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: BookingStatus = BookingStatus.PENDING,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
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