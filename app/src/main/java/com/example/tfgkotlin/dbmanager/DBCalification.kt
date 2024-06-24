package com.example.tfgkotlin.dbmanager

import com.example.tfgkotlin.objects.AllPlayerCalifications
import com.example.tfgkotlin.objects.Calification
import com.example.tfgkotlin.objects.Califications
import com.example.tfgkotlin.objects.Player
import com.example.tfgkotlin.objects.PlayerCalification
import com.example.tfgkotlin.objects.UserType
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object DBCalification {
    val database = FirebaseDatabase.getInstance()
    val refTrainings = database.getReference("trainings")
    val refUsers = database.getReference("users")
    val refCalifications = database.getReference("califications")

    fun getTrainingsAssisted(userId: String, callback: (MutableList<String>) -> Unit) {
        refTrainings.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var trainings = mutableListOf<String>()
                for (data in snapshot.children) {
                    val assistants = data.child("assistants")
                    val training = data.child("date").getValue(String::class.java)
                    for (ids in assistants.children) {
                        val id = ids.value
                        if (id == userId) {
                            trainings.add(training!!)
                        }
                    }
                }
                println(trainings)
                callback(trainings)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun checkTrainingExists(date: String, callback: (Boolean, String?) -> Unit) {
        refTrainings.orderByChild("date").equalTo(date)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (trainingSnapshot in snapshot.children) {
                            val id = trainingSnapshot.key
                            callback(true, id)
                        }
                    } else {
                        callback(false, null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getCalifications(callback: (MutableList<Califications>) -> Unit) {
        var califications = mutableListOf<Califications>()
        refCalifications.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    var calificationsList = mutableListOf<Calification>()
                    val calif = snapshot.child("califications").children

                    for (c in calif) {
                        val califName = c.key

                        val calificationsPlayer = c.value as? Map<String, String>

                        if (calificationsPlayer == null) continue

                        val playerCalificationsList = mutableListOf<PlayerCalification>()

                        for ((playerName, calificationValue) in calificationsPlayer) {
                            playerCalificationsList.add(
                                PlayerCalification(
                                    playerName,
                                    calificationValue
                                )
                            )
                        }

                        calificationsList.add(
                            Calification(
                                califName,
                                false,
                                playerCalificationsList
                            )
                        )
                    }
                    val trainindId = snapshot.child("training").getValue(String::class.java)
                    califications.add(Califications(trainindId, calificationsList))
                }
                callback(califications)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun getPlayerCalifications(
        training: String,
        player: String,
        callback: (MutableList<PlayerCalification>?) -> Unit
    ) {
        refCalifications
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var playerCalifications = mutableListOf<PlayerCalification>()
                    for (data in snapshot.children) {
                        val tr = data.child("date").getValue(String::class.java)
                        if (tr == training) {
                            for (califs in data.child("califications").children) {
                                for (values in califs.children) {
                                    if (values.key == player) {
                                        playerCalifications.add(
                                            PlayerCalification(
                                                califs.key,
                                                player,
                                                values.value.toString()
                                            )
                                        )
                                    }
                                }
                            }
                        }

                    }
                    callback(playerCalifications)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun calculateTotals(calif: MutableList<PlayerCalification>): Double {
        val sum = mutableMapOf<Pair<String, String>, Double>()
        val count = mutableMapOf<Pair<String, String>, Int>()

        for (c in calif) {
            val key = Pair(c.name!!, c.userId!!)

            sum[key] = sum.getOrDefault(key, 0.0) + c.calification!!.toInt()
            count[key] = count.getOrDefault(key, 0) + 1
        }

        val averageCalif = mutableListOf<AllPlayerCalifications>()
        for ((key, total) in sum) {
            val numCount = count[key] ?: 1
            val average = total / numCount
            averageCalif.add(AllPlayerCalifications(key.second, key.first, average.toString()))
        }

        var totalCalif = 0.0

        for (total in averageCalif) {
            totalCalif += total.calification!!.toDouble()
        }

        return totalCalif
    }

    fun getTotals(players: List<Player>, callback: (MutableList<AllPlayerCalifications>) -> Unit) {
        val list = mutableListOf<AllPlayerCalifications>()
        refCalifications
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (p in players) {
                        var playerCalifications = mutableListOf<PlayerCalification>()
                        for (data in snapshot.children) {
                            for (califs in data.child("califications").children) {
                                for (values in califs.children) {
                                    if (values.key == p.id) {
                                        playerCalifications.add(
                                            PlayerCalification(
                                                califs.key,
                                                p.id,
                                                values.value.toString(),
                                            )
                                        )
                                    }
                                }
                            }

                        }
                        if (playerCalifications.isEmpty()) {
                            list.add(
                                AllPlayerCalifications(
                                    p.id,
                                    p.username,
                                    0.toString(),
                                    p.profileImage
                                )
                            )
                        } else {
                            list.add(
                                AllPlayerCalifications(
                                    p.id,
                                    p.username,
                                    calculateTotals(playerCalifications).toString(),
                                    p.profileImage
                                )
                            )
                        }
                    }
                    callback(list)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getAllPlayerCalifications(
        player: String,
        callback: (MutableList<PlayerCalification>?) -> Unit
    ) {
        refCalifications
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var playerCalifications = mutableListOf<PlayerCalification>()
                    for (data in snapshot.children) {
                        for (califs in data.child("califications").children) {
                            for (values in califs.children) {
                                if (values.key == player) {
                                    playerCalifications.add(
                                        PlayerCalification(
                                            califs.key,
                                            player,
                                            values.value.toString()
                                        )
                                    )
                                }
                            }
                        }

                    }
                    callback(playerCalifications)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun checkTrainingCalificated(id: String, callback: (Boolean) -> Unit) {
        refCalifications.orderByChild("training").equalTo(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (trainingSnapshot in snapshot.children) {
                            callback(true)
                        }
                    } else {
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getPlayersTraining(
        idTraining: String,
        callback: (MutableList<PlayerCalification>) -> Unit
    ) {
        refTrainings.child(idTraining).child("assistants")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val usersId = mutableListOf<String>()
                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(String::class.java)
                        user?.let {
                            usersId.add(user)
                        }
                    }

                    refUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val users = mutableListOf<PlayerCalification>()
                            for (snapshot in dataSnapshot.children) {
                                val type = snapshot.child("type").getValue(String::class.java)
                                if (type == UserType.PLAYER.toString()) {
                                    val id = snapshot.key
                                    if (usersId.contains(id)) {
                                        val user = snapshot.getValue(Player::class.java)
                                        user?.let {
                                            users.add(
                                                PlayerCalification(
                                                    user.username,
                                                    user.id,
                                                    "0"
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                            callback(users)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
    }

    fun getCalificationId(idTraining: String, callback: (String) -> Unit) {
        refCalifications.orderByChild("training").equalTo(idTraining)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (calificationSnapshot in dataSnapshot.children) {
                        val calificationId = calificationSnapshot.key
                        callback(calificationId!!)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
    }

    fun setCalifications(
        califications: List<Calification>,
        trainingId: String,
        id: String? = null
    ) {
        var califId = ""
        if (id == null) {
            califId = refCalifications.push().key!!
        } else {
            califId = id
        }
        califId.let {
            califications.forEach { calification ->
                calification.players?.forEach { playerCalification ->
                    var number = playerCalification.calification!!.trim()

                    if (number.isEmpty()) {
                        number = 0.toString()
                    }

                    refCalifications.child(califId).child("califications")
                        .child(calification.name!!)
                        .child(playerCalification.userId!!)
                        .setValue(number)
                }
            }
            refCalifications.child(califId).child("training").setValue(trainingId)
                .addOnSuccessListener {
                    refTrainings.child(trainingId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                val date = dataSnapshot.child("date").getValue(String::class.java)
                                println(date)
                                refCalifications.child(califId).child("date").setValue(date)

                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                }
        }
    }

    fun deleteCalification(trainingId: String) {
        refCalifications.orderByChild("training").equalTo(trainingId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
    }

}