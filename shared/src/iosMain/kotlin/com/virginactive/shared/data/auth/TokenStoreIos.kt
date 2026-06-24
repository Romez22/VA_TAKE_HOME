package com.virginactive.shared.data.auth

import com.russhwolf.settings.Settings
import com.virginactive.shared.data.remote.appJson
import com.virginactive.shared.domain.store.Session
import com.virginactive.shared.domain.store.TokenStore
import kotlinx.serialization.json.Json

internal class TokenStoreIos(
    private val settings: Settings,
    private val json: Json = appJson,
) : TokenStore {

    override suspend fun session(): Session? =
        settings.getStringOrNull(KEY_SESSION)
            ?.let { json.decodeFromString(SessionDto.serializer(), it).toDomain() }

    override suspend fun save(session: Session) {
        settings.putString(KEY_SESSION, json.encodeToString(SessionDto.serializer(), session.toDto()))
    }

    override suspend fun clear() {
        settings.remove(KEY_SESSION)
    }

    companion object {
        const val SERVICE_NAME: String = "com.virginactive.tokens"

        const val KEY_SESSION: String = "session"
    }
}
