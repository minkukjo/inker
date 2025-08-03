package com.example.inker.user.entity

import java.time.LocalDateTime

/**
 * 사용자 엔티티
 */
data class User(
    val id: Long? = null,
    val name: String,
    val email: String,
    val phone: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = null
) 