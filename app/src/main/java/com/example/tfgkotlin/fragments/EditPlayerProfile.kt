package com.example.tfgkotlin.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.activities.Login
import com.example.tfgkotlin.activities.MainActivity
import com.example.tfgkotlin.dbmanager.DBAccess
import com.example.tfgkotlin.dbmanager.DBAdministration
import com.example.tfgkotlin.model.OpenGallery
import com.example.tfgkotlin.objects.UserSex
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class EditPlayerProfile : Fragment(), AdapterView.OnItemSelectedListener {
    private var userId: String? = null
    private lateinit var editPlayerImg: ImageView
    private lateinit var dialog: AlertDialog
    private lateinit var deletePlayer: LinearLayout
    private lateinit var acceptEditPlayer: Button
    private lateinit var editPlayerBack: ImageView
    private lateinit var editPlayerName: EditText
    private lateinit var editPlayerEmail: EditText
    private lateinit var editPlayerPass: EditText
    private lateinit var editPlayerNewPass: EditText
    private lateinit var editPlayerWeight: EditText
    private lateinit var editPlayerHeight: EditText
    private lateinit var editPlayerPos: Spinner
    private lateinit var editPlayerBirthdate: TextView
    private lateinit var editPlayerNumber: EditText
    private var oldUserEmail = ""
    private lateinit var sp: SharedPreferences
    private var oldUserName: String? = null
    private var oldNumber: String? = null
    private var oldSex: String? = null
    private var oldWeight: String? = null
    private var oldHeight: String? = null
    private var oldPos: String? = null
    private var oldDate: String? = null
    private lateinit var anim: Animation
    private var imgChosen: Uri? = null
    private var selecPos = ""
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var txtBirthdate = ""
    private lateinit var rbMascEdit: RadioButton
    private lateinit var rbFemEdit: RadioButton
    private lateinit var ctw: ContextThemeWrapper
    private lateinit var rbOtherEdit: RadioButton
    private lateinit var playSex: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_player_profile, container, false)

        setAttributes(view)
        setGoBack()
        getBirthdate()
        setValues()
        listenerImage()
        editPlayerPos.onItemSelectedListener = this
        configDropDown()
        deletePlayer(view)
        accept(view)

        return view
    }

    private fun setAttributes(view: View) {
        editPlayerImg = view.findViewById(R.id.editPlayerImg)
        deletePlayer = view.findViewById(R.id.deletePlayer)
        acceptEditPlayer = view.findViewById(R.id.acceptEditPlayer)
        editPlayerBack = view.findViewById(R.id.editPlayerBack)
        editPlayerName = view.findViewById(R.id.editPlayerName)
        editPlayerEmail = view.findViewById(R.id.editPlayerEmail)
        editPlayerPass = view.findViewById(R.id.editPlayerPass)
        editPlayerNewPass = view.findViewById(R.id.editPlayerNewPass)
        editPlayerWeight = view.findViewById(R.id.editPlayerWeight)
        editPlayerHeight = view.findViewById(R.id.editPlayerHeight)
        editPlayerPos = view.findViewById(R.id.editPlayerPos)
        editPlayerBirthdate = view.findViewById(R.id.editPlayerBirthdate)
        editPlayerNumber = view.findViewById(R.id.editPlayerNumber)
        rbMascEdit = view.findViewById(R.id.rbMascEdit)
        rbFemEdit = view.findViewById(R.id.rbFemEdit)
        rbOtherEdit = view.findViewById(R.id.rbOtherEdit)
        anim = AnimationUtils.loadAnimation(requireContext(), R.anim.escalate)
        userId = (activity as MainActivity).getUserId()
        ctw = ContextThemeWrapper(requireContext(), R.style.CustomSnackbarTheme)
    }

    private fun getBirthdate() {
        editPlayerBirthdate.setOnClickListener {
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
                editPlayerBirthdate.text = txtBirthdate
            }
    }

    private fun configDropDown() {
        lifecycleScope.launch {
            DBAccess.getPositions().collect { positions ->
                if (positions.isNotEmpty()) {
                    val adapter =
                        ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            positions
                        )
                    editPlayerPos.setAdapter(adapter)
                } else {
                    positions.add("Sin posición")
                    val adapter =
                        ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            positions
                        )
                    editPlayerPos.setAdapter(adapter)
                }
            }
        }
    }

    private fun setSpinner(pos: String) {
        lifecycleScope.launch {
            DBAccess.getPositions().collect { positions ->
                for ((index, item) in positions.withIndex()) {
                    if (item.contains(pos)) {
                        editPlayerPos.setSelection(index)
                        selecPos = pos
                    }
                }
            }
        }
    }

    private fun setChecked(sex: String) {
        when (sex) {
            in UserSex.MALE.toString() -> {
                rbMascEdit.isChecked = true
            }

            in UserSex.FEMALE.toString() -> {
                rbFemEdit.isChecked = true
            }

            in UserSex.OTHER.toString() -> {
                rbOtherEdit.isChecked = true
            }
        }
    }

    private fun logout() {
        sp = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.clear().apply()

        val intent = Intent(this@EditPlayerProfile.requireContext(), Login::class.java)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            requireContext(),
            R.anim.transition_fade_in,
            R.anim.transition_fade_out
        )
        startActivity(intent, options.toBundle())
        requireActivity().finish()
    }

    private fun setGoBack() {
        editPlayerBack.setOnClickListener {
            (activity as MainActivity).changeFragmentTransition(PlayerProfile())
        }
    }

    private fun setValues() {
        lifecycleScope.launch {
            DBAdministration.getPlayer(userId!!)
                .collect { player ->
                    player?.let {
                        editPlayerName.setText(player.username)
                        editPlayerEmail.setText(player.email)
                        Glide.with(requireContext())
                            .asBitmap()
                            .load(player.profileImage)
                            .into(editPlayerImg)
                        editPlayerWeight.setText(player.weight)
                        editPlayerHeight.setText(player.height)
                        editPlayerBirthdate.setText(player.birthdate)
                        txtBirthdate = player.birthdate!!
                        editPlayerNumber.setText(player.number)
                        setChecked(player.sex!!)
                        setSpinner(player.position!!)

                        oldUserEmail = player.email!!
                        oldUserName = player.username
                        oldSex = player.sex
                        oldWeight = player.weight
                        oldHeight = player.height
                        oldDate = player.birthdate
                        oldNumber = player.number
                        oldPos = player.position
                    }
                }
        }
    }

    private fun accept(view: View) {
        acceptEditPlayer.setOnClickListener {
            val name = editPlayerName.text.toString().trim()
            val email = editPlayerEmail.text.toString().trim()
            val password = editPlayerPass.text.toString().trim()
            val newPass = editPlayerNewPass.text.toString().trim()
            val weight = editPlayerWeight.text.toString().trim()
            val height = editPlayerHeight.text.toString().trim()
            val number = editPlayerNumber.text.toString().trim()
            val passRegex = Regex("^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")
            val regEmail = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")

            if (rbMascEdit.isChecked) {
                playSex = UserSex.MALE.toString()
            } else if (rbFemEdit.isChecked) {
                playSex = UserSex.FEMALE.toString()
            } else if (rbOtherEdit.isChecked) {
                playSex = UserSex.OTHER.toString()
            }

            if (name.isEmpty()) {
                editPlayerName.error = getString(R.string.rellena_campos_obligatorios)
            }
            if (email.isEmpty()) {
                editPlayerEmail.error = getString(R.string.rellena_campos_obligatorios)
            }
            if (password.isEmpty()) {
                editPlayerPass.error = getString(R.string.rellena_campos_obligatorios)
            }
            if (weight.isEmpty()) {
                editPlayerWeight.error = getString(R.string.rellena_campos_obligatorios)
            }
            if (height.isEmpty()) {
                editPlayerHeight.error = getString(R.string.rellena_campos_obligatorios)
            }
            if (number.isEmpty()) {
                editPlayerNumber.error = getString(R.string.rellena_campos_obligatorios)
            }

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && weight.isNotEmpty() && height.isNotEmpty() && number.isNotEmpty() && txtBirthdate.isNotEmpty() && selecPos.isNotEmpty()) {
                if (name.length < 5) {
                    editPlayerName.error = "El nombre debe contener mínimo 5 caracteres."
                }
                if (!email.matches(regEmail)) {
                    editPlayerEmail.error = getString(R.string.email_invalido)
                }
                if (!password.matches(passRegex)) {
                    editPlayerPass.error =
                        "La contraseña debe contener al menos 1 dígito, 1 caracter alfabético y 8 caracteres totales."
                }
                if (newPass.isNotEmpty() && !newPass.matches(passRegex)) {
                    editPlayerNewPass.error =
                        "La contraseña debe contener al menos 1 dígito, 1 caracter alfabético y 8 caracteres totales."
                }
                if (name.length >= 5 && email.matches(regEmail) && password.matches(passRegex)) {
                    //comprobar si existen ya
                    if (imgChosen == null && name == oldUserName && email == oldUserEmail && newPass.isEmpty() && oldNumber == number && oldSex == playSex && oldWeight == weight && oldHeight == height && oldPos == selecPos && oldDate == txtBirthdate) {
                        Snackbar.make(
                            ctw,
                            view,
                            "No se ha detectado ninguna actualización para el perfil",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (newPass.isNotEmpty() && !newPass.matches(passRegex)) {
                            editPlayerNewPass.error =
                                "La contraseña debe contener al menos 1 dígito, 1 caracter alfabético y 8 caracteres totales."
                        } else {
                            val oldEncryptedPassword =
                                DBAccess.encryptPassword(password)

                            if (email != oldUserEmail) {
                                DBAccess.checkEmailExists(email) { exists ->
                                    if (exists) {
                                        editPlayerEmail.error =
                                            "El email ya está en uso."
                                    } else {
                                        DBAccess.checkUsernameExistsEdit(name, oldUserEmail) { exists ->
                                            if (exists) {
                                                editPlayerName.error = "El nombre ya está en uso."
                                            } else {
                                                edit(
                                                    number,
                                                    oldEncryptedPassword!!,
                                                    newPass,
                                                    email,
                                                    name,
                                                    weight,
                                                    height,
                                                    view
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                DBAccess.checkUsernameExistsEdit(name, oldUserEmail) { exists ->
                                    if (exists) {
                                        editPlayerName.error = "El nombre ya está en uso."
                                    } else {
                                        edit(
                                            number,
                                            oldEncryptedPassword!!,
                                            newPass,
                                            email,
                                            name,
                                            weight,
                                            height,
                                            view
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun edit(
        number: String,
        oldEncryptedPassword: String,
        newPass: String,
        email: String,
        name: String,
        weight: String,
        height: String,
        view: View
    ) {
        DBAccess.checkNumber(number, oldUserEmail) { numExists ->
            if (numExists) {
                finalComprobations(number,
                    oldEncryptedPassword,
                    newPass,
                    email,
                    name,
                    weight,
                    height,
                    view)
            } else {
                DBAccess.checkNumberExists(number) { numberExists ->
                    if (numberExists) {
                        editPlayerNumber.error =
                            "El dorsal ya está en uso."
                    } else {
                        finalComprobations(number,
                            oldEncryptedPassword,
                            newPass,
                            email,
                            name,
                            weight,
                            height,
                            view)
                    }
                }
            }
        }
    }

    private fun finalComprobations(
        number: String,
        oldEncryptedPassword: String,
        newPass: String,
        email: String,
        name: String,
        weight: String,
        height: String,
        view: View
    ) {
        DBAccess.checkPassword(
            oldUserEmail,
            oldEncryptedPassword
        ) { correct ->
            if (correct) {
                var newPassword = ""
                if (newPass.isNotEmpty()) {
                    val newEncryptedPassword =
                        DBAccess.encryptPassword(
                            newPass
                        )
                    newPassword =
                        newEncryptedPassword!!
                }

                //comprobar si coinciden las contraseñas
                if (newPass.isNotEmpty() && newPassword == oldEncryptedPassword) {
                    editPlayerNewPass.error =
                        "La nueva contraseña debe ser distinta a la anterior."
                } else {
                    DBAccess.updatePlayerProfile(
                        email,
                        name,
                        newPassword,
                        weight,
                        height,
                        selecPos,
                        txtBirthdate,
                        number,
                        playSex,
                        userId!!
                    )
                    if (imgChosen != null) {
                        uploadImage(
                            imgChosen!!,
                            userId!!
                        )
                    }
                    Snackbar.make(
                        ctw,
                        view,
                        "Su perfil ha sido actualizado",
                        Toast.LENGTH_SHORT
                    ).show()
                    (activity as MainActivity).changeFragmentTransition(
                        PlayerProfile()
                    )
                }
            } else {
                editPlayerPass.error =
                    "La contraseña es incorrecta"
            }
        }
    }

    fun deletePlayer(rootView: View) {
        deletePlayer.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_account, null)
            val cancelDelete = view.findViewById<Button>(R.id.cancelDelete)
            val acceptDelete = view.findViewById<Button>(R.id.acceptDelete)
            val passwordDelete = view.findViewById<EditText>(R.id.passwordDelete)

            cancelDelete.setOnClickListener { dialog.dismiss() }
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)

            acceptDelete.setOnClickListener {
                val pass = passwordDelete.text.toString().trim()
                if (pass.isEmpty()) {
                    passwordDelete.error = "Rellena este campo."
                } else {
                    val passwordEncrypted = DBAccess.encryptPassword(pass)
                    DBAccess.checkPassword(oldUserEmail!!, passwordEncrypted!!) { correct ->
                        if (correct) {
                            DBAccess.deleteUser(oldUserEmail!!) { deleted ->
                                if (deleted) {
                                    Snackbar.make(
                                        ctw,
                                        rootView,
                                        "Usuario eliminado con éxito",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Snackbar.make(
                                        ctw,
                                        rootView,
                                        "El usuario no se pudo eliminar",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                logout()
                                dialog.dismiss()
                            }
                        } else {
                            passwordDelete.error = "La contraseña es incorrecta"
                        }
                    }
                }
            }
            dialog.show()
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

    private fun uploadImage(imgUri: Uri, idUser: String) {
        DBAccess.uploadImage(imgUri, idUser)
    }

    private fun listenerImage() {
        editPlayerImg.setOnClickListener {
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
                    editPlayerImg.setImageURI(imgChosen)
                }
            }
        }
}