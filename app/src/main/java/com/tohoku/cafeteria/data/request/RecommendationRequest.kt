package com.tohoku.cafeteria.data.request

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationRequest(
    val query: String
)