package com.example.newsfinance.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MarketChartDto(
    @SerializedName("prices") val prices: List<List<Double>>
)
