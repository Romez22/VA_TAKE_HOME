package com.virginactive.shared.data.auth.dto

import kotlinx.serialization.Serializable


@Serializable
internal data class LoginRequestDto(
    val username: String,
    val password: String,
)

@Serializable
internal data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val user: UserProfileDto,
)

@Serializable
internal data class UserProfileDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val membershipTier: String,
    val homeClub: ClubSummaryDto,
)

@Serializable
internal data class ClubSummaryDto(
    val id: String,
    val name: String,
)

@Serializable
internal data class RefreshRequestDto(
    val refreshToken: String,
)

@Serializable
internal data class RefreshResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
)
