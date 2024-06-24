package com.example.tfgkotlin.fragments

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
import android.view.ViewGroup
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.tfgkotlin.R
import com.example.tfgkotlin.activities.MainActivity
import com.example.tfgkotlin.dbmanager.DBAccess
import com.example.tfgkotlin.model.OpenGallery
import com.example.tfgkotlin.objects.UserSex
import com.example.tfgkotlin.objects.UserType
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class AddNewPlayer : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var addPlayerImg: ImageView
    private lateinit var addPlayUsername: EditText
    private lateinit var addPlayEmail: EditText
    private lateinit var addPlayPass: EditText
    private lateinit var addPlayWeight: EditText
    private lateinit var addPlayHeight: EditText
    private lateinit var addPlayNumber: EditText

    private lateinit var ctw: ContextThemeWrapper

    private lateinit var positionsSpinner: Spinner
    private var selecPos = ""

    private lateinit var addPlayBirthdate: TextView
    private lateinit var addPlayAccept: Button
    private lateinit var addPlayBack: ImageView
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var txtBirthdate = ""

    private lateinit var rbMascAdd: RadioButton
    private lateinit var rbFemAdd: RadioButton
    private lateinit var rbOtherAdd: RadioButton
    private lateinit var playSex: String

    private lateinit var dialog: AlertDialog
    private lateinit var anim: Animation
    private var imgChosen: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_player, container, false)

        setAttributes(view)
        getBirthdate()
        getPositions()
        positionsSpinner.onItemSelectedListener = this
        accept()
        back()
        listenerImage()

        return view
    }

    private fun setAttributes(view: View) {
        addPlayerImg = view.findViewById(R.id.addPlayerImg)
        addPlayUsername = view.findViewById(R.id.addPlayUsername)
        addPlayEmail = view.findViewById(R.id.addPlayEmail)
        addPlayPass = view.findViewById(R.id.addPlayPass)
        addPlayWeight = view.findViewById(R.id.addPlayWeight)
        addPlayHeight = view.findViewById(R.id.addPlayHeight)
        addPlayNumber = view.findViewById(R.id.addPlayNumber)
        positionsSpinner = view.findViewById(R.id.addPositionsSpinner)
        addPlayBirthdate = view.findViewById(R.id.addPlayBirthdate)
        addPlayAccept = view.findViewById(R.id.addPlayAccept)
        addPlayBack = view.findViewById(R.id.addPlayBack)
        rbMascAdd = view.findViewById(R.id.rbMascAdd)
        rbFemAdd = view.findViewById(R.id.rbFemAdd)
        rbOtherAdd = view.findViewById(R.id.rbOtherAdd)
        anim = AnimationUtils.loadAnimation(requireContext(), R.anim.escalate)

        rbMascAdd.isChecked = true
    }

    private fun back() {
        addPlayBack.startAnimation(anim)
        addPlayBack.setOnClickListener {
            goBack()
        }
    }

    private fun accept() {
        addPlayAccept.setOnClickListener {
            addPlayAccept.startAnimation(anim)
            comprobations()
        }
    }

    private fun comprobations() {
        val view = requireView().rootView
        val username = addPlayUsername.text.toString().trim()
        val email = addPlayEmail.text.toString().trim()
        val password = addPlayPass.text.toString().trim()
        val weight = addPlayWeight.text.toString().trim()
        val height = addPlayHeight.text.toString().trim()
        val number = addPlayNumber.text.toString().trim()
        ctw = ContextThemeWrapper(view.getContext(), R.style.CustomSnackbarTheme)

        if (rbMascAdd.isChecked) {
            playSex = UserSex.MALE.toString()
        } else if (rbFemAdd.isChecked) {
            playSex = UserSex.FEMALE.toString()
        } else if (rbOtherAdd.isChecked) {
            playSex = UserSex.OTHER.toString()
        }

        val passRegex = Regex("^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")
        val regEmail = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")

        if (username.isEmpty()) {
            addPlayUsername.error = getString(R.string.campos_vacios)
        }
        if (email.isEmpty()) {
            addPlayEmail.error = getString(R.string.campos_vacios)
        }
        if (password.isEmpty()) {
            addPlayPass.error = getString(R.string.campos_vacios)
        }
        if (weight.isEmpty()) {
            addPlayWeight.error = getString(R.string.campos_vacios)
        }
        if (height.isEmpty()) {
            addPlayHeight.error = getString(R.string.campos_vacios)
        }
        if (number.isEmpty()) {
            addPlayNumber.error = getString(R.string.campos_vacios)
        }
        if (txtBirthdate.isEmpty()) {
            addPlayBirthdate.error = getString(R.string.campos_vacios)
        }
        if (imgChosen == null) {
            Snackbar.make(
                ctw,
                view,
                getResources().getString(R.string.seles_img_perfil),
                Snackbar.LENGTH_SHORT
            ).show()
        }

        if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && weight.isNotEmpty() && height.isNotEmpty() && number.isNotEmpty() && txtBirthdate.isNotEmpty() && selecPos.isNotEmpty()) {
            if (username.length < 5) {
                addPlayUsername.error = "El nombre debe contener mínimo 5 caracteres."
            }
            if (!email.matches(regEmail)) {
                addPlayEmail.error = getString(R.string.email_invalido)
            }
            if (!password.matches(passRegex)) {
                addPlayPass.error =
                    "La contraseña debe contener al menos 1 dígito, 1 caracter alfabético y 8 caracteres totales."
            }

            if (username.length >= 5 && email.matches(regEmail) && password.matches(passRegex) && imgChosen != null) {
                signUpPlayer(email, password, username, weight, height, number)
            }
        }
    }

    private fun getBirthdate() {
        addPlayBirthdate.setOnClickListener {
            val cal = Calendar.getInstance()
            val year = cal[Calendar.YEAR]
            val month = cal[Calendar.MONTH]
            val day = cal[Calendar.DAY_OF_MONTH]
            val dialog = DatePickerDialog(
                requireContext(),
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
                addPlayBirthdate.text = txtBirthdate
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
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reactivate_account, null)
        val cancelReactivate = view.findViewById<Button>(R.id.cancelReactivate)
        val acceptReactivate = view.findViewById<Button>(R.id.acceptReactivate)
        val passwordReactivate = view.findViewById<EditText>(R.id.passwordReactivate)
        val rootView = requireView().rootView

        cancelReactivate.setOnClickListener { dialog.dismiss() }
        val builder = AlertDialog.Builder(requireContext())
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
                                goBack()
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
                            addPlayUsername.error = "El nombre ya está en uso."
                        } else {
                            DBAccess.checkNumber(userNumber, userEmail) { numExists ->
                                if(numExists){
                                    showDialogReactivate(
                                        userEmail,
                                        userPass,
                                        userName,
                                        userWeight,
                                        userHeight,
                                        userNumber,
                                        id!!
                                    )
                                }else{
                                    DBAccess.checkNumberExists(userNumber) { numberExists ->
                                        if (numberExists) {
                                            addPlayNumber.error = "El dorsal ya está en uso."
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
                                    addPlayNumber.error = "El dorsal ya está en uso."
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
                        addPlayUsername.error = "El nombre ya está en uso."
                    } else {
                        DBAccess.checkEmailExists(userEmail) { emailExists ->
                            if (emailExists) {
                                addPlayEmail.error = "El email ya está en uso."
                            } else {
                                DBAccess.checkNumberExists(userNumber) { numberExists ->
                                    if (numberExists) {
                                        addPlayNumber.error = "El dorsal ya está en uso."
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
                                                    goBack()
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
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    positions
                )
            adapter.setDropDownViewResource(R.layout.spinner_item)
            positionsSpinner.setAdapter(adapter)
        } else {
            positions.add("Sin posición")
            val adapter =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    positions
                )
            adapter.setDropDownViewResource(R.layout.spinner_item)
            positionsSpinner.setAdapter(adapter)
        }
    }

    private fun goBack() {
        (activity as MainActivity).changeFragmentTransition(Administration())
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

    private fun uploadImage(imgUri: Uri, idUser: String) {
        DBAccess.uploadImage(imgUri, idUser)
    }

    private fun listenerImage() {
        addPlayerImg.setOnClickListener {
            OpenGallery.openGallery(
                requireActivity(),
                galleryLauncher
            )
        }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                imgChosen = result.data?.data
                if (imgChosen != null) {
                    addPlayerImg.setImageURI(imgChosen)
                }
            }
        }

}