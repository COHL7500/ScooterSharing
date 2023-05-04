package dk.itu.moapd.scootersharing.base.activities

import android.content.Intent
import android.provider.MediaStore
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.*
import dk.itu.moapd.scootersharing.base.R
import org.junit.*
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MainActivityTest {


    @Before
    fun setup() {
        Intents.init()
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        ActivityScenario.launch<MainActivity>(intent)
    }

    @After
    fun teardown() {
        Intents.release()
    }

    @Test
    fun A_testMainActivityButtonsDisplayed() {
        onView(withId(R.id.start_ride_button)).check(matches(isDisplayed()))
        onView(withId(R.id.list_rides_button)).check(matches(isDisplayed()))
        onView(withId(R.id.map_button)).check(matches(isDisplayed()))
    }

    @Test
    fun B_testNavigationToStartRideActivity() {

        onView(withId(R.id.start_ride_button)).perform(click())
        intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    @Test
    fun C_testNavigationToListRidesActivity() {
        onView(withId(R.id.list_rides_button)).perform(click())
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
    }

    @Test
    fun D_testNavigationToMapActivity() {
        onView(withId(R.id.map_button)).perform(click())
        onView(withId(R.id.fragment_map_layout)).check(matches(isDisplayed()))
    }

}