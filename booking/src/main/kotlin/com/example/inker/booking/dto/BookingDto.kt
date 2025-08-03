package com.example.inker.booking.dto

import com.example.inker.booking.entity.Booking
import com.example.inker.booking.entity.BookingStatus
import java.time.LocalDateTime

/**
 * 예약 생성 요청 DTO
 */
data class CreateBookingRequest(
    val userId: Long,
    val serviceName: String,
    val bookingDate: LocalDateTime,
    val durationMinutes: Int,
    val notes: String? = null
)

/**
 * 예약 수정 요청 DTO
 */
data class UpdateBookingRequest(
    val serviceName: String? = null,
    val bookingDate: LocalDateTime? = null,
    val durationMinutes: Int? = null,
    val notes: String? = null,
    val status: BookingStatus? = null
)

/**
 * 예약 상태 변경 요청 DTO
 */
data class UpdateBookingStatusRequest(
    val status: BookingStatus
)

/**
 * 예약 응답 DTO
 */
data class BookingResponse(
    val id: Long,
    val userId: Long,
    val serviceName: String,
    val bookingDate: LocalDateTime,
    val durationMinutes: Int,
    val notes: String?,
    val status: BookingStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun from(booking: Booking): BookingResponse {
            return BookingResponse(
                id = booking.id!!,
                userId = booking.userId,
                serviceName = booking.serviceName,
                bookingDate = booking.bookingDate,
                durationMinutes = booking.durationMinutes,
                notes = booking.notes,
                status = booking.status,
                createdAt = booking.createdAt,
                updatedAt = booking.updatedAt
            )
        }
    }
} 