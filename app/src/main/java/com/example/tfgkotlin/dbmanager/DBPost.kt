package com.example.tfgkotlin.dbmanager

import android.net.Uri
import com.example.tfgkotlin.objects.Administrator
import com.example.tfgkotlin.objects.Post
import com.example.tfgkotlin.objects.Team
import com.example.tfgkotlin.objects.UserType
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

object DBPost {
    val database = FirebaseDatabase.getInstance()
    val refPosts = database.getReference("posts")
    val storageRef = FirebaseStorage.getInstance().reference
    private val databaseReference = FirebaseDatabase.getInstance().reference

    fun uploadPost(
        userId: String,
        description: String,
        date: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val postId = refPosts.push().key

        val post =
            Post(postId, userId, description, date)

        refPosts.child(postId!!).setValue(post)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, postId)
                } else {
                    callback(false, null)
                }
            }

    }

    fun deletePost(id: String, callback: (Boolean) -> Unit) {
        refPosts.child(id).removeValue().addOnSuccessListener {
            callback(true)
        }
    }

    fun setImgPost(imgUrl: String, idPost: String, callback: (Boolean) -> Unit) {
        val refPost = databaseReference.child("posts").child(idPost)
        refPost.child("postImage").setValue(imgUrl).addOnSuccessListener {
            callback(true)
        }
    }

    fun uploadImage(imgUri: Uri, idPost: String, callback: (Boolean) -> Unit) {
        val route = storageRef.child("postImages").child(imgUri.lastPathSegment!!)
        route.putFile(imgUri)
            .addOnSuccessListener {
                route.getDownloadUrl().addOnSuccessListener { uri ->
                    setImgPost(uri.toString(), idPost) { added ->
                        if (added) {
                            callback(true)
                        }
                    }
                }
            }
    }

    fun getPosts(callback: (MutableList<Post>) -> Unit) {
        refPosts.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    post?.let {
                        posts.add(post)
                    }
                }
                callback(posts)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun getPostsId(userId: String, callback: (MutableList<Post>) -> Unit) {
        refPosts.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val posts = mutableListOf<Post>()
                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(Post::class.java)
                        post?.let {
                            posts.add(post)
                        }
                    }
                    callback(posts)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}