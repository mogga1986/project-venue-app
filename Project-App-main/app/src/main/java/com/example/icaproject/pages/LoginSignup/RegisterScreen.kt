package com.example.icaproject.pages.LoginSignup

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegistrationScreen(navController: NavController, onRegistrationSuccess: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf(TextFieldValue("")) }
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var selectedRole by remember { mutableStateOf("Customer") }
    val roles = listOf("Customer","Admin", "Company")
    var isLoading by remember { mutableStateOf(false) }
    var company_ID by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { expanded = true }) {
                Text(selectedRole)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                roles.forEach { role ->
                    DropdownMenuItem(onClick = {
                        selectedRole = role
                        expanded = false
                    }) {
                        Text(role)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedRole == "Company") {
            TextField(
                value = company_ID,
                onValueChange = { company_ID = it },
                label = { Text("Company ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
            onClick = { navController.navigate("RegisterACompanyScreen") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Or Register a Company")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = {
                if (email.text.isNotEmpty() && name.text.isNotEmpty() && password.text.isNotEmpty()) {
                    isLoading = true
                    db.collection("Users").get()
                        .addOnSuccessListener { documents ->
                            val highestId = documents.mapNotNull { it.getLong("userId") }.maxOrNull() ?: 0
                            val newUserId = highestId + 1
                            val user = hashMapOf(
                                "email" to email.text,
                                "name" to name.text,
                                "password" to password.text,
                                "userId" to newUserId,
                                "role" to selectedRole,
                                "company_ID" to company_ID
                            )
                            db.collection("Users").add(user)
                                .addOnSuccessListener {
                                    isLoading = false
                                    Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
                                    onRegistrationSuccess()
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Registering..." else "Register")
        }
    }
}