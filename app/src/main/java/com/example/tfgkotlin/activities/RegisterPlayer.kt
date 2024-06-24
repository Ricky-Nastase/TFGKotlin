package com.example.tfgkotlin.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import com.example.tfgkotlin.R
import com.example.tfgkotlin.dbmanager.DBAccess
import com.example.tfgkotlin.model.OpenGallery
import com.example.tfgkotlin.objects.UserSex
import com.example.tfgkotlin.objects.UserType
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class RegisterPlayer : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var regPlayerImg: ImageView
    private lateinit var playUsername: EditText
    private lateinit var playEmail: EditText
    private lateinit var playPass: EditText
    private lateinit var playCode: EditText
    private lateinit var playWeight: EditText
    private lateinit var playHeight: EditText
    private lateinit var playNumber: EditText

    private lateinit var ctw: ContextThemeWrapper

    private lateinit var positionsSpinner: Spinner
    private var selecPos = ""

    private lateinit var playBirthdate: TextView
    private lateinit var playAccept: Button
    private lateinit var regPlayBack: ImageView
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var txtBirthdate = ""

    private lateinit var rbMasc: RadioButton
    private lateinit var rbFem: RadioButton
    private lateinit var rbOther: RadioButton
    private lateinit var playSex: String

    private lateinit var dialog: AlertDialog
    private lateinit var anim: Animation
    private var imgChosen: Uri? = null
    private var accessCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register_player)

        setAttributes()
        getCode()
        getBirthdate()
        getPositions()
        positionsSpinner.onItemSelectedListener = this
        accept()
        back()
        listenerImage()
    }

    private fun setAttributes() {
        regPlayerImg = findViewById(R.id.regPlayerImg)
        playUsername = findViewById(R.id.playUsername)
        playEmail = findViewById(R.id.playEmail)
        playPass = findViewById(R.id.playPass)
        playCode = findViewById(R.id.playCode)
        playWeight = findViewById(R.id.playWeight)
        playHeight = findViewById(R.id.playHeight)
        playNumber = findViewById(R.id.playNumber)
        positionsSpinner = findViewById(R.id.positionsSpinner)
        playBirthdate = findViewById(R.id.playBirthdate)
        playAccept = findViewById(R.id.playAccept)
        regPlayBack = findViewById(R.id.regPlayBack)
        rbMasc = findViewById(R.id.rbMasc)
        rbFem = findViewById(R.id.rbFem)
        rbOther = findViewById(R.id.rbOther)
        anim = AnimationUtils.loadAnimation(this, R.anim.escalate)
        rbMasc.isChecked = true
    }

    private fun getCode() {
        DBAccess.getAccessCode(UserType.PLAYER.toString()) { code ->
            accessCode = code
        }
    }

    private fun back() {
        regPlayBack.startAnimation(anim)
        regPlayBack.setOnClickListener {
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

    private fun accept() {
        playAccept.setOnClickListener {
            playAccept.startAnimation(anim)
            comprobations()
        }
    }

    private fun comprobations() {
        val view = window.decorView.rootView
        val username = playUsername.text.toString().trim()
        val email = playEmail.text.toString().trim()
        val password = playPass.text.toString().trim()
        val weight = playWeight.text.toString().trim()
        val height = playHeight.text.toString().trim()
        val number = playNumber.text.toString().trim()
        val code = playCode.text.toString().trim()
        ctw = ContextThemeWrapper(view.getContext(), R.style.CustomSnackbarTheme)

        if (rbMasc.isChecked) {
            playSex = UserSex.MALE.toString()
        } else if (rbFem.isChecked) {
            playSex = UserSex.FEMALE.toString()
        } else if (rbOther.isChecked) {
            playSex = UserSex.OTHER.toString()
        }

        val passRegex = Regex("^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")
        val regEmail = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")

        if (username.isEmpty()) {
            playUsername.error = getString(R.string.campos_vacios)
        }
        if (email.isEmpty()) {
            playEmail.error = getString(R.string.campos_vacios)
        }
        if (password.isEmpty()) {
            playPass.error = getString(R.string.campos_vacios)
        }
        if (code.isEmpty()) {
            playCode.error = getString(R.string.campos_vacios)
        }
        if (weight.isEmpty()) {
            playWeight.error = getString(R.string.campos_vacios)
        }
        if (height.isEmpty()) {
            playHeight.error = getString(R.string.campos_vacios)
        }
        if (number.isEmpty()) {
            playNumber.error = getString(R.string.campos_vacios)
        }
        if (txtBirthdate.isEmpty()) {
            playBirthdate.error = getString(R.string.campos_vacios)
        }
        if (imgChosen == null) {
            Snackbar.make(
                ctw,
                view,
                getResources().getString(R.string.seles_img_perfil),
                Snackbar.LENGTH_SHORT
            ).show()
        }

        if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && code.isNotEmpty() && weight.isNotEmpty() && height.isNotEmpty() && number.isNotEmpty() && txtBirthdate.isNotEmpty() && selecPos.isNotEmpty()) {
            if (username.length < 5) {
                playUsername.error = "El nombre debe contener mínimo 5 caracteres."
            }
            if (!email.matches(regEmail)) {
                playEmail.error = getString(R.string.email_invalido)
            }
            if (!password.matches(passRegex)) {
                playPass.error =
                    "La contraseña debe contener al menos 1 dígito, 1 caracter alfabético y 8 caracteres totales."
            }
            if (code != accessCode) {
                playCode.error = getString(R.string.codigo_incorrecto)
            }
            if (username.length >= 5 && email.matches(regEmail) && password.matches(passRegex) && code == accessCode && imgChosen != null) {
                signUpPlayer(email, password, username, weight, height, number)
            }
        }
    }

    private fun getBirthdate() {
        playBirthdate.setOnClickListener {
            val cal = Calendar.getInstance()
            val year = cal[Calendar.YEAR]
            val month = cal[Calendar.MONTH]
            val day = cal[Calendar.DAY_OF_MONTH]
            val dialog = DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                dateSetListener,
                year, month, day
            )
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.datePicker.maxDate = System.currentTimeMillis()
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        }
        dateSetListener =
            DatePickerDialog.OnDateSetListener() { datePicker, year, month, day ->
                var month = month
                month = month + 1
                txtBirthdate = "$day/$month/$year"
                playBirthdate.text = txtBirthdate
            }
    }

    private fun showDialogReactivate(
        userEmail: String,
        userPass: String,
        userName: String,
        userWeight: String,
        userHeight: String,
        userNumber: String,
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
            val passReactivate = passwordReactivate.text.toString().trim()
            if (passReactivate.isEmpty()) {
                passwordReactivate.error = "Rellena este campo."
            } else {
                val passwordEncrypted = DBAccess.encryptPassword(passReactivate)
                val newPassEncrypted = DBAccess.encryptPassword(userPass)
                DBAccess.checkPassword(userEmail, passwordEncrypted!!) { correct ->
                    if (correct) {
                        DBAccess.reactivatePlayer(
                            userEmail,
                            newPassEncrypted!!,
                            userName,
                            userWeight,
                            userHeight,
                            selecPos,
                            txtBirthdate,
                            userNumber,
                            playSex
                        ) { reactivated ->
                            if (reactivated) {
                                uploadImage(imgChosen!!, id)
                                goToLogin(userEmail, userPass)
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

    private fun signUpPlayer(
        userEmail: String,
        userPass: String,
        userName: String,
        userWeight: String,
        userHeight: String,
        userNumber: String
    ) {
        DBAccess.checkEmailExistsReactivate(
            userEmail,
            UserType.PLAYER.toString()
        ) { emailExists, id, name ->
            if (emailExists) {
                if (userName != name) {
                    DBAccess.checkUsernameExists(userName) { nameExists ->
                        if (nameExists) {
                            playUsername.error = "El nombre ya está en uso."
                        } else {
                            DBAccess.checkNumber(userNumber, userEmail) { numExists ->
                                if (numExists) {
                                    showDialogReactivate(
                                        userEmail,
                                        userPass,
                                        userName,
                                        userWeight,
                                        userHeight,
                                        userNumber,
                                        id!!
                                    )
                                } else {
                                    DBAccess.checkNumberExists(userNumber) { numberExists ->
                                        if (numberExists) {
                                            playNumber.error = "El dorsal ya está en uso."
                                        } else {
                                            showDialogReactivate(
                                                userEmail,
                                                userPass,
                                                userName,
                                                userWeight,
                                                userHeight,
                                                userNumber,
                                                id!!
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    DBAccess.checkNumber(userNumber, userEmail) { numExists ->
                        if (numExists) {
                            showDialogReactivate(
                                userEmail,
                                userPass,
                                userName,
                                userWeight,
                                userHeight,
                                userNumber,
                                id!!
                            )
                        } else {
                            DBAccess.checkNumberExists(userNumber) { numberExists ->
                                if (numberExists) {
                                    playNumber.error = "El dorsal ya está en uso."
                                } else {
                                    showDialogReactivate(
                                        userEmail,
                                        userPass,
                                        userName,
                                        userWeight,
                                        userHeight,
                                        userNumber,
                                        id!!
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                DBAccess.checkUsernameExists(userName) { nameExists ->
                    if (nameExists) {
                        playUsername.error = "El nombre ya está en uso."
                    } else {
                        DBAccess.checkEmailExists(userEmail) { emailExists ->
                            if (emailExists) {
                                playEmail.error = "El email ya está en uso."
                            } else {
                                DBAccess.checkNumberExists(userNumber) { numberExists ->
                                    if (numberExists) {
                                        playNumber.error = "El dorsal ya está en uso."
                                    } else {
                                        val passwordEncrypted = DBAccess.encryptPassword(userPass)
                                        DBAccess.signUpPlayer(
                                            userEmail,
                                            passwordEncrypted!!,
                                            userName,
                                            userWeight,
                                            userHeight,
                                            selecPos,
                                            txtBirthdate,
                                            userNumber,
                                            playSex
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
        }
    }

    private fun getPositions() {
        lifecycleScope.launch {
            DBAccess.getPositions().collect { positions ->
                configDropDown(positions)
            }
        }
    }

    private fun configDropDown(positions: MutableList<String>) {
        if (positions.isNotEmpty()) {
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, positions)
            adapter.setDropDownViewResource(R.layout.spinner_item)
            positionsSpinner.setAdapter(adapter)
        } else {
            positions.add("Sin posición")
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, positions)
            adapter.setDropDownViewResource(R.layout.spinner_item)
            positionsSpinner.setAdapter(adapter)
        }
    }

    override fun onItemSelected(
        parent: AdapterView<*>,
        view: android.view.View?,
        position: Int,
        id: Long
    ) {
        selecPos = parent.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //default
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

    private fun uploadImage(imgUri: Uri, idUser: String) {
        DBAccess.uploadImage(imgUri, idUser)
    }

    private fun listenerImage() {
        regPlayerImg.setOnClickListener { OpenGallery.openGallery(this, galleryLauncher) }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                imgChosen = result.data?.data
                if (imgChosen != null) {
                    regPlayerImg.setImageURI(imgChosen)
                }
            }
        }

}