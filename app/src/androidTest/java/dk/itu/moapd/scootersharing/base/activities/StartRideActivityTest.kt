package dk.itu.moapd.scootersharing.base.activities

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import dk.itu.moapd.scootersharing.base.R
import dk.itu.moapd.scootersharing.base.models.RidesDB
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

private lateinit var ridesDB: RidesDB

// TODO: Finish these tests.

class StartRideActivityTest
{
    @Before
    fun setUp() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), StartRideActivity::class.java)
        ActivityScenario.launch<StartRideActivity>(intent)

        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        ridesDB = RidesDB.get(appContext)
    }

    @Test
    fun testScooterNameIsRequired() {
        onView(withId(R.id.edit_text_name))
            .perform(typeText("Name"), closeSoftKeyboard())

        onView(withId(R.id.start_ride_button))
            .perform(click())

        assertEquals(3, ridesDB.getRidesList().size)
    }

    @Test
    fun testScooterLocationIsRequired() {
        onView(withId(R.id.edit_text_location))
            .perform(typeText("Location"), closeSoftKeyboard())

        onView(withId(R.id.start_ride_button))
            .perform(click())

        assertEquals(3, ridesDB.getRidesList().size)
    }



}