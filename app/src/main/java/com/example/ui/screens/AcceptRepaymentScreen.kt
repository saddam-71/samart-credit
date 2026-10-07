package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import com.example.data.model.PaymentSource
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.PrimaryTeal
import com.example.viewmodel.Screen
import com.example.viewmodel.SmartCreditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptRepaymentScreen(
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
    var note by remember { mutableStateOf("") }
    var selectedSource by remember { mutableStateOf(PaymentSource.CASH) }
    var showConfirmation by remember { mutableStateOf(false) }

    val filteredCustomers = customers.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) || it.mobileNumber.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accept Repayment", color = Color.White) },
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
            if (selectedCustomer == null) {
                Text("Select Customer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by customer name or mobile number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryTeal) },
                    modifier = Modifier.fillMaxWidth().testTag("repay_cust_search"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(filteredCustomers) { cust ->
                        Card(
                            onClick = {
                                selectedCustomer = cust
                                amountStr = (cust.currentBalancePaise / 100.0).toString()
                            },
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
                            Text("Current Due: ₹${selectedCustomer!!.currentBalancePaise / 100.0}", fontSize = 14.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { selectedCustomer = null }) {
                            Text("Change", color = PrimaryTeal)
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Repayment Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text("Repayment Amount in Rupees (₹) *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("repay_amount_input")
                        )

                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Receipt Note / Reference (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Payment Channel:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedSource == PaymentSource.CASH,
                                onClick = { selectedSource = PaymentSource.CASH },
                                label = { Text("Manual Cash Collection") }
                            )
                            FilterChip(
                                selected = selectedSource == PaymentSource.BANK_TRANSFER,
                                onClick = { selectedSource = PaymentSource.BANK_TRANSFER },
                                label = { Text("Online UPI / Bank") }
                            )
                        }

                        if (selectedSource == PaymentSource.CASH) {
                            Text("Note: Manual cash entries are recorded by store staff and marked as Manual Records.", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            showConfirmation = true
                        } else {
                            viewModel.showToast("Please enter a valid amount")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("repay_review_btn")
                ) {
                    Text("Review Repayment", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }

    if (showConfirmation && selectedCustomer != null) {
        val amt = amountStr.toDoubleOrNull() ?: 0.0
        val remainingBal = (selectedCustomer!!.currentBalancePaise / 100.0) - amt

        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirm Repayment Collection") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Customer: ${selectedCustomer!!.fullName} (${selectedCustomer!!.mobileNumber})")
                    Text("Amount: ₹$amt", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                    Text("Channel: ${selectedSource.name}")
                    Text("Remaining Balance: ₹$remainingBal", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Balances are updated atomically in the append-only ledger.", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordRepayment(
                            customerId = selectedCustomer!!.id,
                            amountRupees = amt,
                            description = note.ifBlank { "Repayment received" },
                            source = selectedSource
                        )
                        showConfirmation = false
                        viewModel.navigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("Confirm & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) { Text("Cancel") }
            }
        )
    }
}
