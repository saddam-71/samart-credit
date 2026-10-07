package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppConfiguration
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.PrimaryTeal
import com.example.viewmodel.Screen
import com.example.viewmodel.SmartCreditViewModel

/**
 * Welcome / Startup Screen for Smart Credit.
 *
 * Implements:
 * - Smart Credit logo and emblem with verified brand branding.
 * - Exact app name and subtitle:
 *   "Smart Credit — Customer Credit & Instalment Management"
 * - Two separate and distinct action buttons for:
 *   1. Admin Login
 *   2. Merchant Login
 * - Premium dark navy, white, and teal business-finance design with responsive Material 3 layout.
 */
@Composable
fun WelcomeScreen(
    viewModel: SmartCreditViewModel,
    config: AppConfiguration?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkNavy,
                        Color(0xFF0F172A),
                        Color(0xFF042F2E)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 1. Smart Credit Logo Emblem
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(2.dp, AccentTeal.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                    .testTag("welcome_logo_emblem")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.smart_credit_logo_1791318963387),
                        contentDescription = "Smart Credit Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = config?.appDisplayName ?: "Smart Credit",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Exact prompt requirement subtitle:
            // "Smart Credit — Customer Credit & Instalment Management"
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = PrimaryTeal.copy(alpha = 0.25f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentTeal.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Customer Credit & Instalment Management",
                    fontSize = 13.sp,
                    color = AccentTeal,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Promotional announcement banner from remote config
            config?.promotionalBanner?.let { banner ->
                if (banner.isNotBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = AccentTeal,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = banner,
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // 2. Action Button 1: Merchant Login
            Card(
                onClick = { viewModel.navigateTo(Screen.MerchantLogin) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("merchant_login_portal_button")
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFCCFBF1),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Merchant Portal",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Merchant Login",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Mobile OTP login for merchants, ledgers & AutoPay",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 16.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Open Merchant Login",
                        tint = PrimaryTeal,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Action Button 2: Admin Login
            Card(
                onClick = { viewModel.navigateTo(Screen.AdminLogin) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_login_portal_button")
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF334155),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Portal",
                                tint = AccentTeal,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Admin Login",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Central Super Admin control & remote configuration",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 16.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Open Admin Login",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Value Proposition Features Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureIndicator(icon = Icons.Default.Security, text = "Bank-Grade")
                FeatureIndicator(icon = Icons.Default.Autorenew, text = "UPI AutoPay")
                FeatureIndicator(icon = Icons.Default.PictureAsPdf, text = "A4 Ledger PDF")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Subtitle / Footer
        Text(
            text = "Smart Credit • Customer Credit & Instalment Management",
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        )
    }
}

@Composable
private fun FeatureIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentTeal,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
        )
    }
}

// Alias for StartupScreen for backward compatibility
@Composable
fun StartupScreen(
    viewModel: SmartCreditViewModel,
    config: AppConfiguration?
) {
    WelcomeScreen(viewModel = viewModel, config = config)
}
