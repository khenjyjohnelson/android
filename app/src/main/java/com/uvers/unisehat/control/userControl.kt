package com.uvers.unisehat.control

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.uvers.unisehat.models.Users

fun addUser(user: Users, imageUri: Uri?, onComplete: (Boolean) -> Unit){
    val database = FirebaseDatabase.getInstance()
    val usersRef = database.getReference("Users").child(user.identifier)

    if (imageUri != null) {
        val storageRef = FirebaseStorage.getInstance().reference.child("user/${user.identifier}")
        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                user.photoUrl = uri.toString()
                usersRef.setValue(user).addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }
            }
        }.addOnFailureListener {
            onComplete(false)
        }
    } else {
        usersRef.setValue(user).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }
}


fun updateUser(user: Users, imageUri: Uri?, onComplete: (Boolean) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val usersRef = database.getReference("Users").child(user.identifier)

    if (imageUri != null) {
        val storageRef = FirebaseStorage.getInstance().reference.child("user/${user.identifier}")
        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                user.photoUrl = uri.toString()
                usersRef.setValue(user).addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }
            }
        }.addOnFailureListener {
            onComplete(false)
        }
    } else {
        usersRef.setValue(user).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }
}


fun deleteUser(userId: String, onComplete: (Boolean) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val usersRef = database.getReference("Users").child(userId)
    usersRef.removeValue().addOnCompleteListener { task ->
        onComplete(task.isSuccessful)
    }
}



@Composable
fun UpdatePasswordDialog(
    username: String,
    newPassword: String,
    onPasswordChange: (String) -> Unit,
    onUpdateClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    var newPasswordIsEmpty by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelClick,
        title = { Text("Update password $username") },
        text = {
            Column {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onPasswordChange,
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = newPasswordIsEmpty,
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (newPassword.isNotEmpty()) {
                    onUpdateClick() // Hanya jalankan jika password tidak kosong
                } else {
                    newPasswordIsEmpty = true // Tandai error jika kosong
                }
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = onCancelClick) {
                Text("Cancel")
            }
        }
    )
}



fun updatePassword(context: Context, identifier: String, newPassword: String, callback: () -> Unit) {
    val databaseReference = Firebase.database.getReference("Users/$identifier")
    databaseReference.child("password").setValue(newPassword)
        .addOnSuccessListener {
            callback()
            Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to update password", Toast.LENGTH_SHORT).show()
        }
}


fun fetchUser(identifier: String, onResult: (Users?) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val usersRef = database.getReference("Users").child(identifier)

    usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val user = snapshot.getValue(Users::class.java)
            onResult(user)
        }

        override fun onCancelled(error: DatabaseError) {
            onResult(null)
        }
    })
}