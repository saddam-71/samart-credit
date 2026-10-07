package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.PrimaryTeal
import com.example.viewmodel.Screen
import com.example.viewmodel.SmartCreditViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiveCreditScreen(
    preselectedCustomerId: String?,
    viewModel: SmartCreditViewModel
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val customers by viewModel.globalCustomers.collectAsState()
    var selectedCustomer by remember { mutableStateOf(customers.find { it.id == preselectedCustomerId }) }
    var searchQuery by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }

    val filteredCustomers = customers.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) || it.mobileNumber.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Give Credit (Udhaar)", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Step 1: Customer Selection
            if (selectedCustomer == null) {
                Text("Select Customer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or mobile number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryTeal) },
                    modifier = Modifier.fillMaxWidth().testTag("credit_cust_search"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(filteredCustomers) { cust ->
                        Card(
                            onClick = { selectedCustomer = cust },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(cust.fullName, fontWeight = FontWeight.Bold)
                                    Text("Customer ID: ${cust.mobileNumber}", fontSize = 12.sp, color = Color(0xFF64748B))
                                }
                                Text("Due: ₹${cust.currentBalancePaise / 100.0}", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(selectedCustomer!!.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                            Text("Customer ID: ${selectedCustomer!!.mobileNumber}", fontSize = 12.sp, color = PrimaryTeal, fontWeight = FontWeight.SemiBold)
                            Text("Current Due: ₹${selectedCustomer!!.currentBalancePaise / 100.0}", fontSize = 12.sp, color = Color(0xFFDC2626))
                        }
                        TextButton(onClick = { selectedCustomer = null }) {
                            Text("Change", color = PrimaryTeal)
                        }
                    }
                }

                // Step 2: Credit Input Form
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Credit Entry Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text("Credit Amount in Rupees (₹) *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("credit_amount_input")
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Items / Service Description *") },
                            modifier = Modifier.fillMaxWidth().testTag("credit_desc_input")
                        )

                        OutlinedTextField(
                            value = reference,
                            onValueChange = { reference = it },
                            label = { Text("Optional Bill / Invoice Reference No.") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull()
                        if (amt != null && amt > 0 && description.isNotBlank()) {
                            showConfirmation = true
                        } else {
                            viewModel.showToast("Please enter a valid amount and description")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("credit_proceed_btn")
                ) {
                    Text("Review Credit Entry", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }

    if (showConfirmation && selectedCustomer != null) {
        val amt = amountStr.toDoubleOrNull() ?: 0.0
        val newBal = (selectedCustomer!!.currentBalancePaise / 100.0) + amt

        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirm Credit Sale") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Customer: ${selectedCustomer!!.fullName} (${selectedCustomer!!.mobileNumber})")
                    Text("New Credit Amount: ₹$amt", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    Text("Description: $description")
                    Text("Updated Outstanding: ₹$newBal", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Would you like to immediately create an instalment plan for this credit?", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = {
                            viewModel.recordCreditSale(selectedCustomer!!.id, amt, description)
                            showConfirmation = false
                            viewModel.navigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Credit Record")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.recordCreditSale(selectedCustomer!!.id, amt, description)
                            showConfirmation = false
                            viewModel.navigateTo(Screen.CreateInstalment(selectedCustomer!!.id, amt))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save & Create Instalment Plan")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) { Text("Cancel") }
            }
        )
    }
}
