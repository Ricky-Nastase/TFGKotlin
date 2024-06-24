package com.example.tfgkotlin.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.activities.MainActivity
import com.example.tfgkotlin.dbmanager.DBPost
import com.example.tfgkotlin.fragments.Administration
import com.example.tfgkotlin.fragments.PlayerProfile
import com.example.tfgkotlin.fragments.ViewPosts
import com.example.tfgkotlin.objects.Administrator
import com.example.tfgkotlin.objects.Player
import com.example.tfgkotlin.objects.Post
import com.google.android.material.snackbar.Snackbar

class ProfilePostAdapter(
    private val posts: MutableList<Post>,
    private val user: Administrator,
    private val context: Context,
    private val rootView: View
) :
    RecyclerView.Adapter<ProfilePostAdapter.ProfilePostViewHolder>() {

    private lateinit var dialog: AlertDialog
    private lateinit var ctw: ContextThemeWrapper

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProfilePostViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.profile_post, parent, false)
        ctw = ContextThemeWrapper(context, R.style.CustomSnackbarTheme)
        return ProfilePostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfilePostViewHolder, position: Int) {
        val post = posts[position]
        Glide.with(holder.itemView.context)
            .asBitmap()
            .load(post.postImage)
            .into(holder.image)
        imageListener(holder, post, position)
    }

    private fun imageListener(holder: ProfilePostViewHolder, post: Post, position: Int) {
        holder.image.setOnClickListener {
            val view =
                LayoutInflater.from(holder.itemView.context)
                    .inflate(R.layout.dialog_profile_post, null)
            val delete = view.findViewById<ImageView>(R.id.deletePost)
            val postImgProfile = view.findViewById<ImageView>(R.id.postImgProfile)
            val postUsername = view.findViewById<TextView>(R.id.postUsername)
            val postDate = view.findViewById<TextView>(R.id.postDate)
            val postDesc = view.findViewById<TextView>(R.id.postDesc)
            val postImage = view.findViewById<ImageView>(R.id.postImage)

            Glide.with(holder.itemView.context)
                .asBitmap()
                .load(post.postImage)
                .into(postImage)

            Glide.with(holder.itemView.context)
                .asBitmap()
                .load(user.profileImage)
                .into(postImgProfile)

            postUsername.text = user.username
            postDate.text = post.date!!.split(" ")[0]
            postDesc.text = post.description

            delete.visibility = View.VISIBLE

            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setView(view)
            dialog = builder.create()

            val txtNoPosts: TextView = rootView.findViewById(R.id.txtNoPosts)
            val numberPosts: TextView = rootView.findViewById(R.id.numberPosts)

            delete.setOnClickListener {
                DBPost.deletePost(post.id!!) { deleted ->
                    dialog.dismiss()
                    posts.removeAt(position)

                    numberPosts.text = "${posts.size} publicaciones"
                    if (posts.size == 0) {
                        txtNoPosts.visibility = View.VISIBLE
                    } else {
                        txtNoPosts.visibility = View.GONE
                    }

                    notifyItemRemoved(position)
                    Snackbar.make(
                        ctw,
                        rootView,
                        "Publicación eliminada",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            dialog.show()
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    class ProfilePostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.postImageProfile)
    }
}