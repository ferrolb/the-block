package com.lagniappe.theblock.vehicle.data.remote

import retrofit2.http.GET

internal interface VehicleApi {
    @GET("vehicles")
    suspend fun getVehicles(): List<VehicleDto>
}
