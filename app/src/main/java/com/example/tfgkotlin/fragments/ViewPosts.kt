package com.example.tfgkotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.activities.MainActivity
import com.example.tfgkotlin.adapters.ProfilePostAdapter
import com.example.tfgkotlin.dbmanager.DBAccess
import com.example.tfgkotlin.dbmanager.DBPost

class ViewPosts : Fragment() {

    private lateinit var rvProfilePosts: RecyclerView
    private lateinit var imgProfilePosts: ImageView
    private lateinit var backPosts: ImageView
    private lateinit var usernamePosts: TextView
    private lateinit var numberPosts: TextView
    private lateinit var txtNoPosts: TextView
    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_posts, container, false)

        setAttributes(view)
        setPosts(view)
        setBack()
        return view
    }

    private fun setAttributes(view: View) {
        rvProfilePosts = view.findViewById(R.id.rvProfilePosts)
        imgProfilePosts = view.findViewById(R.id.imgProfilePosts)
        usernamePosts = view.findViewById(R.id.usernamePosts)
        numberPosts = view.findViewById(R.id.numberPosts)
        userId = (activity as MainActivity).getUserId()
        txtNoPosts = view.findViewById(R.id.txtNoPosts)
        backPosts = view.findViewById(R.id.backPosts)
    }

    private fun setBack() {
        backPosts.setOnClickListener {
            (activity as MainActivity).changeFragmentFade(Home())
        }
    }

    private fun setPosts(view: View) {
        DBAccess.getUserLogged(userId) { user ->
            user?.let {
                Glide.with(requireContext())
                    .asBitmap()
                    .load(user.profileImage)
                    .into(imgProfilePosts)
                usernamePosts.text = user.username

                DBPost.getPostsId(userId) { posts ->
                    if (posts.isEmpty()) {
                        numberPosts.text = "0 publicaciones"
                        txtNoPosts.visibility = View.VISIBLE
                    } else {
                        txtNoPosts.visibility = View.GONE
                        numberPosts.text = "${posts.size} publicaciones"
                        rvProfilePosts.setLayoutManager(GridLayoutManager(context, 3))
                        val adapter = ProfilePostAdapter(posts, user, requireContext(), view)
                        rvProfilePosts.setAdapter(adapter)
                    }
                }
            }
        }
    }
}