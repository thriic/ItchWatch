package com.thriic.core.network.model

import com.google.gson.annotations.SerializedName

data class ProductApiModel(
    @SerializedName("@type") val type: String,
    val aggregateRating: RatingApiModel?,
    val description: String,
    val name: String,
    @SerializedName("@context") val context: String
)

//TODO 评分不存在时解析
data class RatingApiModel(
    @SerializedName("ratingValue") val ratingValue: String,
    @SerializedName("@type") val type: String,
    val ratingCount: Int
)