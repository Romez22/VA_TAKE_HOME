package com.virginactive.shared.fake

import com.virginactive.shared.domain.store.Session
import com.virginactive.shared.domain.store.TokenStore

class InMemoryTokenStore(initial: Session? = null) : TokenStore {

    private var current: Session? = initial

    var saveCount: Int = 0
        private set

    var clearCount: Int = 0
        private set

    val stored: Session? get() = current

    override suspend fun session(): Session? = current

    override suspend fun save(session: Session) {
        current = session
        saveCount++
    }

    override suspend fun clear() {
        current = null
        clearCount++
    }
}
