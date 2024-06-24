package com.example.tfgkotlin.fragments

import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.activities.MainActivity
import com.example.tfgkotlin.adapters.PlayerAdapter
import com.example.tfgkotlin.adapters.PostAdapter
import com.example.tfgkotlin.dbmanager.DBAccess
import com.example.tfgkotlin.dbmanager.DBAdministration
import com.example.tfgkotlin.dbmanager.DBPost
import com.example.tfgkotlin.model.OpenGallery
import com.example.tfgkotlin.objects.Player
import com.example.tfgkotlin.objects.UserType
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Home : Fragment() {
    private lateinit var optionsHome: ImageView
    private lateinit var imgPostLay: LinearLayout
    private lateinit var imgPost: ImageView
    private lateinit var svPlayers: EditText
    private lateinit var txtNoPlayersFound: TextView
    private lateinit var rvPlayers: RecyclerView
    private lateinit var rvPosts: RecyclerView
    private lateinit var playersList: List<Player>
    private lateinit var dialog: AlertDialog
    private var userId = ""
    private var userType = ""
    private var imgChosen: Uri? = null
    private lateinit var ctw: ContextThemeWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        setAttributes(view)

        getPlayers()
        searchPlayer()
        txtNoPlayersFound.visibility = View.GONE
        setPosts(view)
        options(view)

        return view
    }

    private fun setAttributes(view: View) {
        ctw = ContextThemeWrapper(requireContext(), R.style.CustomSnackbarTheme)
        userId = (activity as MainActivity).getUserId()
        optionsHome = view.findViewById(R.id.optionsHome)
        rvPlayers = view.findViewById(R.id.rvPlayers)
        rvPosts = view.findViewById(R.id.rvPosts)
        svPlayers = view.findViewById(R.id.svPlayers)
        txtNoPlayersFound = view.findViewById(R.id.txtNoPlayersFound)
    }

    private fun searchPlayer() {
        svPlayers.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Por defecto
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                filterUsers(searchText)
            }

            override fun afterTextChanged(s: Editable?) {
                // Por defecto
            }
        })
    }

    fun setPosts(view: View) {
        DBPost.getPosts { posts ->
            lifecycleScope.launch {
                DBAccess.getUserType(userId).collect { user ->
                    if (user == UserType.ADMIN.toString()) {
                        userType = UserType.ADMIN.toString()
                    } else {
                        userType = UserType.PLAYER.toString()
                    }
                    val layoutManager =
                        LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                    rvPosts.setLayoutManager(layoutManager)
                    val postAdapter = PostAdapter(posts.sortedByDescending {
                        it.date?.let { date ->
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)
                        }
                    }.toMutableList(), userType, requireContext(), view)
                    rvPosts.setAdapter(postAdapter)
                }
            }
        }
    }

    private fun filterUsers(text: String) {
        val filteredList = playersList.filter { player ->
            player.username!!.contains(text, ignoreCase = true)
        }
        setPlayersAdapter(filteredList)
    }

    private fun getPlayers() {
        lifecycleScope.launch {
            DBAdministration.getPlayersActive()
                .collect { players ->
                    playersList = players
                    setPlayersAdapter(playersList)
                }
        }
    }

    private fun setPlayersAdapter(list: List<Player>) {
        if (list.isEmpty()) {
            txtNoPlayersFound.visibility = View.VISIBLE
        } else {
            txtNoPlayersFound.visibility = View.GONE
        }
        val layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        rvPlayers.setLayoutManager(layoutManager)
        val playerAdapter = PlayerAdapter(list, requireContext())
        rvPlayers.setAdapter(playerAdapter)
    }

    private fun options(rootView: View) {
        optionsHome.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_posts, null)
            val add = view.findViewById<CardView>(R.id.cardAddPost)
            val viewPosts = view.findViewById<CardView>(R.id.cardViewPosts)
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            dialog = builder.create()

            add.setOnClickListener {
                dialog.dismiss()
                addPost(rootView)
            }

            viewPosts.setOnClickListener {
                dialog.dismiss()
                (activity as MainActivity).changeFragmentFade(ViewPosts())
            }

            dialog.show()
        }
    }

    private fun addPost(rootView: View) {
        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_post, null)
        val cancelAdd = view.findViewById<Button>(R.id.cancelAddPost)
        val acceptAdd = view.findViewById<Button>(R.id.acceptAddPost)
        val name = view.findViewById<TextView>(R.id.namePost)
        val imgProfile = view.findViewById<ImageView>(R.id.imgProfilePost)
        val datePost = view.findViewById<TextView>(R.id.datePost)
        val descPost = view.findViewById<EditText>(R.id.descPost)
        imgPostLay = view.findViewById(R.id.imgPostLay)
        imgPost = view.findViewById(R.id.imgPost)

        val today = Date()
        val format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val formatedToday = format.format(today)
        val txtDate = formatedToday.split(" ")[0]

        imgPostLay.setOnClickListener {
            OpenGallery.openGallery(requireActivity(), galleryLauncher)
        }

        DBAccess.getUserLogged(userId) { user ->
            Glide.with(requireContext())
                .asBitmap()
                .load(user!!.profileImage)
                .into(imgProfile)
            name.text = user.username
        }

        datePost.text = txtDate

        cancelAdd.setOnClickListener { dialog.dismiss() }
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(view)
        dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)

        acceptAdd.setOnClickListener {
            DBPost.uploadPost(
                userId,
                descPost.text.toString(),
                formatedToday
            ) { added, postId ->
                if (added) {
                    uploadImage(imgChosen!!, postId!!, view)
                    Snackbar.make(
                        ctw,
                        rootView,
                        "Post publicado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                } else {
                    Snackbar.make(
                        ctw,
                        view,
                        "No se pudo realizar la publicación",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun uploadImage(imgUri: Uri, idPost: String, view: View) {
        DBPost.uploadImage(imgUri, idPost) { added ->
            if (added) {
                setPosts(view)
            }
        }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                imgChosen = result.data?.data
                if (imgChosen != null) {
                    imgPost.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                    imgPost.layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
                    imgPost.requestLayout()
                    imgPost.setImageURI(imgChosen)
                }
            }
        }
}