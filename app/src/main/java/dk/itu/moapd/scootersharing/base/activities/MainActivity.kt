/*
 * MIT License
 * Copyright (c) 2023 Bastjan Sejberg
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * aORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dk.itu.moapd.scootersharing.base.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.scootersharing.base.fragments.MainFragment
import dk.itu.moapd.scootersharing.base.R
import dk.itu.moapd.scootersharing.base.databinding.ActivityMainBinding

/**
 Class for binding the view and instantiating Scooter.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    private fun startLoginActivity() {
        val intent = Intent(baseContext, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    /* TODO: This needs to be commented out everytime running MainActivityTest.
        Make it work without needing to comment it out.
     */


    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null)
            startLoginActivity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val mainFragment = MainFragment()
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container_view, mainFragment)
        fragmentTransaction.commit()
    }
}

