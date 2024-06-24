package com.example.tfgkotlin.dbmanager

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Toast
import com.example.tfgkotlin.objects.Administrator
import com.example.tfgkotlin.objects.Match
import com.example.tfgkotlin.objects.Player
import com.example.tfgkotlin.objects.StatusMatch
import com.example.tfgkotlin.objects.Team
import com.example.tfgkotlin.objects.UserType
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

object DBAdministration {
    val database = FirebaseDatabase.getInstance()
    val refUsers = database.getReference("users")
    val refCodes = database.getReference("codes")
    val refTrainings = database.getReference("trainings")
    val refTeams = database.getReference("teams")
    val refSeasons = database.getReference("seasons")
    val refMatches = database.getReference("matches")
    val refPositions = database.getReference("positions")
    val storageRef = FirebaseStorage.getInstance().reference
    private val databaseReference = FirebaseDatabase.getInstance().reference

    fun addTraining(
        ctw: ContextThemeWrapper,
        date: String,
        observations: String,
        view: View,
        rootView: View,
        callback: (Boolean) -> Unit
    ) {
        refTrainings.orderByChild("date").equalTo(date)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Snackbar.make(
                            ctw,
                            view,
                            "Ya existe entrenamiento este día",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val trainingId = refTrainings.push().key ?: ""
                        val training = hashMapOf(
                            "date" to date,
                            "observations" to observations
                        )
                        refTrainings.child(trainingId).setValue(training)
                            .addOnSuccessListener {
                                Snackbar.make(
                                    ctw,
                                    rootView,
                                    "Entrenamiento añadido",
                                    Toast.LENGTH_SHORT
                                ).show()
                                callback(true)
                            }
                            .addOnFailureListener { error ->
                                callback(false)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }

    fun deleteTraining(
        ctw: ContextThemeWrapper,
        date: String,
        view: View,
        rootView: View,
        callback: (Boolean, String?) -> Unit
    ) {
        refTrainings.orderByChild("date").equalTo(date)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val trainingSnapshot = snapshot.children.first()
                        val trainingId = trainingSnapshot.key

                        trainingId?.let {
                            refTrainings.child(it).removeValue()
                                .addOnSuccessListener {
                                    Snackbar.make(
                                        ctw,
                                        rootView,
                                        "Entrenamiento eliminado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    callback(true, trainingId)
                                }
                        }
                    } else {
                        Snackbar.make(
                            ctw,
                            view,
                            "No existe entrenamiento este día",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, null)
                }
            })
    }

    fun getPlayersActive(): Flow<List<Player>> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val playerList = mutableListOf<Player>()
                for (snapshot in dataSnapshot.children) {
                    val type = snapshot.child("type").getValue(String::class.java)
                    if (type == UserType.PLAYER.toString()) {
                        val player = snapshot.getValue(Player::class.java)
                        player?.let {
                            if (player.active!!) {
                                playerList.add(player)
                            }
                        }
                    }
                }
                trySend(playerList.toList()).isSuccess
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

