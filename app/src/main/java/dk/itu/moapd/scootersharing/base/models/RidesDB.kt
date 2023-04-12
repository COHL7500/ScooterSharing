/*

package dk.itu.moapd.scootersharing.base.models

import android.content.Context
import dk.itu.moapd.scootersharing.base.R
import java.util.Random
import kotlin.collections.ArrayList

class RidesDB private constructor (context: Context) {

    private val rides = ArrayList<Scooter>()
    companion object: RidesDBHolder<RidesDB, Context>(::RidesDB)

    // TODO: Make init work with RidesDBTest.kt
    // context.resources.getStringArray(R.array.scooter_names)[0]
    init {
        rides.add(Scooter ("CPH-001", "ITU", randomDate()))
        rides.add(Scooter ("CPH-002", "Fields", randomDate()))
        rides.add(Scooter ("CPH-003", "Lufthavn", randomDate()))
    }

    fun getRidesList (): ArrayList <Scooter> {
        return rides;
    }

    fun addScooter (name: String , location: String) {
        rides.add(Scooter(name, location, randomDate()));
    }

    fun updateCurrentScooter (location: String) {
        val scooter = rides.last()
        scooter.where = location
        scooter.timestamp = randomDate()
    }

    fun getCurrentScooter (): Scooter {
        return rides.last()
    }

    fun getCurrentScooterInfo (): String {
        val scooter = rides.last()
        return scooter.toString()
    }

    fun clearAll() {
        rides.clear()
    }

    /**
     * Generate a random timestamp in the last 365 days .
     *
     * @return A random timestamp in the last year .
     */
    private fun randomDate(): Long {
        val random = Random()
        val now = System.currentTimeMillis()
        val year = random.nextDouble() * 1000 * 60 * 60 * 24 * 365
        return (now - year).toLong()
    }
}

open class RidesDBHolder <out T: Any, in A>( creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null
    fun get(arg: A): T {
        val checkInstance = instance
        if (checkInstance != null)
            return checkInstance
        return synchronized (this) {
            val checkInstanceAgain = instance
            if (checkInstanceAgain != null)
                checkInstanceAgain
            else {
                val created = creator !! (arg)
                instance = created
                creator = null
                created
            }
        }
    }
}
*/