package com.virginactive.shared.data.home

import com.virginactive.shared.data.home.dto.ManifestBlockDto
import com.virginactive.shared.data.home.dto.UnknownBlockDto
import com.virginactive.shared.data.home.dto.toDomain
import com.virginactive.shared.data.remote.appJson
import com.virginactive.shared.domain.home.HomeManifest
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

internal fun decodeManifest(rawBody: String): HomeManifest {
    val blocksArray = runCatching {
        appJson.parseToJsonElement(rawBody).jsonObject["blocks"]?.jsonArray
    }.getOrNull() ?: return HomeManifest(blocks = emptyList())

    val dtos = blocksArray.map { element ->
        runCatching {
            appJson.decodeFromJsonElement(ManifestBlockDto.serializer(), element)
        }.getOrDefault(UnknownBlockDto)
    }

    return HomeManifest(blocks = dtos.map { it.toDomain() })
}
