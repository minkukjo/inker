package com.example.inker.user.dto

import com.example.inker.user.entity.User
import java.time.LocalDateTime

/**
 * 사용자 생성 요청 DTO
 */
data class CreateUserRequest(
    val name: String,
    val email: String,
    val phone: String? = null
)

/**
 * 사용자 수정 요청 DTO
 */
data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null
)

/**
 * 사용자 응답 DTO
 */
data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                name = user.name,
                email = user.email,
                phone = user.phone,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
} 