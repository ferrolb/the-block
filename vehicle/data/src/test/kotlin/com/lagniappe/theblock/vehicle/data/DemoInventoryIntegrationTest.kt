package com.lagniappe.theblock.vehicle.data

import com.lagniappe.theblock.vehicle.data.remote.DemoInventoryInterceptor
import com.lagniappe.theblock.vehicle.data.repository.DefaultVehicleRepository
import com.lagniappe.theblock.vehicle.domain.model.Money
import com.lagniappe.theblock.vehicle.domain.repository.InventoryFailure
import com.lagniappe.theblock.vehicle.domain.repository.VehicleInventory
import java.io.IOException
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoInventoryIntegrationTest {
    @Test
    fun `bundled inventory travels through OkHttp Retrofit and domain mapping`() = runTest {
        val repository = DemoVehicleRepositoryFactory.create()
        repository.loadInventory()
        val vehicles = (repository.inventory.value as VehicleInventory.Ready).vehicles

        assertEquals(200, vehicles.size)
        assertEquals(200, vehicles.map { it.id }.distinct().size)
        val first = vehicles.first()
        assertEquals("4e3cd74f-bb88-4efe-b234-bcb2f7474b40", first.id)
        assertEquals("Mazda", first.make)
        assertEquals("A-0001", first.lot)
        assertEquals(24_534L, first.specs.odometerKm)
        assertEquals("2.5L I4", first.specs.engine)
        assertEquals(4.0, first.condition.grade, 0.0)
        assertTrue(first.condition.damageNotes.isEmpty())
        assertEquals("Highway 7 Auto Sales", first.dealership.name)
        assertEquals("Mississauga", first.dealership.city)
        assertEquals("2026-04-05T19:00:00", first.auction.scheduledStart)
        assertEquals(Money(2_050_000), first.auction.startingBid)
        assertEquals(Money(2_900_000), first.auction.reservePrice)
        assertEquals(Money(2_100_000), first.auction.currentBid)
        assertEquals(11, first.auction.bidCount)
        assertNull(first.auction.buyNowPrice)
        assertEquals(4, first.images.size)
        assertTrue(first.images.first().startsWith("https://placehold.co/"))

        assertTrue(vehicles.any { it.auction.currentBid == null && it.auction.bidCount == 0 })
        assertTrue(vehicles.any { it.auction.reservePrice == null })
        assertTrue(vehicles.any { it.auction.buyNowPrice != null })
        assertTrue(vehicles.any { it.condition.damageNotes.isNotEmpty() })
    }

    @Test
    fun `malformed JSON is exposed as invalid data`() = runTest {
        val api = DemoVehicleRepositoryFactory.createApi(DemoInventoryInterceptor { "not JSON" })
        val repository = DefaultVehicleRepository(api, StandardTestDispatcher(testScheduler))

        repository.loadInventory()

        assertEquals(
            VehicleInventory.Failed(InventoryFailure.INVALID_DATA),
            repository.inventory.value,
        )
    }

    @Test
    fun `failed fixture load can be retried`() = runTest {
        var fail = true
        val api = DemoVehicleRepositoryFactory.createApi(
            DemoInventoryInterceptor {
                if (fail) throw IOException("Fixture unavailable")
                "[]"
            },
        )
        val repository = DefaultVehicleRepository(api, StandardTestDispatcher(testScheduler))
        repository.loadInventory()
        assertEquals(
            VehicleInventory.Failed(InventoryFailure.UNAVAILABLE),
            repository.inventory.value,
        )

        fail = false
        repository.loadInventory()
        assertEquals(VehicleInventory.Ready(emptyList()), repository.inventory.value)
    }

    @Test
    fun `unsupported demo endpoints return 404 without reading inventory or using network`() {
        var read = false
        val client = OkHttpClient.Builder()
            .addInterceptor(DemoInventoryInterceptor { read = true; "[]" })
            .build()

        client.newCall(
            Request.Builder().url("https://vehicle-demo.invalid/other").build(),
        ).execute().use { response ->
            assertEquals(404, response.code)
        }
        assertFalse(read)
    }
}
