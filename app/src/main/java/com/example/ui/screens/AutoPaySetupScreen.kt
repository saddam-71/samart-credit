package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.MandateStatus
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.AccentTeal
import com.example.viewmodel.Screen
import com.example.viewmodel.SmartCreditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoPaySetupScreen(
    customerId: String,
    viewModel: SmartCreditViewModel
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val allCustomers by viewModel.globalCustomers.collectAsState()
    val customer = allCustomers.find { it.id == customerId }

    var selectedOption by remember { mutableStateOf<String?>(null) } // "QR", "UPI", "LINK"
    var upiInput by remember { mutableStateOf("") }
    var linkSent by remember { mutableStateOf(false) }
    var qrScannedInfo by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer AutoPay Setup", color = Color.White) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Header
            if (customer != null) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(customer.fullName, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DarkNavy)
                        Text("Customer ID: ${customer.mobileNumber}", fontSize = 12.sp, color = PrimaryTeal, fontWeight = FontWeight.SemiBold)
                        Text("Outstanding Due: ₹${customer.currentBalancePaise / 100.0}", fontSize = 12.sp, color = Color(0xFFDC2626))
                    }
                }
            }

            Text("Select AutoPay Activation Method", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
            Text(
                "NPCI UPI AutoPay requires explicit customer authorisation from their UPI application. Select a method below:",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )

            // Option A: Scan Customer UPI QR
            Card(
                onClick = {
                    selectedOption = "QR"
                    qrScannedInfo = "upi://mandate?pa=${customer?.mobileNumber}@upi&pn=${customer?.fullName}&mc=5411"
                },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (selectedOption == "QR") Color(0xFFF0FDFA) else Color.White),
                border = if (selectedOption == "QR") androidx.compose.foundation.BorderStroke(2.dp, PrimaryTeal) else null,
                modifier = Modifier.fillMaxWidth().testTag("option_scan_qr")
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color(0xFFCCFBF1), modifier = Modifier.size(50.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = PrimaryTeal)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Option A: Scan Customer UPI QR", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkNavy)
                        Text("Scan customer's UPI app QR code for recurring mandate", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }

            // Option B: Enter Customer UPI ID
            Card(
                onClick = { selectedOption = "UPI" },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (selectedOption == "UPI") Color(0xFFF0FDFA) else Color.White),
                border = if (selectedOption == "UPI") androidx.compose.foundation.BorderStroke(2.dp, PrimaryTeal) else null,
                modifier = Modifier.fillMaxWidth().testTag("option_enter_upi")
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color(0xFFE0F2FE), modifier = Modifier.size(50.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF0284C7))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Option B: Enter Customer UPI ID", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkNavy)
                        Text("Send collect request directly to customer's VPA ID", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }

            // Option C: Send Activation Link to Mobile
            Card(
                onClick = { selectedOption = "LINK" },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (selectedOption == "LINK") Color(0xFFF0FDFA) else Color.White),
                border = if (selectedOption == "LINK") androidx.compose.foundation.BorderStroke(2.dp, PrimaryTeal) else null,
                modifier = Modifier.fillMaxWidth().testTag("option_send_link")
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color(0xFFFEF3C7), modifier = Modifier.size(50.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.SendToMobile, contentDescription = null, tint = Color(0xFFB45309))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Option C: Send Activation Link to Mobile", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkNavy)
                        Text("Dispatches secure e-mandate authorisation SMS", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }

            // Interactive panels for chosen option
            when (selectedOption) {
                "QR" -> {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Camera QR Scanner Simulation", fontWeight = FontWeight.Bold)
                            Text("Detected mandate payload: ${qrScannedInfo ?: "Waiting for camera scan..."}", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(vertical = 6.dp))
                            Text("Disclaimer: Scanning alone does not debit money. Customer must approve recurring mandate in their UPI app.", fontSize = 11.sp, color = Color(0xFFDC2626))

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.updateCustomerMandate(customerId, MandateStatus.PENDING_CUSTOMER_APPROVAL, "UMN-QR-${(1000..9999).random()}")
                                    viewModel.navigateBack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continue to Provider Authorisation")
                            }
                        }
                    }
                }
                "UPI" -> {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Enter Customer VPA / UPI ID", fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = upiInput,
                                onValueChange = { upiInput = it },
                                label = { Text("Customer UPI ID (e.g. mobile@okhdfcbank)") },
                                modifier = Modifier.fillMaxWidth().testTag("vpa_input"),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (upiInput.contains("@")) {
                                        viewModel.updateCustomerMandate(customerId, MandateStatus.PENDING_CUSTOMER_APPROVAL, "UMN-UPI-${(1000..9999).random()}")
                                        viewModel.navigateBack()
                                    } else {
                                        viewModel.showToast("Please enter a valid UPI ID with @ symbol")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Send Mandate Approval Request")
                            }
                        }
                    }
                }
                "LINK" -> {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Send e-Mandate SMS Link", fontWeight = FontWeight.Bold)
                            Text("Recipient: +91 ${customer?.mobileNumber}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("The customer will receive an approved DLT transactional SMS with a secure link to approve the mandate in GPay/PhonePe/Paytm.", fontSize = 12.sp, color = Color(0xFF64748B))

                            if (linkSent) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                                    Text("Activation link sent! Status: PENDING_CUSTOMER_APPROVAL", color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                                }
                            }

                            Button(
                                onClick = {
                                    linkSent = true
                                    viewModel.updateCustomerMandate(customerId, MandateStatus.ACTIVATION_LINK_SENT, "UMN-SMS-${(1000..9999).random()}")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                modifier = Modifier.fillMaxWidth().testTag("send_mandate_sms_btn")
                            ) {
                                Text(if (linkSent) "Resend Activation Link" else "Send Activation Link via SMS")
                            }
                        }
                    }
                }
            }
        }
    }
}
