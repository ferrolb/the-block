package com.lagniappe.theblock.vehicle.data

import com.lagniappe.theblock.vehicle.data.remote.DemoInventoryInterceptor
import com.lagniappe.theblock.vehicle.data.remote.VehicleApi
import com.lagniappe.theblock.vehicle.data.repository.DefaultVehicleRepository
import com.lagniappe.theblock.vehicle.domain.repository.VehicleRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Creates an offline demo repository. The caller must retain and share the returned instance. */
object DemoVehicleRepositoryFactory {
    fun create(): VehicleRepository = DefaultVehicleRepository(createApi())

    internal fun createApi(
        interceptor: DemoInventoryInterceptor = DemoInventoryInterceptor(),
    ): VehicleApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(DemoInventoryInterceptor.DEMO_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(VehicleApi::class.java)
    }
}
