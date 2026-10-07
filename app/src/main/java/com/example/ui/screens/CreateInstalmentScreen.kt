package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.InterestMethod
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.AccentTeal
import com.example.viewmodel.Screen
import com.example.viewmodel.SmartCreditViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInstalmentScreen(
    customerId: String,
    principalRupees: Double?,
    viewModel: SmartCreditViewModel
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val allCustomers by viewModel.globalCustomers.collectAsState()
    val customer = allCustomers.find { it.id == customerId }

    var principalStr by remember {
        mutableStateOf(
            if (principalRupees != null && principalRupees > 0) principalRupees.toString()
            else if (customer != null && customer.currentBalancePaise > 0) (customer.currentBalancePaise / 100.0).toString()
            else "1000"
        )
    }

    var interestRateStr by remember { mutableStateOf("0.0") } // 0% to 10%
    var method by remember { mutableStateOf(InterestMethod.NO_INTEREST) }
    var durationMonthsStr by remember { mutableStateOf("3") }
    var dueDayStr by remember { mutableStateOf("5") }

    val principal = principalStr.toDoubleOrNull() ?: 0.0
    val interestRate = interestRateStr.toDoubleOrNull() ?: 0.0
    val durationMonths = durationMonthsStr.toIntOrNull() ?: 3

    // Calculations
    val totalInterest = when (method) {
        InterestMethod.NO_INTEREST -> 0.0
        InterestMethod.SIMPLE_INTEREST -> {
            val years = durationMonths / 12.0
            principal * (interestRate / 100.0) * years
        }
        InterestMethod.REDUCING_BALANCE -> {
            val r = (interestRate / 100.0) / 12.0
            if (r > 0) {
                val emi = principal * r * Math.pow(1 + r, durationMonths.toDouble()) / (Math.pow(1 + r, durationMonths.toDouble()) - 1)
                (emi * durationMonths) - principal
            } else 0.0
        }
    }
    val totalPayable = principal + totalInterest
    val monthlyInstalment = if (durationMonths > 0) totalPayable / durationMonths else 0.0

    val dayFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Instalment Plan Calculator", color = Color.White) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Customer Banner
            if (customer != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(customer.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                        Text("Customer ID: ${customer.mobileNumber} • Current Due: ₹${customer.currentBalancePaise / 100.0}", fontSize = 12.sp, color = PrimaryTeal)
                    }
                }
            }

            // Calculation Inputs Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("1. Principal & Interest Terms", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    OutlinedTextField(
                        value = principalStr,
                        onValueChange = { principalStr = it },
                        label = { Text("Outstanding Principal Amount (₹) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("inst_principal_input")
                    )

                    OutlinedTextField(
                        value = interestRateStr,
                        onValueChange = { interestRateStr = it },
                        label = { Text("Interest Rate % (0.0% to 10.0%) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("inst_rate_input")
                    )

                    Text("Calculation Method:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = method == InterestMethod.NO_INTEREST,
                            onClick = {
                                method = InterestMethod.NO_INTEREST
                                interestRateStr = "0.0"
                            },
                            label = { Text("No Interest (0%)") }
                        )
                        FilterChip(
                            selected = method == InterestMethod.SIMPLE_INTEREST,
                            onClick = { method = InterestMethod.SIMPLE_INTEREST },
                            label = { Text("Simple Interest") }
                        )
                        FilterChip(
                            selected = method == InterestMethod.REDUCING_BALANCE,
                            onClick = { method = InterestMethod.REDUCING_BALANCE },
                            label = { Text("Reducing Balance") }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. Duration & Due Schedule", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = durationMonthsStr,
                            onValueChange = { durationMonthsStr = it },
                            label = { Text("Tenure (Months)") },
                            modifier = Modifier.weight(1f).testTag("inst_tenure_input")
                        )
                        OutlinedTextField(
                            value = dueDayStr,
                            onValueChange = { dueDayStr = it },
                            label = { Text("Monthly Due Day (1-28)") },
                            modifier = Modifier.weight(1f).testTag("inst_day_input")
                        )
                    }
                }
            }

            // Live Calculated Preview Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("LIVE REPAYMENT PREVIEW", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Column {
                            Text("Monthly Instalment (EMI)", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%.2f", monthlyInstalment)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentTeal
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Payable", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            Text("₹${String.format(Locale.getDefault(), "%.2f", totalPayable)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFF233554))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Interest: ₹${String.format(Locale.getDefault(), "%.2f", totalInterest)}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text("Duration: $durationMonths Months", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                }
            }

            // Repayment Schedule Table Preview
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Breakdown Schedule (${durationMonths} Instalments)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    (1..durationMonths.coerceAtMost(6)).forEach { idx ->
                        val due = System.currentTimeMillis() + (idx.toLong() * 30L * 24 * 60 * 60 * 1000)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Instalment #$idx (${dayFormat.format(Date(due))})", fontSize = 12.sp, color = Color(0xFF334155))
                            Text("₹${String.format(Locale.getDefault(), "%.2f", monthlyInstalment)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryTeal)
                        }
                        if (idx < durationMonths.coerceAtMost(6)) Divider(color = Color(0xFFF1F5F9))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (interestRate < 0.0 || interestRate > 10.0) {
                        viewModel.showToast("Interest rate must be between 0.0% and 10.0%")
                        return@Button
                    }
                    if (principal <= 0 || durationMonths <= 0) {
                        viewModel.showToast("Please enter a valid principal and duration")
                        return@Button
                    }
                    val day = dueDayStr.toIntOrNull() ?: 5
                    viewModel.createInstalmentPlan(
                        customerId = customerId,
                        principalRupees = principal,
                        interestRatePercent = interestRate,
                        method = method,
                        durationMonths = durationMonths,
                        monthlyDueDateDay = day
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("confirm_instalment_btn")
            ) {
                Text("Confirm & Activate Plan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
