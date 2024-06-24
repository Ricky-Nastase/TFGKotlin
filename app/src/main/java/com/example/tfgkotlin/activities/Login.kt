package com.example.tfgkotlin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import com.example.tfgkotlin.R
import com.example.tfgkotlin.dbmanager.DBAccess
import com.example.tfgkotlin.dbmanager.DBCalification
import com.example.tfgkotlin.model.checkCodeChange
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs


class Login : AppCompatActivity() {
    private lateinit var regPlayer: CardView
    private lateinit var regAdmin: CardView
    private lateinit var loginEmail: EditText
    private lateinit var loginPass: EditText
    private lateinit var btnLogin: Button
    private lateinit var anim: Animation
    private lateinit var ctw: ContextThemeWrapper
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)

        checkCodeChange()



        setAttributes()
        getPreferences()
        regPlayer()
        regAdmin()
        login()
        getData()
    }

    private fun setAttributes() {
        regPlayer = findViewById(R.id.btnRegPlayer)
        regAdmin = findViewById(R.id.btnRegAdmin)
        loginEmail = findViewById(R.id.loginEmail)
        loginPass = findViewById(R.id.loginPass)
        btnLogin = findViewById(R.id.btnLogin)
        anim = AnimationUtils.loadAnimation(this, R.anim.escalate)
        ctw = ContextThemeWrapper(this, R.style.CustomSnackbarTheme)
    }

    private fun regPlayer() {
        regPlayer.setOnClickListener {
            regPlayer.startAnimation(anim)
            val intent = Intent(this, RegisterPlayer::class.java)
            val options = ActivityOptionsCompat.makeCustomAnimation(
                this,
                R.anim.transition_left,
                R.anim.transition_right
            )

            startActivity(intent, options.toBundle())
        }
    }

    private fun regAdmin() {
        regAdmin.setOnClickListener {
            regAdmin.startAnimation(anim)
            val intent = Intent(this, RegisterAdmin::class.java)
            val options = ActivityOptionsCompat.makeCustomAnimation(
                this,
                R.anim.transition_left,
                R.anim.transition_right
            )
            startActivity(intent, options.toBundle())
        }
    }

    private fun setSP(id: String) {
        val sp = getSharedPreferences("login", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString("userId", id)
        editor.apply()
    }

    private fun getPreferences() {
        val sp = getSharedPreferences("login", Context.MODE_PRIVATE)
        //sp.edit().clear().apply() // Usar en caso de querer eliminar el guardado de preferencias durante las pruebas de desarrollo del código
        val userId = sp.getString("userId", "")
        if (!userId.isNullOrEmpty()) {
            goToMain(userId)
        }
    }

    private fun login() {
        btnLogin.setOnClickListener {
            val view = window.decorView.rootView
            btnLogin.startAnimation(anim)
            val email = loginEmail.text.toString().trim()
            val password = loginPass.text.toString().trim()

            if (email.isEmpty()) {
                loginEmail.error = getString(R.string.campos_vacios)
            }
            if (password.isEmpty()) {
                loginPass.error = getString(R.string.campos_vacios)
            }

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val encryptedPassword = DBAccess.encryptPassword(password)
                DBAccess.login(email, encryptedPassword!!) { success, id, failureReason ->
                    if (success) {
                        id?.let {
                            DBAccess.checkUserActive(id) { isActive ->
                                if (isActive) {
                                    userId = id
                                    setSP(userId!!)
                                    goToMain(userId!!)
                                } else {
                                    Snackbar.make(
                                        ctw,
                                        view,
                                        "El usuario no está activo",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        failureReason?.let { reason ->
                            when (reason) {
                                DBAccess.LoginFailureReason.USER_NOT_FOUND -> {
                                    Snackbar.make(
                                        ctw,
                                        view,
                                        "El usuario no existe",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                DBAccess.LoginFailureReason.INVALID_CREDENTIALS -> {
                                    loginPass.error = "Email o contraseña incorrectos."
                                    loginEmail.error = "Email o contraseña incorrectos."
                                }

                                DBAccess.LoginFailureReason.DATABASE_ERROR -> {
                                    Log.d("tag", "Error Database Login")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun goToMain(userId: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userId", userId)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            this,
            R.anim.transition_fade_in,
            R.anim.transition_fade_out
        )
        startActivity(intent, options.toBundle())
        this.finish()
    }

    private fun getData() {
        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")
        if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
            loginEmail.setText(email)
            loginPass.setText(password)
        }
    }
}