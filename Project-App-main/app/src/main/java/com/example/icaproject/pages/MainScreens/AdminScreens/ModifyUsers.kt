package com.example.icaproject.pages.MainScreens.AdminScreens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.TextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

data class User(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val userId: Long
) {
    constructor() : this(
        name = "",
        email = "",
        password = "",
        role = "",
        userId = 0L
    )
}

@Composable
fun ModifyUsersScreen(navController: NavController) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()
    var userid by remember { mutableStateOf(TextFieldValue()) }

    // Function to fetch user data from Firestore
    fun fetchUsers() {
        db.collection("Users")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val usersList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject<User>()
                }
                users = usersList
                isLoading = false
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
                isLoading = false
            }
    }

    // Fetch companies on first launch
    LaunchedEffect(Unit) {
        fetchUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // User list section
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Users",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            UserList(users)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Modify Users section
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Modify Users",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = userid,
                onValueChange = { userid = it },
                label = { Text("User ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    if (userid.text.isNotEmpty()) {
                        getDocumentIdForUser(userid.text) { documentId ->
                            if (documentId != null) {
                                db.collection("Users").document(documentId)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "Document successfully deleted!")
                                        fetchUsers() // Refresh list after deletion
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("Firestore", "Error deleting document", e)
                                    }
                            } else {
                                Log.w("Firestore", "No document found with Company_ID: ${userid.text}")
                            }
                        }
                    } else {
                        Log.w("Firestore", "userid is empty")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Delete User")
            }
        }
    }
}



@Composable
fun UserList(users: List<User>) {
    if (users.isEmpty()) {
        Text(
            text = "No users available",
            modifier = Modifier.fillMaxSize().padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(users) { user ->
                UserItem(user)
                Spacer(modifier = Modifier.height(16.dp)) // Spacing between items
            }
        }
    }
}

@Composable
fun UserItem(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("User ID: ${user.userId}", style = MaterialTheme.typography.titleMedium)
            Text("Name: ${user.name}")
            Text("Email: ${user.email}")
            Text("Role: ${user.role}")
        }
    }
}

fun getDocumentIdForUser(userId: String, onResult: (String?) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    // Convert userid to Long before querying
    val userIdLong = userId.toLongOrNull()
    if (userIdLong == null) {
        Log.e("Firestore", "Invalid userId format")
        onResult(null)
        return
    }

    db.collection("Users")
        .whereEqualTo("userId", userIdLong)
        .limit(1)
        .get()
        .addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                val document = documents.documents[0]
                Log.e("Firestore", "Documents retrieved: ${documents.size()}")
                onResult(document.id)
            } else {
                onResult(null)
                Log.e("Firestore", "No matching document found")
            }
        }
        .addOnFailureListener { exception ->
            Log.e("Firestore", "Error getting document ID", exception)
            onResult(null)
        }
}