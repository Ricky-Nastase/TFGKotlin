package com.example.tfgkotlin.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.dbmanager.DBAccess
import com.example.tfgkotlin.dbmanager.DBPost
import com.example.tfgkotlin.objects.Player
import com.example.tfgkotlin.objects.Post
import com.example.tfgkotlin.objects.UserType
import com.google.android.material.snackbar.Snackbar
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class PostAdapter(
    private var postsList: MutableList<Post>,
    private val userType: String,
    private val context: Context,
    private val rootView: View
) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private lateinit var dialog: AlertDialog
    private lateinit var ctw: ContextThemeWrapper

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false)
        ctw = ContextThemeWrapper(context, R.style.CustomSnackbarTheme)
        return PostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postsList[position]

        DBAccess.getUserLogged(post.userId!!) { user ->
            user?.let {
                Glide.with(holder.itemView.context)
                    .asBitmap()
                    .load(user.profileImage)
                    .into(holder.postImgProfile)
                Glide.with(holder.itemView.context)
                    .asBitmap()
                    .load(post.postImage)
                    .into(holder.postImage)
                holder.name.text = user.username
                val txtDate = post.date!!.split(" ")
                holder.postDate.text = txtDate[0]
                if (post.description!!.isEmpty()) {
                    holder.postDesc.visibility = View.GONE
                } else {
                    holder.postDesc.visibility = View.VISIBLE
                    holder.postDesc.text = post.description
                }
            }
        }

        when (userType) {
            UserType.ADMIN.toString() -> {
                holder.deletePost.visibility = View.VISIBLE
                deletePost(holder, post, position)
            }

            UserType.PLAYER.toString() -> holder.deletePost.visibility = View.GONE
        }
    }

    private fun deletePost(holder: PostViewHolder, post: Post, position: Int) {
        holder.deletePost.setOnClickListener {
            val view =
                LayoutInflater.from(context).inflate(R.layout.dialog_delete_post, null)
            val cancel = view.findViewById<Button>(R.id.cancelDeletePost)
            val accept = view.findViewById<Button>(R.id.deletePost)

            val builder = AlertDialog.Builder(context)
            builder.setView(view)
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
            cancel.setOnClickListener { dialog.dismiss() }

            accept.setOnClickListener {
                DBPost.deletePost(post.id!!) { deleted ->
                    if (deleted) {
                        postsList.removeAt(position)
                        notifyItemRemoved(position)
                        dialog.dismiss()
                        Snackbar.make(
                            ctw,
                            rootView,
                            "Publicación eliminada",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        dialog.dismiss()
                        Snackbar.make(
                            ctw,
                            rootView,
                            "No se puedo eliminar la publicación",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            dialog.show()
        }
    }

    override fun getItemCount(): Int {
        return postsList.size
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImgProfile: CircleImageView = itemView.findViewById(R.id.postImgProfile)
        val name: TextView = itemView.findViewById(R.id.postUsername)
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val postDate: TextView = itemView.findViewById(R.id.postDate)
        val postDesc: TextView = itemView.findViewById(R.id.postDesc)
        val deletePost: ImageView = itemView.findViewById(R.id.deletePost)
    }
}