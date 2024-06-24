package com.example.tfgkotlin.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.tfgkotlin.R
import com.example.tfgkotlin.dbmanager.DBAccess
import com.example.tfgkotlin.fragments.Administration
import com.example.tfgkotlin.fragments.Calendar
import com.example.tfgkotlin.fragments.Home
import com.example.tfgkotlin.fragments.Matches
import com.example.tfgkotlin.fragments.PlayerProfile
import com.example.tfgkotlin.fragments.ViewCalifications
import com.example.tfgkotlin.objects.UserType
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var fragment: Fragment

    private var userId = ""
    private lateinit var userType: UserType
    private var startingPosition: Int = 0

    private lateinit var bnv: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getUserIdIntent()
        setUserType()

        bnv = findViewById(R.id.bottomNavigationView)
        loadFragment(Home(), 0)
        startingPosition = 0

        var newPosition = 0
        bnv.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    fragment = Home()
                    newPosition = 1
                }

                R.id.calendar -> {
                    fragment = Calendar()
                    newPosition = 2
                }

                R.id.statistics -> {
                    fragment = ViewCalifications()
                    newPosition = 3
                }

                R.id.matches -> {
                    fragment = Matches()
                    newPosition = 4
                }

                R.id.profile -> {
                    fragment = if (userType == UserType.ADMIN) {
                        Administration()
                    } else {
                        PlayerProfile()
                    }
                    newPosition = 5
                }
            }

            if (::fragment.isInitialized) {
                loadFragment(fragment, newPosition)
            }

            true
        }
    }

    private fun loadFragment(fragment: Fragment?, newPosition: Int): Boolean {
        if (fragment != null) {
            if (newPosition == 0) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment, fragment).commit()
            }
            if (startingPosition > newPosition) {
                supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.transition_from_left, R.anim.transition_from_right)
                    .replace(R.id.fragment, fragment).commit()
            }
            if (startingPosition < newPosition) {
                supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.transition_left, R.anim.transition_right)
                    .replace(R.id.fragment, fragment).commit()
            }

            startingPosition = newPosition
            return true
        }
        return false
    }

    private fun setIconProfile() {
        val menuItem: MenuItem = bnv.menu.findItem(R.id.profile)
        if (userType == UserType.ADMIN) {
            menuItem.setIcon(R.drawable.admin_profile)
        } else {
            menuItem.setIcon(R.drawable.person)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bottom_menu, menu)
        return true
    }

    fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun changeFragmentFade(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.transition_fade_in,
                R.anim.transition_fade_out
            )
            .replace(R.id.fragment, fragment).commit()
    }

    fun changeFragmentTransition(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.transition_from_left, R.anim.transition_from_right)
            .replace(R.id.fragment, fragment).commit()
    }

    fun changeFragmentTransitionRight(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.transition_left, R.anim.transition_right)
            .replace(R.id.fragment, fragment).commit()
    }

    private fun getUserIdIntent() {
        userId = intent.getStringExtra("userId")!!
    }

    fun getUserId(): String {
        return userId
    }

    fun getUserType(): UserType {
        return userType
    }

    private fun setUserType() {
        lifecycleScope.launch {
            DBAccess.getUserType(userId).collect { user ->
                if (user == UserType.ADMIN.toString()) {
                    userType = UserType.ADMIN
                } else {
                    userType = UserType.PLAYER
                }
                setIconProfile()
            }
        }
    }
}