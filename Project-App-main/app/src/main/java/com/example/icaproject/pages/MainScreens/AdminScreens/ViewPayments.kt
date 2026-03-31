package com.example.icaproject.pages.MainScreens.AdminScreens

import android.util.Log
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
import androidx.compose.material.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

data class Payment(
    val Payment_Due:  Timestamp? = null,
    val Status: String,
    val Amount: Long,
    val Booking_ID: Long,
    val Payment_ID: Long
) {
    constructor() : this(
        Payment_Due = null,
        Status = "",
        Amount = 0L,
        Booking_ID = 0L,
        Payment_ID = 0L
    )
}

@Composable
fun ViewPayments(navController: NavController) {
    var Payments by remember { mutableStateOf<List<Payment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()

    // Function to refresh the list of payments
    fun refreshPayments() {
        isLoading = true
        db.collection("Payments")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val PaymentsList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject<Payment>()
                }
                Payments = PaymentsList
                isLoading = false
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
                isLoading = false
            }
    }

    // Fetch data from Firestore on initial load
    LaunchedEffect(Unit) {
        refreshPayments()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Title and list of payments
        Text("Payments", style = MaterialTheme.typography.titleMedium)

        // If loading, show progress indicator
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Display the list of payments
            PaymentsList(Payments)
        }

        // Refresh Button
        Button(
            onClick = { refreshPayments() },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Refresh Payments")
        }
    }
}


@Composable
fun PaymentsList(Payments: List<Payment>) {
    if (Payments.isEmpty()) {
        Text(
            text = "No Payments available",
            modifier = Modifier.fillMaxSize().padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(Payments) { Payment ->
                PaymentItem(Payment)
                Spacer(modifier = Modifier.height(16.dp)) // Spacing between items
            }
        }
    }
}

@Composable
fun PaymentItem(Payment: Payment) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Payment_ID: ${Payment.Payment_ID}", style = MaterialTheme.typography.titleMedium)
            Text("Status: ${Payment.Status}")
            Text("Payment_Due: ${Payment.Payment_Due}")
            Text("Booking_ID: ${Payment.Booking_ID}")
            Text("Amount: ${Payment.Amount}")

        }
    }
}