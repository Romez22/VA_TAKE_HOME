package com.virginactive.shared.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ApiErrorDto(
    val error: String,
    val message: String,
    val code: String? = null,
    val requestId: String,
)
