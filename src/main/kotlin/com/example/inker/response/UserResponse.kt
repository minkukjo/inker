package com.example.inker.response

/**
 * 유저 정보를 나타내는 데이터 클래스
 * @property id 유저의 고유 ID
 * @property name 유저의 이름
 */
data class UserResponse(
    val id: Long,
    val name: String
)
