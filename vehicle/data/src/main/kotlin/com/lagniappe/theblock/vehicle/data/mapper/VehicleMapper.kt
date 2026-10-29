package com.lagniappe.theblock.vehicle.data.mapper

import com.lagniappe.theblock.vehicle.data.remote.VehicleDto
import com.lagniappe.theblock.vehicle.domain.model.Dealership
import com.lagniappe.theblock.vehicle.domain.model.Money
import com.lagniappe.theblock.vehicle.domain.model.Vehicle
import com.lagniappe.theblock.vehicle.domain.model.VehicleAuction
import com.lagniappe.theblock.vehicle.domain.model.VehicleCondition
import com.lagniappe.theblock.vehicle.domain.model.VehicleSpecs

internal fun VehicleDto.toDomain(): Vehicle {
    require(id.isNotBlank()) { "Vehicle ID is missing" }
    require(odometerKm >= 0 && bidCount >= 0) { "Invalid mileage or bid count" }
    require(conditionGrade in 0.0..5.0) { "Invalid condition grade" }
    require((currentBid == null) == (bidCount == 0)) { "Inconsistent bid state" }

    return Vehicle(
        id = id,
        vin = vin,
        year = year,
        make = make,
        model = model,
        trim = trim,
        lot = lot,
        specs = VehicleSpecs(
            bodyStyle = bodyStyle,
            exteriorColor = exteriorColor,
            interiorColor = interiorColor,
            engine = engine,
            transmission = transmission,
            drivetrain = drivetrain,
            odometerKm = odometerKm,
            fuelType = fuelType,
        ),
        condition = VehicleCondition(
            grade = conditionGrade,
            report = conditionReport,
            damageNotes = damageNotes.toList(),
            titleStatus = titleStatus,
        ),
        dealership = Dealership(
            name = sellingDealership,
            city = city,
            province = province,
        ),
        auction = VehicleAuction(
            scheduledStart = auctionStart,
            startingBid = startingBid.toMoney(),
            reservePrice = reservePrice?.toMoney(),
            buyNowPrice = buyNowPrice?.toMoney(),
            currentBid = currentBid?.toMoney(),
            bidCount = bidCount,
        ),
        images = images.toList(),
    )
}

// Fixture prices are whole CAD dollars, while domain prices are always cents.
private fun Long.toMoney(): Money = Money(Math.multiplyExact(this, 100L))
