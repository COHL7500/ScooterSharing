package dk.itu.moapd.scootersharing.base.activities

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import dk.itu.moapd.scootersharing.base.R
import org.junit.Before
import org.junit.Test

class MainActivityTest {

    @Before
    fun setup() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        ActivityScenario.launch<MainActivity>(intent)
    }

    @Test
    fun testNavigationToStartRideActivity() {
        onView(withId(R.id.start_ride_button)).perform(click())
        onView(withId(R.id.fragment_start_ride_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToUpdateRideActivity() {
        onView(withId(R.id.update_ride_button)).perform(click())
        onView(withId(R.id.fragment_update_ride_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToListRidesActivity() {
        onView(withId(R.id.list_rides_button)).perform(click())
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
    }
}