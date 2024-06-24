package com.example.tfgkotlin.dbmanager

import android.util.Log
import android.view.View
import com.example.tfgkotlin.objects.Administrator
import com.example.tfgkotlin.objects.Player
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.Flow

object DBCalendar {
    val database = FirebaseDatabase.getInstance()
    val refUsers = database.getReference("users")
    val refTrainings = database.getReference("trainings")

    fun checkExistsTraining(date: String, callback: (Boolean) -> Unit) {
        refTrainings.orderByChild("date").equalTo(date)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        callback(true)
                    } else {
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error $error")
                }
            })
    }

    fun getTraining(date: String): kotlinx.coroutines.flow.Flow<Map<String, Any>> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val dataMap = mutableMapOf<String, Any>()
                    var assistantsList = mutableListOf<String>()

                    val trainingSnapshot = dataSnapshot.children.first()
                    val assistantsSnapshot = trainingSnapshot.child("assistants")

                    for (assistantId in assistantsSnapshot.children) {
                        val id = assistantId.getValue(String::class.java)
                        id?.let {
                            assistantsList.add(it)
                        }
                    }

                    val observations =
                        trainingSnapshot.child("observations").getValue(String::class.java)

                    dataMap["assistants"] = assistantsList

                    observations?.let {
                        dataMap["observations"] = observations
                    }

                    trySend(dataMap).isSuccess
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                close(databaseError.toException())
            }
        }

        refTrainings.orderByChild("date").equalTo(date).addValueEventListener(valueEventListener)

        awaitClose {
            refTrainings.removeEventListener(valueEventListener)
        }
    }


    fun getAssistants(assistantsIds: MutableList<String>): kotlinx.coroutines.flow.Flow<MutableList<Player>> =
        callbackFlow {
            val assistants = mutableListOf<Player>()
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (playerSnapshot in dataSnapshot.children) {
                        val player = playerSnapshot.getValue(Player::class.java)
                        if (player != null) {
                            if (assistantsIds.contains(player.id)) {
                                assistants.add(player)
                            }
                        }
                    }
                    trySend(assistants).isSuccess
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    close(databaseError.toException())
                }
            }

            refUsers.addValueEventListener(valueEventListener)

            awaitClose {
                refUsers.removeEventListener(valueEventListener)
            }
        }

    fun removePlayer(date: String, userId: String, callback: (Boolean) -> Unit) {
        refTrainings.orderByChild("date").equalTo(date)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val trainingSnapshot = snapshot.children.first()
                        val refAssistants = trainingSnapshot.child("assistants").ref

                        refAssistants.orderByValue().equalTo(userId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        dataSnapshot.children.first().ref.removeValue()
                                            .addOnSuccessListener {
                                                callback(true)
                                            }
                                            .addOnFailureListener { exception ->
                                                callback(false)
                                            }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.d("tag", "Error: $databaseError")
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error: ${error.message}")
                }
            })
    }

    fun addPlayer(date: String, userId: String, callback: (Boolean) -> Unit) {
        refTrainings.orderByChild("date").equalTo(date)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val trainingSnapshot = snapshot.children.first()
                        val refAssistants = trainingSnapshot.child("assistants").ref

                        refAssistants.push().setValue(userId)
                            .addOnSuccessListener {
                                callback(true)
                            }
                            .addOnFailureListener { exception ->
                                callback(false)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error: ${error.message}")
                }
            })
    }

}