package com.tohoku.cafeteria.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RatingQuery(
    @SerialName("variant_id")
    val variantId: Int,
    val rating: String
)