    fun getSeasons(): Flow<List<String>> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val seasonList = mutableListOf<String>()
                for (snapshot in dataSnapshot.children) {
                    val season = snapshot.getValue(String::class.java)
                    season?.let {
                        seasonList.add(season)
                    }
                }
                trySend(seasonList.toList()).isSuccess
            }

            override fun onCancelled(databaseError: DatabaseError) {
                close(databaseError.toException())
            }
        }

        refSeasons.addValueEventListener(valueEventListener)

        awaitClose {
            refSeasons.removeEventListener(valueEventListener)
        }
    }

    fun addTeam(image: Uri, name: String, callback: (Boolean) -> Unit) {
        val route = storageRef.child("profileImages").child(image.lastPathSegment!!)

        route.putFile(image)
            .addOnSuccessListener {
                route.getDownloadUrl().addOnSuccessListener { uri ->
                    refTeams.orderByChild("name").equalTo(name)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (userSnapshot in snapshot.children) {
                                        val teamId = userSnapshot.key
                                        if (teamId != null) {
                                            val updateTeamMap = mapOf<String, Any>(
                                                "image" to uri.toString(),
                                                "active" to true
                                            )
                                            refTeams.child(teamId)
                                                .updateChildren(updateTeamMap)
                                                .addOnSuccessListener {
                                                    callback(true)
                                                }
                                                .addOnFailureListener { e ->
                                                    callback(false)
                                                }
                                        }
                                    }
                                } else {
                                    val teamId = refTeams.push().key

                                    teamId?.let {
                                        val teamMap = hashMapOf(
                                            "name" to name,
                                            "image" to uri.toString(),
                                            "active" to true
                                        )
                                        refTeams.child(teamId).setValue(teamMap)
                                        callback(true)
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }
            .addOnFailureListener { e ->
                callback(false)
            }
    }

    fun editTeam(name: String, callback: (Boolean) -> Unit) {
        refTeams.child("-OWNER").child("name").setValue(name).addOnSuccessListener {
            callback(true)
        }
    }

    fun setImgTeam(imgUrl: String, idTeam: String) {
        val refUser = databaseReference.child("teams").child(idTeam)
        refUser.child("image").setValue(imgUrl)
    }

    fun uploadImage(imgUri: Uri, idTeam: String) {
        val route = storageRef.child("profileImages").child(imgUri.lastPathSegment!!)
        route.putFile(imgUri)
            .addOnSuccessListener {
                route.getDownloadUrl().addOnSuccessListener { uri ->
                    setImgTeam(uri.toString(), idTeam)
                }
            }
    }

    fun getTeamsActive(callback: (MutableList<Team>) -> Unit) {
        refTeams.orderByChild("active").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val activeTeams = mutableListOf<Team>()
                    for (teamSnapshot in snapshot.children) {
                        val team = teamSnapshot.getValue(Team::class.java)
                        team?.let {
                            if (!team.owner) {
                                activeTeams.add(team)
                            }
                        }
                    }
                    callback(activeTeams)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    //gets owner too
    fun getAllTeamsActive(callback: (MutableList<Team>) -> Unit) {
        refTeams.orderByChild("active").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val activeTeams = mutableListOf<Team>()
                    for (teamSnapshot in snapshot.children) {
                        val team = teamSnapshot.getValue(Team::class.java)
                        team?.let {
                            activeTeams.add(team)
                        }
                    }
                    callback(activeTeams)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun deleteTeam(team: Team, callback: (Boolean) -> Unit) {
        refTeams.orderByChild("name").equalTo(team.name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        val teamId = userSnapshot.key
                        teamId?.let {
                            refTeams.child(teamId).child("active").setValue(false)
                                .addOnSuccessListener {
                                    callback(true)
                                }
                                .addOnFailureListener { e ->
                                    callback(false)
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }

    fun getAdmin(userId: String): Flow<Administrator?> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(Administrator::class.java)
                trySend(user).isSuccess
            }

            override fun onCancelled(databaseError: DatabaseError) {
                close(databaseError.toException())
            }
        }

        refUsers.child(userId).addValueEventListener(valueEventListener)

        awaitClose {
            refUsers.removeEventListener(valueEventListener)
        }
    }

    fun getPlayer(userId: String): Flow<Player?> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(Player::class.java)
                trySend(user).isSuccess
            }

            override fun onCancelled(databaseError: DatabaseError) {
                close(databaseError.toException())
            }
        }

        refUsers.child(userId).addValueEventListener(valueEventListener)

        awaitClose {
            refUsers.removeEventListener(valueEventListener)
        }
    }

    fun seeCodes(callback: (String?, String?) -> Unit) {
        refCodes
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val admin = snapshot.child("admin").getValue(String::class.java)
                    val player = snapshot.child("player").getValue(String::class.java)
                    callback(admin, player)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun addPosition(name: String, callback: (Boolean) -> Unit) {
        val query: Query = refPositions.orderByChild("name").equalTo(name)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val exists = dataSnapshot.exists()

                if (!exists) {
                    val newPos = refPositions.push()
                    newPos.child("name").setValue(name)
                        .addOnSuccessListener {
                            callback(true)
                        }
                } else {
                    callback(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun getPositions(callback: (MutableList<String>) -> Unit) {
        refPositions.orderByChild("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val positions = mutableListOf<String>()
                    for (pSnapshot in snapshot.children) {
                        val pos = pSnapshot.child("name").getValue(String::class.java)
                        pos?.let {
                            positions.add(pos)
                        }
                    }
                    callback(positions)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun deletePos(pos: String, callback: (Boolean) -> Unit) {
        refPositions.orderByChild("name").equalTo(pos)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val posSnapshot = snapshot.children.first()
                        val posId = posSnapshot.key

                        posId?.let {
                            refPositions.child(it).removeValue()
                                .addOnSuccessListener {
                                    callback(true)
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    fun getTeam(teamId: String, callback: (Team?) -> Unit) {
        refTeams.child(teamId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val team = dataSnapshot.getValue(Team::class.java)
                callback(team)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(null)
            }
        })
    }

    fun deleteMatch(matchId: String?, callback: (Boolean) -> Unit) {
        matchId?.let {
            refMatches.child(matchId).removeValue()
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
        }
    }

    fun finishMatch(
        match: Match,
        callback: (Boolean) -> Unit
    ) {
        refMatches.child(match.id!!).setValue(match).addOnSuccessListener {
            callback(true)
        }
    }

    fun cancelMatch(matchId: String?, callback: (Boolean) -> Unit) {
        matchId?.let {
            refMatches.child(matchId).child("status").setValue(StatusMatch.CANCELLED.toString())
                .addOnSuccessListener {
                    callback(true)
                }
        }
    }

    fun getMatches(callback: (MutableList<Match>) -> Unit) {
        refMatches.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val matches = mutableListOf<Match>()
                for (snapshot in dataSnapshot.children) {
                    val match = snapshot.getValue(Match::class.java)
                    match?.let {
                        matches.add(match)
                    }
                }
                callback(matches)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun updateMatchesStatus(callback: (Boolean) -> Unit) {
        //get today date
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val today = dateFormat.format(calendar.time)

        val comparator = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val actual = comparator.parse(today)


        refMatches.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val match = snapshot.getValue(Match::class.java)
                    match!!.id?.let {
                        //set ongoing
                        if (match.status == StatusMatch.SCHEDULED.toString() && match.date == today) {
                            val status = StatusMatch.ONGOING.toString()
                            refMatches.child(match.id!!).child("status").setValue(status)
                        }

                        //set finished
                        val selected = comparator.parse(match.date)

                        if (match.status != StatusMatch.FINISHED.toString()) {
                            if (match.status != StatusMatch.CANCELLED.toString() && selected.before(
                                    actual
                                )
                            ) {
                                val status = StatusMatch.FINISHED.toString()
                                refMatches.child(match.id!!).child("status").setValue(status)
                            }
                        }
                    }
                }
                callback(true)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun getFilteredMatches(seasonMatch: String, callback: (MutableList<Match>) -> Unit) {
        refMatches.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val matches = mutableListOf<Match>()
                for (snapshot in dataSnapshot.children) {
                    val season = snapshot.child("season").getValue(String::class.java)
                    if (season == seasonMatch) {
                        val match = snapshot.getValue(Match::class.java)
                        match?.let {
                            matches.add(match)
                        }
                    }
                }
                callback(matches)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun addMatch(
        team1: String,
        team2: String,
        date: String,
        place: String,
        seasonSelected: String,
        league: String,
        start: String,
        finish: String,
        status: String,
        scoreTeam1: String,
        scoreTeam2: String,
        callback: (Boolean) -> Unit
    ) {
        refTeams.orderByChild("name").equalTo(team1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (team1Snapshot in dataSnapshot.children) {
                        val team1Id = team1Snapshot.key
                        team1Id?.let {
                            refTeams.orderByChild("name").equalTo(team2)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        for (team2Snapshot in dataSnapshot.children) {
                                            val team2Id = team2Snapshot.key
                                            team2Id?.let {

                                                //check if exists already
                                                refMatches.addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                        var exists = false

                                                        val match = Match(
                                                            team1Id,
                                                            team2Id,
                                                            date,
                                                            place,
                                                            seasonSelected,
                                                            league,
                                                            start,
                                                            finish,
                                                            status,
                                                            scoreTeam1,
                                                            scoreTeam2,
                                                        )

                                                        for (snapshot in dataSnapshot.children) {
                                                            val existingMatch =
                                                                snapshot.getValue(Match::class.java)
                                                            if (existingMatch != null && existingMatch == match) {
                                                                exists = true
                                                                break
                                                            }
                                                        }

                                                        if (exists) {
                                                            callback(false)
                                                        } else {
                                                            val matchId = refMatches.push().key
                                                            match.id = matchId
                                                            if (matchId != null) {
                                                                refMatches.child(matchId)
                                                                    .setValue(match)
                                                                    .addOnCompleteListener { task ->
                                                                        if (task.isSuccessful) {
                                                                            callback(true)
                                                                        }
                                                                    }
                                                            }
                                                        }
                                                    }

                                                    override fun onCancelled(databaseError: DatabaseError) {}
                                                })

                                            }
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {}
                                })
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }
}