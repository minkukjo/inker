package com.example.inker.user.service

import com.example.inker.user.dto.CreateUserRequest
import com.example.inker.user.dto.UpdateUserRequest
import com.example.inker.user.dto.UserResponse
import com.example.inker.user.entity.User
import com.example.inker.user.exception.UserNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 사용자 서비스
 */
@Service
class UserService {
    
    // 임시 저장소 (실제로는 Repository를 사용해야 함)
    private val users = mutableMapOf<Long, User>()
    private var nextId = 1L
    
    /**
     * 모든 사용자 조회
     */
    fun getAllUsers(): List<UserResponse> {
        return users.values.map { UserResponse.from(it) }
    }
    
    /**
     * ID로 사용자 조회
     */
    fun getUserById(id: Long): UserResponse {
        return users[id]?.let { UserResponse.from(it) } ?: throw UserNotFoundException()
    }
    
    /**
     * 사용자 생성
     */
    fun createUser(request: CreateUserRequest): UserResponse {
        val user = User(
            id = nextId,
            name = request.name,
            email = request.email,
            phone = request.phone
        )
        
        users[nextId] = user
        nextId++
        
        return UserResponse.from(user)
    }
    
    /**
     * 사용자 수정
     */
    fun updateUser(id: Long, request: UpdateUserRequest): UserResponse {
        val existingUser = users[id] ?: throw UserNotFoundException()
        
        val updatedUser = existingUser.copy(
            name = request.name ?: existingUser.name,
            email = request.email ?: existingUser.email,
            phone = request.phone ?: existingUser.phone,
            updatedAt = LocalDateTime.now()
        )
        
        users[id] = updatedUser
        return UserResponse.from(updatedUser)
    }
    
    /**
     * 사용자 삭제
     */
    fun deleteUser(id: Long): Boolean {
        return users.remove(id)?.let { true } ?: throw UserNotFoundException()
    }
} 