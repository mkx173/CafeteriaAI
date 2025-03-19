package com.tohoku.cafeteria.data.response

import kotlinx.serialization.Serializable

@Serializable
data class DetectResponse(
    val response: Map<String, Float>
)
