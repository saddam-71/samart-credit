package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.Screen
import com.example.viewmodel.SmartCreditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantLoginScreen(viewModel: SmartCreditViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    var phone by remember { mutableStateOf("9876543210") }
    var otpSent by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("1234") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSendingOtp by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Merchant Login & Access") },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("merchant_login_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFE0F2FE),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PhoneIphone,
                        contentDescription = "OTP Login",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mobile OTP Authentication",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Text(
                text = "Enter your Indian mobile number to access your credit ledger or onboard your business.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10) phone = it },
                label = { Text("10-Digit Mobile Number") },
                prefix = { Text("+91 ", fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A)) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF0284C7)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("merchant_phone_input"),
                singleLine = true
            )

            if (!otpSent) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (phone.length == 10) {
                            otpSent = true
                            errorMessage = null
                            viewModel.showToast("OTP sent via transactional SMS service to +91 $phone")
                        } else {
                            errorMessage = "Please enter an exact 10-digit mobile number"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("send_otp_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request 4-Digit OTP", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6) otp = it },
                    label = { Text("Enter 4-Digit OTP (Default test: 1234)") },
                    leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null, tint = Color(0xFF0284C7)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("merchant_otp_input"),
                    singleLine = true
                )

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = err, color = Color(0xFFDC2626), fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.verifyMerchantOtp(
                            phone = phone,
                            otp = otp,
                            onSuccess = { /* navigated inside viewmodel */ },
                            onNewMerchant = {
                                viewModel.navigateTo(Screen.MerchantRegistration)
                                viewModel.showToast("Phone verified! Please complete the 6 business details.")
                            },
                            onError = { errorMessage = it }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("verify_otp_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Verify & Continue", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { otpSent = false },
                    modifier = Modifier.testTag("change_phone_button")
                ) {
                    Text("Change Mobile Number", color = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enterprise Data Isolation", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your business records, customer ledgers, and cash repayments are strictly isolated per Merchant ID. No cross-store visibility.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantRegistrationScreen(viewModel: SmartCreditViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    var businessName by remember { mutableStateOf("") }
    var businessAddress by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var authorisedPersonName by remember { mutableStateOf("") }
    var businessUpiId by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Merchant Onboarding") },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("reg_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Register Your Business",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Enter exactly the six business fields required to initialize your ledger account.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Exactly the 6 required business fields
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("1. Business Name *") },
                modifier = Modifier.fillMaxWidth().testTag("reg_bus_name"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = businessAddress,
                onValueChange = { businessAddress = it },
                label = { Text("2. Business Address (Full) *") },
                modifier = Modifier.fillMaxWidth().testTag("reg_bus_address"),
                minLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = contactNumber,
                onValueChange = { if (it.length <= 10) contactNumber = it },
                label = { Text("3. Business Contact Number *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth().testTag("reg_bus_phone"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("4. Business Email ID *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().testTag("reg_bus_email"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = authorisedPersonName,
                onValueChange = { authorisedPersonName = it },
                label = { Text("5. Authorised Person Name *") },
                modifier = Modifier.fillMaxWidth().testTag("reg_bus_person"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = businessUpiId,
                onValueChange = { businessUpiId = it },
                label = { Text("6. Business UPI ID * (e.g. name@okaxis)") },
                modifier = Modifier.fillMaxWidth().testTag("reg_bus_upi"),
                singleLine = true
            )

            errorMsg?.let { err ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = err, color = Color(0xFFDC2626), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.registerNewMerchant(
                        businessName = businessName,
                        address = businessAddress,
                        phone = contactNumber,
                        email = email,
                        personName = authorisedPersonName,
                        upiId = businessUpiId,
                        onSuccess = { /* navigated */ },
                        onError = { errorMsg = it }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_registration_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Registration (Default Free Plan)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
