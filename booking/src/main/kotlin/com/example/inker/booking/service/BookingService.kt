package com.example.inker.booking.service

import com.example.inker.booking.dto.CreateBookingRequest
import com.example.inker.booking.dto.UpdateBookingRequest
import com.example.inker.booking.dto.UpdateBookingStatusRequest
import com.example.inker.booking.dto.BookingResponse
import com.example.inker.booking.entity.Booking
import com.example.inker.booking.entity.BookingStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 예약 서비스
 */
@Service
class BookingService {
    
    // 임시 저장소 (실제로는 Repository를 사용해야 함)
    private val bookings = mutableMapOf<Long, Booking>()
    private var nextId = 1L
    
    /**
     * 모든 예약 조회
     */
    fun getAllBookings(): List<BookingResponse> {
        return bookings.values.map { BookingResponse.from(it) }
    }
    
    /**
     * ID로 예약 조회
     */
    fun getBookingById(id: Long): BookingResponse? {
        return bookings[id]?.let { BookingResponse.from(it) }
    }
    
    /**
     * 사용자 ID로 예약 조회
     */
    fun getBookingsByUserId(userId: Long): List<BookingResponse> {
        return bookings.values
            .filter { it.userId == userId }
            .map { BookingResponse.from(it) }
    }
    
    /**
     * 예약 생성
     */
    fun createBooking(request: CreateBookingRequest): BookingResponse {
        val booking = Booking(
            id = nextId,
            userId = request.userId,
            serviceName = request.serviceName,
            bookingDate = request.bookingDate,
            durationMinutes = request.durationMinutes,
            notes = request.notes
        )
        
        bookings[nextId] = booking
        nextId++
        
        return BookingResponse.from(booking)
    }
    
    /**
     * 예약 수정
     */
    fun updateBooking(id: Long, request: UpdateBookingRequest): BookingResponse? {
        val existingBooking = bookings[id] ?: return null
        
        val updatedBooking = existingBooking.copy(
            serviceName = request.serviceName ?: existingBooking.serviceName,
            bookingDate = request.bookingDate ?: existingBooking.bookingDate,
            durationMinutes = request.durationMinutes ?: existingBooking.durationMinutes,
            notes = request.notes ?: existingBooking.notes,
            status = request.status ?: existingBooking.status,
            updatedAt = LocalDateTime.now()
        )
        
        bookings[id] = updatedBooking
        return BookingResponse.from(updatedBooking)
    }
    
    /**
     * 예약 상태 변경
     */
    fun updateBookingStatus(id: Long, request: UpdateBookingStatusRequest): BookingResponse? {
        val existingBooking = bookings[id] ?: return null
        
        val updatedBooking = existingBooking.copy(
            status = request.status,
            updatedAt = LocalDateTime.now()
        )
        
        bookings[id] = updatedBooking
        return BookingResponse.from(updatedBooking)
    }
    
    /**
     * 예약 삭제
     */
    fun deleteBooking(id: Long): Boolean {
        return bookings.remove(id) != null
    }
    
    /**
     * 예약 취소
     */
    fun cancelBooking(id: Long): BookingResponse? {
        return updateBookingStatus(id, UpdateBookingStatusRequest(BookingStatus.CANCELLED))
    }
} 