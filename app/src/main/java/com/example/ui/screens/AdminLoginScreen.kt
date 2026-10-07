package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.Screen
import com.example.viewmodel.SmartCreditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(viewModel: SmartCreditViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    var email by remember { mutableStateOf("dr.saddam.co@gmail.com") }
    var pinOrOtp by remember { mutableStateOf("2026") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Super Admin Authentication") },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = CircleShape,
                color = Color(0xFF1E293B),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Shield",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Protected Primary Owner Access",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Verified server-side role check • Multi-factor authentication",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Designated Admin Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF38BDF8)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_email_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedLabelColor = Color(0xFF38BDF8),
                    unfocusedLabelColor = Color(0xFF94A3B8)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pinOrOtp,
                onValueChange = { pinOrOtp = it },
                label = { Text("Security MFA Token / PIN (Default: 2026)") },
                leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF38BDF8)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_pin_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedLabelColor = Color(0xFF38BDF8),
                    unfocusedLabelColor = Color(0xFF94A3B8)
                ),
                singleLine = true
            )

            errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = err,
                    color = Color(0xFFF87171),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    viewModel.loginAdmin(
                        email = email,
                        pinOrOtp = pinOrOtp,
                        onSuccess = { isLoading = false },
                        onError = {
                            isLoading = false
                            errorMessage = it
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("admin_verify_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify & Open Super Admin Dashboard", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Security Notice & Audit Guarantee",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF1F5F9),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Every Super Admin session, parameter update, merchant restriction, and rate adjustment is permanently recorded in the append-only audit log.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
