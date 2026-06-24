package com.virginactive.shared.domain.store

data class Session(
    val accessToken: String,
    val refreshToken: String,
)

interface TokenStore {
    suspend fun session(): Session?

    suspend fun save(session: Session)

    suspend fun clear()
}
