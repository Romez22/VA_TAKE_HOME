package com.virginactive.shared.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JsonConfigTest {

    @Serializable
    private enum class Tier { @SerialName("essential") ESSENTIAL, @SerialName("premium") PREMIUM }

    @Serializable
    private data class Sample(
        val name: String,
        val tier: Tier = Tier.ESSENTIAL,
        val nickname: String? = null,
    )

    @Test
    fun ignoresUnknownKeys() {
        val json = """{"name":"Sizwe","tier":"premium","futureField":"ignored","another":42}"""
        val decoded = appJson.decodeFromString(Sample.serializer(), json)
        assertEquals("Sizwe", decoded.name)
        assertEquals(Tier.PREMIUM, decoded.tier)
    }

    @Test
    fun coercesUnknownNonListEnumToDefault() {
        val json = """{"name":"Sizwe","tier":"club"}"""
        val decoded = appJson.decodeFromString(Sample.serializer(), json)
        assertEquals(Tier.ESSENTIAL, decoded.tier)
    }

    @Test
    fun missingNullableFieldDecodesToNull() {
        val json = """{"name":"Sizwe","tier":"premium"}"""
        val decoded = appJson.decodeFromString(Sample.serializer(), json)
        assertNull(decoded.nickname)
    }
}
