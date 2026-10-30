package com.lagniappe.theblock

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.lagniappe.theblock.vehicle.domain.repository.VehicleRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import okhttp3.OkHttpClient

@HiltAndroidApp
class TheBlockApplication : Application(), SingletonImageLoader.Factory {
    @Inject
    lateinit var vehicleRepository: VehicleRepository

    override fun newImageLoader(context: Context): ImageLoader {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                // Wikimedia rejects generic User-Agents such as okhttp/4.12.0.
                val request = chain.request().newBuilder()
                    .header("User-Agent", IMAGE_USER_AGENT)
                    .build()
                chain.proceed(request)
            }
            .build()
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { client }))
            }
            .build()
    }
}

private const val IMAGE_USER_AGENT =
    "TheBlockDemoInventory/1.0 (https://github.com/ferrolb/the-block)"
