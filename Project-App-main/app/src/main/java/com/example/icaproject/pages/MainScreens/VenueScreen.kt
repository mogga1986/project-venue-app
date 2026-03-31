package com.example.icaproject.pages.MainScreens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.example.icaproject.pages.LoginSignup.UserSession.userID
import androidx.compose.ui.text.input.TextFieldValue
import com.google.firebase.Timestamp
import com.google.firebase.firestore.toObject
import androidx.compose.ui.Alignment
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

data class Venue(
    val name: String,
    val Capacity: Long,
    val CompanyId: Long,
    val Description: String,
    val Location: String,
    val Price_Per_Hour: Long,
    val Status: String,
    val Type: String,
    val Venue_ID: Long
) {
    // Explicit constructor
    constructor() : this(
        name = "",
        Capacity = 0L,
        CompanyId = 0L,
        Description = "",
        Location = "",
        Price_Per_Hour = 0L,
        Status = "",
        Type = "",
        Venue_ID = 0L
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VenueScreen(navController: NavController) {
    var venues by remember { mutableStateOf<List<Venue>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()
    var Venue_ID by remember { mutableStateOf(TextFieldValue("")) }
    var selectedDate by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }
    var timestamp by remember { mutableStateOf<Timestamp?>(null) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Function to refresh the list of venues
    fun refreshVenues() {
        isLoading = true
        db.collection("Venues")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val venuesList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject<Venue>()
                }
                venues = venuesList
                isLoading = false
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
                isLoading = false
            }
    }

    // Open Date Picker
    val datePicker = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    LaunchedEffect(Unit) {
        refreshVenues() // Initially load the venues
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
// Top half: Venue list
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                VenueList(venues)
            }
        }

        // Bottom half: Booking a venue using the venue ID and date/time
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Pick a Venue and input the ID")

            // Venue ID Input
            TextField(
                value = Venue_ID,
                onValueChange = { Venue_ID = it },
                label = { Text("Venue_ID") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Date Picker Button
            Button(onClick = { datePicker.show() }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Pick a Date")
            }
            Text(text = "Selected Date: $selectedDate")

            // Time Input Field
            OutlinedTextField(
                value = timeInput,
                onValueChange = { timeInput = it },
                label = { Text("Enter Time (HH:mm)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Save Button
            Button(
                onClick = {
                    if (selectedDate.isNotEmpty() && timeInput.isNotEmpty()) {
                        timestamp = convertToFirestoreTimestamp(selectedDate, timeInput)
                        saveToFirestore(Venue_ID.text, timestamp!!)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Save Booking")
            }

            // Display Saved Timestamp
            timestamp?.let {
                Text(text = "Saved Timestamp: ${it.toDate()}")
            }

            // Refresh Button
            Button(
                onClick = { refreshVenues() },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Refresh Venues")
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun convertToFirestoreTimestamp(date: String, time: String): Timestamp {
    val formatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm")
    val localDateTime = LocalDateTime.parse("$date $time", formatter)

    // Convert LocalDateTime to UTC Instant
    val instant = localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toInstant()

    return Timestamp(Date.from(instant))
}

    @Composable
    fun VenueList(venues: List<Venue>) {
        if (venues.isEmpty()) {
            Text(
                text = "No venues available",
                modifier = Modifier.fillMaxSize().padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(venues) { venue ->
                    VenueItem(venue)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    @Composable
    fun VenueItem(venue: Venue) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Venue ID: ${venue.Venue_ID}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Name: ${venue.name}", style = MaterialTheme.typography.titleMedium)
                Text("Location: ${venue.Location}")
                Text("Capacity: ${venue.Capacity}")
                Text("Price per Hour: $${venue.Price_Per_Hour}")
                Text("Status: ${venue.Status}")
                Text("Type: ${venue.Type}")
            }
        }
    }


fun saveToFirestore(venueId: String, timestamp: Timestamp) {
    val db = FirebaseFirestore.getInstance()
    db.collection("Payments").get()
        .addOnSuccessListener { documents ->
            val highestId = documents.mapNotNull { it.getLong("Payment_ID") }.maxOrNull() ?: 0
            val newPaymentId = highestId + 1
            db.collection("Bookings").get()
                .addOnSuccessListener { documents ->
                    val highestId =
                        documents.mapNotNull { it.getLong("Booking_ID") }.maxOrNull() ?: 0
                    val newBookingId = highestId + 1

                    // Fetch the price per hour first
                    getPricePerHour(venueId.toInt()) { pricePerHour ->
                        if (pricePerHour != null) {
                            val bookingData = hashMapOf(
                                "userId" to userID,
                                "Event_Date" to timestamp,
                                "Booking_ID" to newBookingId,
                                "Venue_ID" to venueId.toInt(),
                                "Booking_Date" to Timestamp.now(),
                                "Payement_ID" to newPaymentId
                            )

                            val paymentData = hashMapOf(
                                "Status" to "Pending",
                                "Payment_Due" to timestamp,
                                "Booking_ID" to newBookingId,
                                "Amount" to (pricePerHour * 8),
                                "Payement_ID" to newPaymentId
                            )

                            db.collection("Bookings")
                                .add(bookingData)
                                .addOnSuccessListener { Log.d("Firestore", "Booking added!") }
                                .addOnFailureListener { e -> Log.e("Firestore", "Error adding booking", e) }

                            db.collection("Payments")
                                .add(paymentData)
                                .addOnSuccessListener { Log.d("Firestore", "Payment added!") }
                                .addOnFailureListener { e -> Log.e("Firestore", "Error adding Payment", e) }
                        } else {
                            Log.e("Firestore", "Price per hour not found for venue $venueId")
                        }
                    }
                }
        }
}



fun getPricePerHour(venueId: Int, onResult: (Int?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("Venues")
        .whereEqualTo("Venue_ID", venueId)
        .get()
        .addOnSuccessListener { querySnapshot ->
            // Check if the query snapshot has any documents
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first() // Get the first document
                val pricePerHour = document.getLong("Price_Per_Hour")?.toInt()
                onResult(pricePerHour)
            } else {
                // No documents found for the given venueId
                onResult(null)
            }
        }
        .addOnFailureListener { exception ->
            // Handle any errors
            onResult(null)
        }
}

