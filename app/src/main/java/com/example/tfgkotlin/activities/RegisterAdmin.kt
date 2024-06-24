package com.example.tfgkotlin.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.example.tfgkotlin.R
import com.example.tfgkotlin.dbmanager.DBAccess
import com.example.tfgkotlin.objects.Administrator
import com.example.tfgkotlin.model.OpenGallery
import com.example.tfgkotlin.objects.UserType
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class RegisterAdmin : AppCompatActivity() {
    private lateinit var regAdminImg: ImageView
    private lateinit var ctw: ContextThemeWrapper
    private lateinit var adminUsername: EditText
    private lateinit var adminEmail: EditText
    private lateinit var dialog: AlertDialog
    private lateinit var adminPass: EditText
    private lateinit var adminCode: EditText
    private lateinit var adminAccept: Button
    private lateinit var regAdminBack: ImageView
    private lateinit var anim: Animation
    private lateinit var passReactivate: String
    private var imgChosen: Uri? = null
    private var accessCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register_admin)

        setAttributes()
        getCode()
        accept()
        back()
        listenerImage()
    }

    private fun setAttributes() {
        regAdminImg = findViewById(R.id.regAdminImg)
        adminUsername = findViewById(R.id.adminUsername)
        adminEmail = findViewById(R.id.adminEmail)
        adminPass = findViewById(R.id.adminPass)
        adminCode = findViewById(R.id.adminCode)
        adminAccept = findViewById(R.id.adminAccept)
        regAdminBack = findViewById(R.id.regAdminBack)
        anim = AnimationUtils.loadAnimation(this, R.anim.escalate)
        ctw = ContextThemeWrapper(this, R.style.CustomSnackbarTheme)
    }

    private fun getCode() {
        DBAccess.getAccessCode(UserType.ADMIN.toString()) { code ->
            accessCode = code
        }
    }

    private fun accept() {
        adminAccept.setOnClickListener {
            adminAccept.startAnimation(anim)
            comprobations()
        }
    }

    private fun comprobations() {
        val view = window.decorView.rootView
        val username = adminUsername.text.toString().trim()
        val email = adminEmail.text.toString().trim()
        val password = adminPass.text.toString().trim()
        val code = adminCode.text.toString().trim()
        val passRegex = Regex("^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")
        val regEmail = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")

        if (username.isEmpty()) {
            adminUsername.error = getString(R.string.campos_vacios)
        }
        if (email.isEmpty()) {
            adminEmail.error = getString(R.string.campos_vacios)
        }
        if (password.isEmpty()) {
            adminPass.error = getString(R.string.campos_vacios)
        }
        if (code.isEmpty()) {
            adminCode.error = getString(R.string.campos_vacios)
        }
        if (imgChosen == null) {
            Snackbar.make(
                ctw,
                view,
                "Selecciona una imagen de perfil",
                Toast.LENGTH_SHORT
            ).show()
        }

        if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && code.isNotEmpty()) {
            if (username.length < 5) {
                adminUsername.error = "El nombre debe contener mínimo 5 caracteres."
            }
            if (!email.matches(regEmail)) {
                adminEmail.error = getString(R.string.email_invalido)
            }
            if (!password.matches(passRegex)) {
                adminPass.error =
                    "La contraseña debe contener al menos 1 dígito, 1 caracter alfabético y 8 caracteres totales."
            }
            if (code != accessCode) {
                adminCode.error = getString(R.string.codigo_incorrecto)
            }
            if (username.length >= 5 && email.matches(regEmail) && password.matches(passRegex) && code == accessCode && imgChosen != null) {
                signUpAdmin(email, password, username)
            }
        }
    }

    private fun signUpAdmin(userEmail: String, userPass: String, userName: String) {
        DBAccess.checkEmailExistsReactivate(
            userEmail,
            UserType.ADMIN.toString()
        ) { exists, id, name ->
            if (exists) {
                if (userName != name) {
                    DBAccess.checkUsernameExists(userName) { nameExists ->
                        if (nameExists) {
                            adminUsername.error = "El nombre ya está en uso."
                        } else {
                            showDialogReactivate(userEmail, userName, userPass, id!!)
                        }
                    }
                } else {
                    showDialogReactivate(userEmail, userName, userPass, id!!)
                }
            } else {
                DBAccess.checkUsernameExists(userName) { nameExists ->
                    if (nameExists) {
                        adminUsername.error = "El nombre ya está en uso."
                    } else {
                        DBAccess.checkEmailExists(userEmail) { emailExists ->
                            if (emailExists) {
                                adminEmail.error = "El email ya está en uso."
                            } else {
                                val encryptedPassword = DBAccess.encryptPassword(userPass)
                                DBAccess.signUpAdmin(
                                    userEmail,
                                    encryptedPassword!!,
                                    userName
                                ) { signedUp, id ->
                                    if (signedUp) {
                                        id?.let {
                                            uploadImage(imgChosen!!, id)
                                            goToLogin(userEmail, userPass)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showDialogReactivate(
        email: String,
        username: String,
        userPass: String,
        id: String
    ) {
        val view =
            LayoutInflater.from(this).inflate(R.layout.dialog_reactivate_account, null)
        val cancelReactivate = view.findViewById<Button>(R.id.cancelReactivate)
        val acceptReactivate = view.findViewById<Button>(R.id.acceptReactivate)
        val passwordReactivate = view.findViewById<EditText>(R.id.passwordReactivate)
        val rootView = window.decorView.rootView

        cancelReactivate.setOnClickListener { dialog.dismiss() }
        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)

        acceptReactivate.setOnClickListener {
            passReactivate = passwordReactivate.text.toString().trim()
            if (passReactivate.isEmpty()) {
                passwordReactivate.error = "Rellena este campo."
            } else {
                val passwordEncrypted = DBAccess.encryptPassword(passReactivate)
                val newPassEncrypted = DBAccess.encryptPassword(userPass)
                DBAccess.checkPassword(email, passwordEncrypted!!) { correct ->
                    if (correct) {
                        DBAccess.reactivateAdmin(
                            email,
                            username,
                            newPassEncrypted!!
                        ) { reactivated ->
                            if (reactivated) {
                                uploadImage(imgChosen!!, id)
                                goToLogin(email, userPass)
                                dialog.dismiss()
                            } else {
                                Snackbar.make(
                                    ctw,
                                    rootView,
                                    "No se pudo reactivar el usuario",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            }
                        }
                    } else {
                        passwordReactivate.error = "La contraseña es incorrecta."
                    }
                }
            }
        }

        dialog.show()
    }

    private fun goToLogin(userEmail: String, userPass: String) {
        val intent = Intent(this, Login::class.java)
        intent.putExtra("email", userEmail)
        intent.putExtra("password", userPass)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            this,
            R.anim.transition_from_left,
            R.anim.transition_from_right
        )
        startActivity(intent, options.toBundle())
        this.finish()
    }

    private fun back() {
        regAdminBack.startAnimation(anim)
        regAdminBack.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            val options = ActivityOptionsCompat.makeCustomAnimation(
                this,
                R.anim.transition_from_left,
                R.anim.transition_from_right
            )
            startActivity(intent, options.toBundle())
            this.finish()
        }
    }

    private fun uploadImage(imgUri: Uri, idUser: String) {
        DBAccess.uploadImage(imgUri, idUser)
    }

    private fun listenerImage() {
        regAdminImg.setOnClickListener { OpenGallery.openGallery(this, galleryLauncher) }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                imgChosen = result.data?.data
                if (imgChosen != null) {
                    regAdminImg.setImageURI(imgChosen)
                }
            }
        }
}