package com.example.inker.booking.controller

import com.example.inker.booking.dto.CreateBookingRequest
import com.example.inker.booking.dto.UpdateBookingRequest
import com.example.inker.booking.dto.UpdateBookingStatusRequest
import com.example.inker.booking.dto.BookingResponse
import com.example.inker.booking.service.BookingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 예약 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/bookings")
class BookingController(
    private val bookingService: BookingService
) {

    /**
     * 모든 예약 조회
     */
    @GetMapping
    fun getAllBookings(): ResponseEntity<List<BookingResponse>> {
        val bookings = bookingService.getAllBookings()
        return ResponseEntity.ok(bookings)
    }

    /**
     * ID로 예약 조회
     */
    @GetMapping("/{id}")
    fun getBookingById(@PathVariable id: Long): ResponseEntity<BookingResponse> {
        val booking = bookingService.getBookingById(id)
        return if (booking != null) {
            ResponseEntity.ok(booking)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 사용자 ID로 예약 조회
     */
    @GetMapping("/user/{userId}")
    fun getBookingsByUserId(@PathVariable userId: Long): ResponseEntity<List<BookingResponse>> {
        val bookings = bookingService.getBookingsByUserId(userId)
        return ResponseEntity.ok(bookings)
    }

    /**
     * 예약 생성
     */
    @PostMapping
    fun createBooking(@RequestBody request: CreateBookingRequest): ResponseEntity<BookingResponse> {
        val createdBooking = bookingService.createBooking(request)
        return ResponseEntity.status(201).body(createdBooking)
    }

    /**
     * 예약 수정
     */
    @PutMapping("/{id}")
    fun updateBooking(
        @PathVariable id: Long,
        @RequestBody request: UpdateBookingRequest
    ): ResponseEntity<BookingResponse> {
        val updatedBooking = bookingService.updateBooking(id, request)
        return if (updatedBooking != null) {
            ResponseEntity.ok(updatedBooking)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 예약 상태 변경
     */
    @PatchMapping("/{id}/status")
    fun updateBookingStatus(
        @PathVariable id: Long,
        @RequestBody request: UpdateBookingStatusRequest
    ): ResponseEntity<BookingResponse> {
        val updatedBooking = bookingService.updateBookingStatus(id, request)
        return if (updatedBooking != null) {
            ResponseEntity.ok(updatedBooking)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 예약 취소
     */
    @PostMapping("/{id}/cancel")
    fun cancelBooking(@PathVariable id: Long): ResponseEntity<BookingResponse> {
        val cancelledBooking = bookingService.cancelBooking(id)
        return if (cancelledBooking != null) {
            ResponseEntity.ok(cancelledBooking)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 예약 삭제
     */
    @DeleteMapping("/{id}")
    fun deleteBooking(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = bookingService.deleteBooking(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
} 