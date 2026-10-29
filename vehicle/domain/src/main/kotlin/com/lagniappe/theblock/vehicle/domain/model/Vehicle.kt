package com.lagniappe.theblock.vehicle.domain.model

data class Vehicle(
    val id: String,
    val vin: String,
    val year: Int,
    val make: String,
    val model: String,
    val trim: String,
    val lot: String,
    val specs: VehicleSpecs,
    val condition: VehicleCondition,
    val dealership: Dealership,
    val auction: VehicleAuction,
    val images: List<String>,
)

data class VehicleSpecs(
    val bodyStyle: String,
    val exteriorColor: String,
    val interiorColor: String,
    val engine: String,
    val transmission: String,
    val drivetrain: String,
    val odometerKm: Long,
    val fuelType: String,
)

data class VehicleCondition(
    val grade: Double,
    val report: String,
    val damageNotes: List<String>,
    val titleStatus: String,
)

data class Dealership(
    val name: String,
    val city: String,
    val province: String,
)

data class VehicleAuction(
    // Original local ISO timestamp. The fixture supplies neither a timezone nor an end time.
    val scheduledStart: String,
    val startingBid: Money,
    val reservePrice: Money?,
    val buyNowPrice: Money?,
    val currentBid: Money?,
    val bidCount: Int,
)
