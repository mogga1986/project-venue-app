package com.example.icaproject.pages.MainScreens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.icaproject.pages.LoginSignup.UserSession.userID
import com.example.icaproject.pages.LoginSignup.UserSession.userRole
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

// Data class to represent a Booking
data class Booking(
    val Booking_Date: Timestamp? = null,
    val Booking_ID: Long,
    val End_Time: Timestamp? = null,
    val Event_Date: Timestamp? = null,
    val Payment_ID: Long,
    val userId: Long,
    val venue_ID: Long
){
    constructor() : this(
        Booking_Date = null,
        Booking_ID = 0L ,
        End_Time = null,
        Event_Date = null,
        Payment_ID = 0L,
        userId = 0L,
        venue_ID = 0L
    )
}

@Composable
fun HomeScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val userID = userID

    // Function to refresh the list of bookings
    fun refreshBookings() {
        isLoading = true
        db.collection("Bookings")
            .whereEqualTo("userId", userID)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val bookingList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject<Booking>()
                }
                bookings = bookingList
                isLoading = false
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
                isLoading = false
            }
    }

    // Fetch data from Firestore on initial load
    LaunchedEffect(Unit) {
        refreshBookings()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // List of data from Firestore
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(bookings.size) { index ->
                    val booking = bookings[index]
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Booking ID: ${booking.Booking_ID}")
                        Text("Booking Date: ${booking.Booking_Date}")
                        Text("Event Date: ${booking.Event_Date}")
                        Text("End Time: ${booking.End_Time}")
                        Text("Payment ID: ${booking.Payment_ID}")
                        Text("User ID: ${booking.userId}")
                        Text("Venue ID: ${booking.venue_ID}")
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Buttons
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(onClick = { navController.navigate("VenueScreen") }, modifier = Modifier.fillMaxWidth()) {
                Text("Book A Venue")
            }

            if (userRole == "Company") {
                Button(onClick = { navController.navigate("ListAVenueScreen") }, modifier = Modifier.fillMaxWidth()) {
                    Text("List A Venue")
                }
            }
            if (userRole == "Admin") {
                Button(onClick = { navController.navigate("ModifyUsersScreen") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Modify Users")
                }
                Button(onClick = { navController.navigate("ViewPayments") }, modifier = Modifier.fillMaxWidth()) {
                    Text("View Payments")
                }
                Button(onClick = { navController.navigate("ModifyCompaniesScreen") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Modify Companies")
                }
            }

            // Refresh Button
            Button(
                onClick = { refreshBookings() },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Refresh Bookings")
            }
        }
    }
}

