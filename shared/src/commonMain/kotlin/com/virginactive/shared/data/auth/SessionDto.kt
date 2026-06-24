package com.virginactive.shared.data.auth

import com.virginactive.shared.domain.store.Session
import kotlinx.serialization.Serializable

@Serializable
internal data class SessionDto(
    val accessToken: String,
    val refreshToken: String,
)

internal fun Session.toDto(): SessionDto =
    SessionDto(accessToken = accessToken, refreshToken = refreshToken)

internal fun SessionDto.toDomain(): Session =
    Session(accessToken = accessToken, refreshToken = refreshToken)
