package com.example.inker.controller

import com.example.inker.response.UserResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 유저 관련 API를 제공하는 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/users")
class UserController {

    /**
     * 특정 유저의 정보를 조회합니다.
     * @param userId 유저의 ID
     * @return UserDTO 유저 정보
     */
    @GetMapping("/{userId}")
    fun getUserById(@PathVariable userId: Long): ResponseEntity<UserResponse> {
        val user = UserResponse(userId, "Test User")
        return ResponseEntity.ok(user)
    }

    /**
     * 새로운 유저를 생성합니다.
     * @param userResponse 생성할 유저의 정보
     * @return 생성된 유저 정보
     */
    @PostMapping
    fun createUser(@RequestBody userResponse: UserResponse): ResponseEntity<UserResponse> {
        // 실제로는 서비스를 호출하여 유저를 생성하는 로직이 들어갑니다.
        return ResponseEntity.status(201).body(userResponse)
    }

    /**
     * 유저 정보를 업데이트합니다.
     * @param userId 업데이트할 유저의 ID
     * @param userResponse 업데이트할 유저 정보
     * @return 업데이트된 유저 정보
     */
    @PutMapping("/{userId}")
    fun updateUser(
        @PathVariable userId: Long,
        @RequestBody userResponse: UserResponse
    ): ResponseEntity<UserResponse> {
        val updatedUser = userResponse.copy(id = userId)
        return ResponseEntity.ok(updatedUser)
    }

    /**
     * 특정 유저를 삭제합니다.
     * @param userId 삭제할 유저의 ID
     */
    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<Void> {
        return ResponseEntity.noContent().build()
    }

    /**
     * 모든 유저 목록을 조회합니다.
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 당 아이템 수
     * @return 유저 정보 리스트
     */
    @GetMapping
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<List<UserResponse>> {
        val users = listOf(
            UserResponse(1, "User 1"),
            UserResponse(2, "User 2")
        )
        return ResponseEntity.ok(users)
    }
}
