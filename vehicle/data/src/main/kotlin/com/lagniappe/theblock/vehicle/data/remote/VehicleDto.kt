package com.lagniappe.theblock.vehicle.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VehicleDto(
    val id: String,
    val vin: String,
    val year: Int,
    val make: String,
    val model: String,
    val trim: String,
    @SerialName("body_style") val bodyStyle: String,
    @SerialName("exterior_color") val exteriorColor: String,
    @SerialName("interior_color") val interiorColor: String,
    val engine: String,
    val transmission: String,
    val drivetrain: String,
    @SerialName("odometer_km") val odometerKm: Long,
    @SerialName("fuel_type") val fuelType: String,
    @SerialName("condition_grade") val conditionGrade: Double,
    @SerialName("condition_report") val conditionReport: String,
    @SerialName("damage_notes") val damageNotes: List<String>,
    @SerialName("title_status") val titleStatus: String,
    val province: String,
    val city: String,
    @SerialName("auction_start") val auctionStart: String,
    @SerialName("starting_bid") val startingBid: Long,
    @SerialName("reserve_price") val reservePrice: Long?,
    @SerialName("buy_now_price") val buyNowPrice: Long?,
    val images: List<String>,
    @SerialName("selling_dealership") val sellingDealership: String,
    val lot: String,
    @SerialName("current_bid") val currentBid: Long?,
    @SerialName("bid_count") val bidCount: Int,
)
