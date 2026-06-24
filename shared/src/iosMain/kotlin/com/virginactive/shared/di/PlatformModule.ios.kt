package com.virginactive.shared.di

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.virginactive.shared.data.auth.TokenStoreIos
import com.virginactive.shared.domain.store.TokenStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrService

@OptIn(
    ExperimentalSettingsImplementation::class,
    ExperimentalSettingsApi::class,
    ExperimentalForeignApi::class,
)
actual fun platformModule(): Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single<TokenStore> {
        val serviceRef = CFStringCreateWithCString(
            null,
            TokenStoreIos.SERVICE_NAME,
            kCFStringEncodingUTF8,
        )
        val settings = KeychainSettings(
            kSecAttrService to serviceRef,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
        )
        TokenStoreIos(settings = settings, json = get())
    }
    single<PlatformApiHost> {
        object : PlatformApiHost {
            override val baseUrl: String = "http://localhost:8080"
        }
    }
}
