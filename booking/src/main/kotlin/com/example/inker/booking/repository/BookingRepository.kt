package com.example.inker.booking.repository

import com.example.inker.booking.entity.Booking
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class BookingRepository {

    // 임시 저장소 (실제로는 Repository를 사용해야 함)
    private val bookings = mutableMapOf<Long, Booking>()

    fun findAll(): List<Booking> = bookings.values.toList()

    fun findById(id: Long): Optional<Booking> = bookings[id]?.let { Optional.of(it) } ?: Optional.empty()

    fun save(booking: Booking): Booking {
        bookings[booking.id!!] = booking
        return bookings[booking.id!!]!!
    }

    fun delete(id: Long) = bookings.remove(id)

}