package com.lagniappe.theblock.vehicle.data.remote

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType

/**
 * Offline demo transport, not an auction server. Only the inventory endpoint is implemented.
 * Reject other requests rather than accidentally attempting a real network connection.
 */
internal class DemoInventoryInterceptor(
    private val readInventory: () -> String = {
        DemoInventoryInterceptor::class.java.getResourceAsStream("/vehicles.json")
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?: throw IOException("Bundled vehicles.json is missing")
    },
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isInventory = request.method == "GET" &&
            request.url.toString() == DEMO_BASE_URL + "vehicles"
        val body = if (isInventory) readInventory() else """{"error":"Unsupported demo endpoint"}"""

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(if (isInventory) 200 else 404)
            .message(if (isInventory) "OK" else "Not Found")
            .body(body.toResponseBody("application/json; charset=utf-8".toMediaType()))
            .build()
    }

    companion object {
        // .invalid is reserved for names that must not resolve.
        const val DEMO_BASE_URL = "https://vehicle-demo.invalid/"
    }
}
