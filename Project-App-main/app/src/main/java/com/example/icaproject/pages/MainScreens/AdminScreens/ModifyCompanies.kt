package com.example.icaproject.pages.MainScreens.AdminScreens

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
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

data class Company(
    val Company_ID: Long,
    val name: String,
    val Contact_email: String,
    val Contact_Phone: String,
    val address: String
) {
    constructor() : this(
        Company_ID = 0L,
        name = "",
        Contact_email = "",
        Contact_Phone = "",
        address = ""
    )
}

@Composable
fun ModifyCompaniesScreen(navController: NavController) {
    var companies by remember { mutableStateOf<List<Company>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()
    var Company_ID by remember { mutableStateOf(TextFieldValue()) }

    // Function to fetch company data from Firestore
    fun fetchCompanies() {
        isLoading = true
        db.collection("Companies")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val companiesList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject<Company>()
                }
                companies = companiesList
                isLoading = false
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
                isLoading = false
            }
    }

    // Fetch companies on first launch
    LaunchedEffect(Unit) {
        fetchCompanies()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Company list section
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Companies",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            CompanyList(companies)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Modify Companies section
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Modify Companies",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = Company_ID,
                onValueChange = { Company_ID = it },
                label = { Text("Company ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    if (Company_ID.text.isNotEmpty()) {
                        getDocumentIdForCompany(Company_ID.text) { documentId ->
                            if (documentId != null) {
                                db.collection("Companies").document(documentId)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "Document successfully deleted!")
                                        fetchCompanies() // Refresh list after deletion
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("Firestore", "Error deleting document", e)
                                    }
                            } else {
                                Log.w("Firestore", "No document found with Company_ID: ${Company_ID.text}")
                            }
                        }
                    } else {
                        Log.w("Firestore", "Company_ID is empty")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Delete Company")
            }
        }
    }
}


@Composable
fun CompanyList(companies: List<Company>) {
    if (companies.isEmpty()) {
        Text(
            text = "No companies available",
            modifier = Modifier.fillMaxSize().padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(companies) { company ->
                CompanyItem(company)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CompanyItem(company: Company) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Company ID: ${company.Company_ID}", style = MaterialTheme.typography.titleMedium)
            Text("Name: ${company.name}")
            Text("Email: ${company.Contact_email}")
            Text("Phone: ${company.Contact_Phone}")
            Text("Address: ${company.address}")
        }
    }
}

fun getDocumentIdForCompany(companyID: String, onResult: (String?) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    // Convert companyID to Long before querying
    val companyIDLong = companyID.toLongOrNull()
    if (companyIDLong == null) {
        Log.e("Firestore", "Invalid Company_ID format")
        onResult(null)
        return
    }

    db.collection("Companies")
        .whereEqualTo("Company_ID", companyIDLong)
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

