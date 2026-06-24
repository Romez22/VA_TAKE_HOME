package com.virginactive.shared.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.virginactive.shared.data.auth.TokenStoreAndroid
import com.virginactive.shared.domain.store.TokenStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<TokenStore> {
        TokenStoreAndroid(settings = buildEncryptedSettings(get()), json = get())
    }
    single<PlatformApiHost> {
        object : PlatformApiHost {
            override val baseUrl: String = "http://10.0.2.2:8080"
        }
    }
}

private fun buildEncryptedSettings(context: Context): Settings {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    val prefs = EncryptedSharedPreferences.create(
        context,
        TokenStoreAndroid.STORAGE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
    return SharedPreferencesSettings(prefs)
}
