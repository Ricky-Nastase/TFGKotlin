package com.example.tfgkotlin.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class EditAdminProfile : Fragment() {
    private var userId: String? = null
    private lateinit var editAdminImg: ImageView
    private lateinit var dialog: AlertDialog
    private lateinit var deleteAdmin: LinearLayout
    private lateinit var acceptEditAdmin: Button
    private lateinit var editAdminBack: ImageView
    private lateinit var editAdminName: EditText
    private lateinit var editAdminEmail: EditText
    private lateinit var editAdminPass: EditText
    private lateinit var newAdminPass: EditText
    private var oldUserEmail = ""
    private lateinit var sp: SharedPreferences
    private var oldUserName: String? = null
    private lateinit var anim: Animation
    private var imgChosen: Uri? = null
    private lateinit var ctw: ContextThemeWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_admin_profile, container, false)

        setAttributes(view)
        setValues()
        setGoBack()
        listenerImage()
        accept(view)
        deleteAdmin(view)

        return view
    }

    private fun setAttributes(view: View) {
        acceptEditAdmin = view.findViewById(R.id.acceptEditAdmin)
        editAdminBack = view.findViewById(R.id.editAdminBack)
        editAdminImg = view.findViewById(R.id.editAdminImg)
        editAdminName = view.findViewById(R.id.editAdminName)
        deleteAdmin = view.findViewById(R.id.deleteAdmin)
        ctw = ContextThemeWrapper(requireContext(), R.style.CustomSnackbarTheme)
        editAdminEmail = view.findViewById(R.id.editAdminEmail)
        editAdminPass = view.findViewById(R.id.editAdminPass)
        newAdminPass = view.findViewById(R.id.newAdminPass)
        anim = AnimationUtils.loadAnimation(context, R.anim.escalate)
        userId = (activity as MainActivity).getUserId()
    }

    private fun logout() {
        sp = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.clear().apply()

        val intent = Intent(this@EditAdminProfile.requireContext(), Login::class.java)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            requireContext(),
            R.anim.transition_fade_in,
            R.anim.transition_fade_out
        )
        startActivity(intent, options.toBundle())
        requireActivity().finish()
    }

    private fun setValues() {
        lifecycleScope.launch {
            DBAdministration.getAdmin(userId!!)
                .collect { admin ->
                    admin?.let {
                        editAdminName.setText(admin.username)
                        editAdminEmail.setText(admin.email)
                        oldUserEmail = admin.email!!
                        oldUserName = admin.username
                        Glide.with(requireContext())
                            .asBitmap()
                            .load(admin.profileImage)
                            .into(editAdminImg)
                    }
                }
        }
    }

    fun deleteAdmin(rootView: View) {
        deleteAdmin.setOnClickListener {
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
                            passwordDelete.error = "La contraseña es incorrecta."
                        }
                    }
                }
            }
            dialog.show()
        }
    }

    private fun uploadImage(imgUri: Uri, idUser: String) {
        DBAccess.uploadImage(imgUri, idUser)
    }

    private fun setGoBack() {
        editAdminBack.setOnClickListener {
            (activity as MainActivity).changeFragmentTransition(Administration())
        }
    }

    private fun accept(view: View) {
        acceptEditAdmin.setOnClickListener {
            val name = editAdminName.text.toString().trim()
            val email = editAdminEmail.text.toString().trim()
            val oldPass = editAdminPass.text.toString().trim()
            val newPass = newAdminPass.text.toString().trim()
            val passRegex = Regex("^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")
            val regEmail = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")

            if (name.isEmpty()) {
                editAdminName.error = getString(R.string.rellena_campos_obligatorios)
            }
            if (email.isEmpty()) {
                editAdminEmail.error = getString(R.string.rellena_campos_obligatorios)
            }
            if (oldPass.isEmpty()) {
                editAdminPass.error = getString(R.string.rellena_campos_obligatorios)
            }

            if (name.isNotEmpty() && email.isNotEmpty() && oldPass.isNotEmpty()) {
                if (name.length < 5) {
                    editAdminName.error = "El nombre debe contener mínimo 5 caracteres."
                }
                if (!email.matches(regEmail)) {
                    editAdminEmail.error = getString(R.string.email_invalido)
                }
                if (!oldPass.matches(passRegex)) {
                    editAdminPass.error =
                        "La contraseña debe contener al menos 1 dígito, 1 caracter alfabético y 8 caracteres totales."
                }
                if (newPass.isNotEmpty() && !newPass.matches(passRegex)) {
                    newAdminPass.error =
                        "La contraseña debe contener al menos 1 dígito, 1 caracter alfabético y 8 caracteres totales."
                }
                if (name.length >= 5 && email.matches(regEmail) && oldPass.matches(passRegex)) {
                    //comprobar si existen ya
                    if (imgChosen == null && name == oldUserName && email == oldUserEmail && newPass.isEmpty()) {
                        Snackbar.make(
                            ctw,
                            view,
                            "No se ha detectado ninguna actualización para el perfil",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (newPass.isNotEmpty() && !newPass.matches(passRegex)) {
                            newAdminPass.error =
                                "La contraseña debe contener al menos 1 dígito, 1 caracter alfabético y 8 caracteres totales."
                        } else {
                            val oldEncryptedPassword =
                                DBAccess.encryptPassword(oldPass)

                            if (email != oldUserEmail) {
                                DBAccess.checkEmailExists(email) { exists ->
                                    if (exists) {
                                        editAdminEmail.error =
                                            "El email ya está en uso."
                                    } else {
                                        DBAccess.checkUsernameExistsEdit(
                                            name,
                                            oldUserEmail
                                        ) { exists ->
                                            if (exists) {
                                                editAdminName.error = "El nombre ya está en uso."
                                            } else {
                                                finalEdit(
                                                    oldEncryptedPassword!!,
                                                    newPass,
                                                    name,
                                                    email,
                                                    view
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                DBAccess.checkUsernameExistsEdit(name, oldUserEmail) { exists ->
                                    if (exists) {
                                        editAdminName.error = "El nombre ya está en uso."
                                    } else {
                                        finalEdit(
                                            oldEncryptedPassword!!,
                                            newPass,
                                            name,
                                            email,
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

    private fun finalEdit(
        oldEncryptedPassword: String,
        newPass: String,
        name: String,
        email: String,
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
                        DBAccess.encryptPassword(newPass)
                    newPassword = newEncryptedPassword!!
                }

                //comprobar si coinciden las contraseñas
                if (newPass.isNotEmpty() && newPassword == oldEncryptedPassword) {
                    newAdminPass.error =
                        "La nueva contraseña debe ser distinta a la anterior."
                } else {
                    DBAccess.updateAdminProfile(
                        name,
                        email,
                        newPassword,
                        userId!!
                    )
                    if (imgChosen != null) {
                        uploadImage(imgChosen!!, userId!!)
                    }
                    Snackbar.make(
                        ctw,
                        view,
                        "Su perfil ha sido actualizado",
                        Toast.LENGTH_SHORT
                    ).show()
                    (activity as MainActivity).changeFragmentTransition(
                        Administration()
                    )
                }
            } else {
                editAdminPass.error = "La contraseña es incorrecta."
            }
        }
    }

    private fun listenerImage() {
        editAdminImg.setOnClickListener {
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
                    editAdminImg.setImageURI(imgChosen)
                }
            }
        }
}