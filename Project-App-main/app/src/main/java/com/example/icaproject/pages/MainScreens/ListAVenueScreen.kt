package com.example.icaproject.pages.MainScreens

import androidx.navigation.NavController
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.platform.LocalContext

@Composable
fun ListAVenueScreen(navController: NavController){
        val db = FirebaseFirestore.getInstance()
        val context = LocalContext.current

        var name by remember { mutableStateOf(TextFieldValue("")) }
        var description by remember { mutableStateOf(TextFieldValue("")) }
        var location by remember { mutableStateOf(TextFieldValue("")) }
        var type by remember { mutableStateOf(TextFieldValue("")) }
        var status by remember { mutableStateOf(TextFieldValue("")) }
        var companyId by remember { mutableStateOf(TextFieldValue("")) }
        var capacity by remember { mutableStateOf(TextFieldValue("")) }
        var pricePerHour by remember { mutableStateOf(TextFieldValue("")) }
        var isLoading by remember { mutableStateOf(false) }
        var venueId by remember { mutableStateOf<Long?>(null) }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Venue Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Type") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = status,
                onValueChange = { status = it },
                label = { Text("Status") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = companyId,
                onValueChange = { companyId = it },
                label = { Text("Company ID") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = capacity,
                onValueChange = { capacity = it },
                label = { Text("Capacity") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = pricePerHour,
                onValueChange = { pricePerHour = it },
                label = { Text("Price Per Hour") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            venueId?.let {
                Text("Venue ID: $it", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
            }

            Button(
                onClick = {
                    if (name.text.isNotEmpty() && description.text.isNotEmpty() && location.text.isNotEmpty() &&
                        type.text.isNotEmpty() && status.text.isNotEmpty() && companyId.text.isNotEmpty() &&
                        capacity.text.isNotEmpty() && pricePerHour.text.isNotEmpty()) {

                        isLoading = true
                        db.collection("Venues").get()
                            .addOnSuccessListener { documents ->
                                val highestId = documents.mapNotNull { it.getLong("Venue_ID") }.maxOrNull() ?: 0
                                val newVenueId = highestId + 1
                                venueId = newVenueId
                                val venue = hashMapOf(
                                    "Venue_ID" to newVenueId,
                                    "name" to name.text,
                                    "Description" to description.text,
                                    "Location" to location.text,
                                    "Type" to type.text,
                                    "Status" to status.text,
                                    "Company_ID" to companyId.text.toLong(),
                                    "Capacity" to capacity.text.toInt(),
                                    "Price_Per_Hour" to pricePerHour.text.toInt()
                                )
                                db.collection("Venues").add(venue)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        Toast.makeText(context, "Venue Listed Successfully", Toast.LENGTH_SHORT).show()
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
                Text(if (isLoading) "Listing..." else "List Venue")
            }
        }
    }