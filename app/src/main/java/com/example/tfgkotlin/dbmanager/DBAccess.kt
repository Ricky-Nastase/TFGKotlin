package com.example.tfgkotlin.dbmanager

import android.net.Uri
import com.example.tfgkotlin.objects.Administrator
import com.example.tfgkotlin.objects.Player
import com.example.tfgkotlin.objects.UserType
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.security.MessageDigest

object DBAccess {
    val database = FirebaseDatabase.getInstance()
    val refUsers = database.getReference("users")
    val refCodes = database.getReference("codes")
    val refPositions = database.getReference("positions")
    val storageRef = FirebaseStorage.getInstance().getReference()
    private val databaseReference = FirebaseDatabase.getInstance().reference

    enum class LoginFailureReason {
        USER_NOT_FOUND,
        INVALID_CREDENTIALS,
        DATABASE_ERROR,
    }

    fun checkUserActive(userId: String, callback: (Boolean) -> Unit) {
        refUsers.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val active = dataSnapshot.child("active").getValue(Boolean::class.java) ?: false
                callback(active)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }

    fun login(
        email: String,
        pass: String,
        callback: (Boolean, String?, LoginFailureReason?) -> Unit
    ) {
        refUsers.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val password =
                                userSnapshot.child("password").getValue(String::class.java)
                            if (pass == password) {
                                callback(true, userSnapshot.key, null)
                                return
                            }
                        }
                        callback(false, null, LoginFailureReason.INVALID_CREDENTIALS)
                    } else {
                        callback(false, null, LoginFailureReason.USER_NOT_FOUND)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(false, null, LoginFailureReason.DATABASE_ERROR)
                }
            })
    }

    fun setImgUser(imgUrl: String, idUser: String) {
        val refUser = databaseReference.child("users").child(idUser)
        refUser.child("profileImage").setValue(imgUrl)
    }

    fun uploadImage(imgUri: Uri, idUser: String) {
        val route = storageRef.child("profileImages").child(imgUri.lastPathSegment!!)
        route.putFile(imgUri)
            .addOnSuccessListener {
                route.getDownloadUrl().addOnSuccessListener { uri ->
                    setImgUser(uri.toString(), idUser)
                }
            }
    }

    fun signUpAdmin(
        userEmail: String,
        userPass: String,
        userName: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val adminId = refUsers.push().key

        val userAdmin =
            Administrator(
                adminId,
                userName,
                userEmail,
                userPass,
                UserType.ADMIN,
                true
            )

        if (adminId != null) {
            refUsers.child(adminId).setValue(userAdmin)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(true, adminId)
                    } else {
                        callback(false, null)
                    }
                }
        }
    }

    fun signUpPlayer(
        userEmail: String,
        userPass: String,
        userName: String,
        userWeight: String,
        userHeight: String,
        userPos: String,
        userBirthdate: String,
        userNumber: String,
        userSex: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val playerId = refUsers.push().key

        val userPlayer =
            Player(
                playerId,
                userName,
                userEmail,
                userPass,
                userWeight,
                userHeight,
                userPos,
                userBirthdate,
                userNumber,
                userSex,
                UserType.PLAYER,
                true
            )

        if (playerId != null) {
            refUsers.child(playerId).setValue(userPlayer)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(true, playerId)
                    } else {
                        callback(false, null)
                    }
                }
        }
    }

    fun checkEmailExists(
        email: String,
        callback: (Boolean) -> Unit
    ) {
        val query: Query = refUsers.orderByChild("email").equalTo(email)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.exists())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }

    fun checkNumberExists(
        number: String,
        callback: (Boolean) -> Unit
    ) {
        val query: Query = refUsers.orderByChild("number").equalTo(number)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.exists())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }

    fun checkEmailExistsReactivate(
        email: String,
        userType: String,
        callback: (Boolean, String?, String?) -> Unit
    ) {
        refUsers.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val active = userSnapshot.child("active").getValue(Boolean::class.java)
                            if (active == false) {
                                val type = userSnapshot.child("type").getValue(String::class.java)
                                val name =
                                    userSnapshot.child("username").getValue(String::class.java)
                                if (type == userType) {
                                    val userId = userSnapshot.key
                                    callback(true, userId, name)
                                    return
                                }
                            }
                        }
                        callback(false, null, null)
                    } else {
                        callback(false, null, null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun checkNumber(
        number: String,
        email: String,
        callback: (Boolean) -> Unit
    ) {
        refUsers.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val num =
                                userSnapshot.child("number").getValue(String::class.java)
                            if (num == number) {
                                callback(true)
                            } else {
                                callback(false)
                            }
                        }
                    } else {
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun checkUsernameExists(name: String, callback: (Boolean) -> Unit) {
        val query: Query = refUsers.orderByChild("username").equalTo(name)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.exists())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }

    fun checkUsernameExistsEdit(name: String, email: String, callback: (Boolean) -> Unit) {
        refUsers.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val username =
                                userSnapshot.child("username").getValue(String::class.java)
                            if (name == username) {
                                callback(false)
                            } else {
                                checkUsernameExists(name) { exists ->
                                    if (exists) {
                                        callback(true)
                                        println("PPPPPPP")
                                    }else{
                                        callback(false)
                                    }
                                }
                            }
                        }
                    } else {
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getCodeLastChange(callback: (String) -> Unit) {
        refCodes.child("lastDateChange")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val date = dataSnapshot.getValue(String::class.java)
                    callback(date!!)
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    fun updateAccessCode(
        newCodeAdmin: String,
        newCodePlayer: String,
        newDate: String
    ) {
        refCodes.child("lastDateChange").setValue(newDate).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                refCodes.child("admin").setValue(newCodeAdmin)
                refCodes.child("player").setValue(newCodePlayer)
            }
        }
    }

    fun getAccessCode(type: String, callback: (String?) -> Unit) {
        refCodes.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var code: String? = null
                when (type) {
                    UserType.ADMIN.toString() -> {
                        code = snapshot.child("admin").getValue(String::class.java)
                    }

                    UserType.PLAYER.toString() -> {
                        code = snapshot.child("player").getValue(String::class.java)
                    }
                }
                callback(code)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun checkPassword(
        email: String,
        pass: String,
        callback: (Boolean) -> Unit
    ) {
        refUsers.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val password =
                                userSnapshot.child("password").getValue(String::class.java)
                            if (pass == password) {
                                callback(true)
                                return
                            }
                        }
                        callback(false)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(false)
                }
            })
    }

    fun reactivateAdmin(
        email: String,
        name: String,
        userPass: String,
        callback: (Boolean) -> Unit
    ) {
        refUsers.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            userSnapshot.ref.child("username").setValue(name)
                            userSnapshot.ref.child("active").setValue(true)
                            userSnapshot.ref.child("password").setValue(userPass)
                        }
                        callback(true)
                    } else {
                        callback(false)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(false)
                }
            })
    }

    fun reactivatePlayer(
        email: String,
        pass: String,
        name: String,
        weight: String,
        height: String,
        position: String,
        birthdate: String,
        number: String,
        sex: String,
        callback: (Boolean) -> Unit
    ) {
        refUsers.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val id = userSnapshot.key
                            val player = userSnapshot.getValue(Player::class.java)
                            player?.let {
                                val updatedUser = player.copy(
                                    email = email,
                                    username = name,
                                    password = pass,
                                    weight = weight,
                                    height = height,
                                    position = position,
                                    birthdate = birthdate,
                                    number = number,
                                    sex = sex,
                                    type = UserType.PLAYER,
                                    active = true
                                )
                                refUsers.child(id!!).setValue(updatedUser)
                                    .addOnSuccessListener {
                                        callback(true)
                                    }
                                    .addOnFailureListener {
                                        callback(false)
                                    }
                            }
                        }
                    } else {
                        callback(false)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(false)
                }
            })
    }

    fun encryptPassword(password: String): String? {
        try {
            val md5 = MessageDigest.getInstance("MD5")
            md5.update(password.toByteArray())
            val byteArray = md5.digest()
            val encrypt = StringBuilder()
            for (b in byteArray) {
                encrypt.append(((b.toInt() and 0xff) + 0x100).toString(16).substring(1))
            }
            return encrypt.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getPositions(): Flow<MutableList<String>> = callbackFlow {
        val positionsList = mutableListOf<String>()
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val pos = snapshot.child("name").getValue(String::class.java)
                    println(pos)
                    pos?.let {
                        positionsList.add(it)
                    }
                }
                trySend(positionsList).isSuccess
            }

            override fun onCancelled(databaseError: DatabaseError) {
                close(databaseError.toException())
            }
        }

        refPositions.addValueEventListener(valueEventListener)

        awaitClose {
            refPositions.removeEventListener(valueEventListener)
        }
    }

    fun getUserType(userId: String): Flow<String?> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val type = dataSnapshot.child("type").getValue(String::class.java)
                    trySend(type).isSuccess
                }
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

    fun getUserLogged(userId: String, callback: (Administrator?) -> Unit) {
        refUsers.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val user = dataSnapshot.getValue(Administrator::class.java)
                        callback(user)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    fun updateAdminProfile(
        name: String,
        email: String,
        newPass: String,
        id: String
    ) {
        if (name.isNotEmpty()) {
            refUsers.child(id).child("username").setValue(name)
        }
        if (email.isNotEmpty()) {
            refUsers.child(id).child("email").setValue(email)
        }
        if (newPass.isNotEmpty()) {
            refUsers.child(id).child("password").setValue(newPass)
        }
    }

    fun updatePlayerProfile(
        email: String,
        name: String,
        pass: String,
        weight: String,
        height: String,
        position: String,
        birthdate: String,
        number: String,
        sex: String,
        id: String
    ) {
        if (name.isNotEmpty()) {
            refUsers.child(id).child("username").setValue(name)
        }
        if (email.isNotEmpty()) {
            refUsers.child(id).child("email").setValue(email)
        }
        if (pass.isNotEmpty()) {
            refUsers.child(id).child("password").setValue(pass)
        }
        if (weight.isNotEmpty()) {
            refUsers.child(id).child("weight").setValue(weight)
        }
        if (position.isNotEmpty()) {
            refUsers.child(id).child("position").setValue(position)
        }
        if (height.isNotEmpty()) {
            refUsers.child(id).child("height").setValue(height)
        }
        if (birthdate.isNotEmpty()) {
            refUsers.child(id).child("birthdate").setValue(birthdate)
        }
        if (number.isNotEmpty()) {
            refUsers.child(id).child("number").setValue(number)
        }
        if (sex.isNotEmpty()) {
            refUsers.child(id).child("sex").setValue(sex)
        }
    }

    fun deleteUser(
        email: String,
        callback: (Boolean) -> Unit
    ) {
        refUsers.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            userSnapshot.ref.child("active").setValue(false)
                        }
                        callback(true)
                    } else {
                        callback(false)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(false)
                }
            })
    }
}