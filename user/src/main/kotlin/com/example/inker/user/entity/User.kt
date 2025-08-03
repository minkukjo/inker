package com.example.inker.user.entity

import jakarta.persistence.*

/**
 * 사용자 엔티티
 */
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 100)
    val name: String,
    
    @Column(nullable = false, unique = true, length = 100)
    val email: String,
    
    @Column(length = 20)
    val phone: String? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    
    @Column(name = "updated_at")
    val updatedAt: java.time.LocalDateTime? = null
) 