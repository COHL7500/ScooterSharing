package dk.itu.moapd.scootersharing.base.models

import android.content.Context
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RidesDBTest {

    @Mock
    lateinit var mockContext: Context

    private lateinit var ridesDB: RidesDB

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        ridesDB = RidesDB.get(mockContext)
    }

    @Test
    fun A_testGetRidesList() {
        val ridesList = ridesDB.getRidesList()
        assertNotNull(ridesList)
        assertEquals(3, ridesList.size)
    }

    @Test
    fun B_testAddScooter() {
        ridesDB.addScooter("Scooter", "Location")
        assertEquals(4, ridesDB.getRidesList().size)
    }

    @Test
    fun C_testGetCurrentScooter() {
        val expected = Scooter("Scooter", "Location", 1234567)
        val actual = ridesDB.getCurrentScooter()
        assertEquals(expected.name, actual.name)
        assertEquals("Location", actual.where)
    }

    @Test
    fun D_testUpdateCurrentScooter() {
        ridesDB.updateCurrentScooter("newLocation")
        val currentScooter = ridesDB.getCurrentScooter()
        assertEquals("newLocation", currentScooter.where)
    }

    @Test
    fun E_testGetCurrentScooterInfo() {
        val scooterInfo = ridesDB.getCurrentScooterInfo()
        assertNotNull(scooterInfo)
        assertTrue(scooterInfo.isNotEmpty())
    }

}