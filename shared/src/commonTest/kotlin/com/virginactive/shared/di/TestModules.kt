package com.virginactive.shared.di

import com.virginactive.shared.domain.store.TokenStore
import com.virginactive.shared.fake.InMemoryTokenStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import org.koin.core.module.Module
import org.koin.dsl.module

fun testPlatformModule(engine: MockEngine = defaultMockEngine()): Module = module {
    single<HttpClientEngine> { engine }
    single<TokenStore> { InMemoryTokenStore() }
    single<PlatformApiHost> {
        object : PlatformApiHost {
            override val baseUrl: String = "http://test.local"
        }
    }
}

fun defaultMockEngine(): MockEngine = MockEngine { _ ->
    respond(
        content = "{}",
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    )
}
