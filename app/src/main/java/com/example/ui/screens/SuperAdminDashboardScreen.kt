package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.*
import com.example.viewmodel.SmartCreditViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboardScreen(viewModel: SmartCreditViewModel) {
    BackHandler {
        viewModel.signOut()
    }

    val activeAdmin by viewModel.activeAdmin.collectAsState()
    val config by viewModel.appConfig.collectAsState()
    val merchants by viewModel.allMerchants.collectAsState()
    val customers by viewModel.globalCustomers.collectAsState()
    val transactions by viewModel.globalTransactions.collectAsState()
    val auditLogs by viewModel.allAuditLogs.collectAsState()
    val releases by viewModel.allReleases.collectAsState()

    var selectedAdminSection by remember { mutableStateOf(0) }
    // 0: Master Overview & Finance
    // 1: Merchant Accounts Management
    // 2: App Appearance & Branding (Real-time Customisation)
    // 3: Remote Config & Feature Flags
    // 4: Pricing & Platform Fees (0%-10%)
    // 5: App Releases & Updates
    // 6: Immutable Audit Logs

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Super Admin Command Center",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${activeAdmin?.fullName ?: "dr.saddam.co@gmail.com"} • ROLE: ${activeAdmin?.role?.name ?: "SUPER_ADMIN"}",
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.signOut() },
                        modifier = Modifier.testTag("admin_signout_action")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Horizontal Admin Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedAdminSection,
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                edgePadding = 12.dp
            ) {
                Tab(
                    selected = selectedAdminSection == 0,
                    onClick = { selectedAdminSection = 0 },
                    text = { Text("Overview & Finance") },
                    icon = { Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedAdminSection == 1,
                    onClick = { selectedAdminSection = 1 },
                    text = { Text("Merchants (${merchants.size})") },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedAdminSection == 2,
                    onClick = { selectedAdminSection = 2 },
                    text = { Text("Appearance & Branding") },
                    icon = { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedAdminSection == 3,
                    onClick = { selectedAdminSection = 3 },
                    text = { Text("Feature Flags") },
                    icon = { Icon(Icons.Default.ToggleOn, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedAdminSection == 4,
                    onClick = { selectedAdminSection = 4 },
                    text = { Text("Pricing & Fees") },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedAdminSection == 5,
                    onClick = { selectedAdminSection = 5 },
                    text = { Text("Releases") },
                    icon = { Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedAdminSection == 6,
                    onClick = { selectedAdminSection = 6 },
                    text = { Text("Audit Trail (${auditLogs.size})") },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedAdminSection) {
                    0 -> AdminOverviewSection(merchants, customers, transactions, config)
                    1 -> AdminMerchantsSection(merchants, viewModel)
                    2 -> AdminAppearanceSection(config, viewModel)
                    3 -> AdminFeatureFlagsSection(config, viewModel)
                    4 -> AdminPricingSection(config, viewModel)
                    5 -> AdminReleasesSection(releases, viewModel)
                    6 -> AdminAuditTrailSection(auditLogs)
                }
            }
        }
    }
}

@Composable
fun AdminOverviewSection(
    merchants: List<MerchantProfile>,
    customers: List<Customer>,
    transactions: List<LedgerTransaction>,
    config: AppConfiguration?
) {
    val totalCredit = transactions.filter { it.transactionType == TransactionType.CREDIT_SALE }.sumOf { it.amountPaise }
    val totalRepayment = transactions.filter { it.transactionType == TransactionType.CASH_REPAYMENT || it.transactionType == TransactionType.ONLINE_REPAYMENT }.sumOf { it.amountPaise }
    val totalPlatformFeesAccrued = merchants.sumOf { it.platformFeesAccruedPaise }
    val totalPlatformFeesSettled = merchants.sumOf { it.platformFeesSettledPaise }
    val premiumCount = merchants.count { it.planType == SubscriptionPlan.PREMIUM }
    val premiumRevenueRupees = premiumCount * (config?.premiumMonthlyPriceRupees ?: 500)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Financial Health & Settlement Monitor",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Real-time totals computed strictly from append-only ledger entries.",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("TOTAL NET PLATFORM EARNINGS", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${(totalPlatformFeesAccrued / 100.0) + premiumRevenueRupees}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Free Plan Fees Accrued", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("₹${totalPlatformFeesAccrued / 100.0}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Subscription Revenue", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("₹$premiumRevenueRupees", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                        }
                        Column {
                            Text("Settled To UPI", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("₹${totalPlatformFeesSettled / 100.0}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Merchants", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text("${merchants.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("${merchants.count { it.accountStatus == AccountStatus.ACTIVE }} Active • $premiumCount Premium", fontSize = 10.sp, color = Color(0xFF0F766E))
                    }
                }
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Ledger Customers", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text("${customers.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Active customer profiles", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Gross Credit Extended", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text("₹${totalCredit / 100.0}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                }
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Gross Repayments", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text("₹${totalRepayment / 100.0}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Hub, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aggregator & Webhook Pipeline Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Customer UPI AutoPay Gateway: Sandbox Ready / Provider Mandates Active\n• Merchant Premium AutoPay: Recurring e-Mandate Active\n• Transactional SMS Service: DLT Template Header 'SMTCRD' Configured", fontSize = 12.sp, color = Color(0xFF334155), lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
fun AdminMerchantsSection(
    merchants: List<MerchantProfile>,
    viewModel: SmartCreditViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMerchantForAction by remember { mutableStateOf<MerchantProfile?>(null) }
    var actionDialogType by remember { mutableStateOf<String?>(null) } // "SUSPEND", "PLAN"

    val filtered = merchants.filter {
        it.businessName.contains(searchQuery, ignoreCase = true) ||
                it.contactNumber.contains(searchQuery) ||
                it.id.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search merchant by ID, business name, or phone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("admin_search_merchant_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filtered) { m ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().testTag("admin_merch_card_${m.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(m.businessName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                Text("ID: ${m.id} • ${m.contactNumber} • ${m.email}", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text("Authorised: ${m.authorisedPersonName} • UPI: ${m.businessUpiId}", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (m.accountStatus) {
                                    AccountStatus.ACTIVE -> Color(0xFFDCFCE7)
                                    AccountStatus.SUSPENDED -> Color(0xFFFEE2E2)
                                    AccountStatus.RESTRICTED -> Color(0xFFFEF3C7)
                                }
                            ) {
                                Text(
                                    text = m.accountStatus.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (m.accountStatus) {
                                        AccountStatus.ACTIVE -> Color(0xFF16A34A)
                                        AccountStatus.SUSPENDED -> Color(0xFFDC2626)
                                        AccountStatus.RESTRICTED -> Color(0xFFB45309)
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Address: ${m.businessAddress}", fontSize = 11.sp, color = Color(0xFF475569))

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Plan: ${m.planType.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Credit: ₹${m.totalCreditPaise / 100.0}", fontSize = 12.sp)
                            Text("Repaid: ₹${m.totalRepaymentPaise / 100.0}", fontSize = 12.sp)
                            Text("Accrued Fees: ₹${m.platformFeesAccruedPaise / 100.0}", fontSize = 12.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (m.accountStatus == AccountStatus.ACTIVE) {
                                OutlinedButton(
                                    onClick = {
                                        selectedMerchantForAction = m
                                        actionDialogType = "SUSPEND"
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp).testTag("btn_suspend_${m.id}")
                                ) {
                                    Text("Suspend Store", fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.setMerchantAccountStatus(m.id, AccountStatus.ACTIVE, "Super Admin reactivated store")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Text("Reactivate", fontSize = 11.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    val nextPlan = if (m.planType == SubscriptionPlan.FREE) SubscriptionPlan.PREMIUM else SubscriptionPlan.FREE
                                    viewModel.switchPlanMerchant(m.id, nextPlan)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text(if (m.planType == SubscriptionPlan.FREE) "Set Premium" else "Set Free", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (actionDialogType == "SUSPEND" && selectedMerchantForAction != null) {
        var reason by remember { mutableStateOf("Compliance check / Policy violation") }
        AlertDialog(
            onDismissRequest = { actionDialogType = null },
            title = { Text("Suspend Merchant Account") },
            text = {
                Column {
                    Text("Suspending ${selectedMerchantForAction!!.businessName} will prevent store staff from opening credit transactions.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Recorded Suspension Reason *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setMerchantAccountStatus(selectedMerchantForAction!!.id, AccountStatus.SUSPENDED, reason)
                        actionDialogType = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Confirm Suspension")
                }
            },
            dismissButton = {
                TextButton(onClick = { actionDialogType = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminAppearanceSection(
    config: AppConfiguration?,
    viewModel: SmartCreditViewModel
) {
    val cfg = config ?: AppConfiguration()
    var appName by remember { mutableStateOf(cfg.appDisplayName) }
    var brandName by remember { mutableStateOf(cfg.merchantBrandName) }
    var primaryHex by remember { mutableStateOf(cfg.primaryColorHex) }
    var secondaryHex by remember { mutableStateOf(cfg.secondaryColorHex) }
    var accentHex by remember { mutableStateOf(cfg.accentColorHex) }
    var visualStyle by remember { mutableStateOf(cfg.visualStyle) }
    var isDark by remember { mutableStateOf(cfg.isDarkMode) }
    var banner by remember { mutableStateOf(cfg.promotionalBanner) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Complete App Appearance & Branding", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Centrally manage in-app colours, themes, display names, and merchant brand labels without requiring a new APK release.", fontSize = 12.sp, color = Color(0xFF64748B))
        }

        item {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Branding & Labels", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        label = { Text("App Display Name") },
                        modifier = Modifier.fillMaxWidth().testTag("conf_app_name")
                    )
                    OutlinedTextField(
                        value = brandName,
                        onValueChange = { brandName = it },
                        label = { Text("Merchant-Facing Brand Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = banner,
                        onValueChange = { banner = it },
                        label = { Text("Global Promotional Banner / Notice") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Color Palette (Hex Codes)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = primaryHex,
                            onValueChange = { primaryHex = it },
                            label = { Text("Primary Hex") },
                            modifier = Modifier.weight(1f).testTag("conf_primary_color")
                        )
                        OutlinedTextField(
                            value = secondaryHex,
                            onValueChange = { secondaryHex = it },
                            label = { Text("Secondary Hex") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = accentHex,
                        onValueChange = { accentHex = it },
                        label = { Text("Accent Color Hex") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick Palette Preset Selectors
                    Text("Pre-Approved Financial Palettes:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = primaryHex == "#0284C7",
                            onClick = {
                                primaryHex = "#0284C7"
                                secondaryHex = "#0F766E"
                                accentHex = "#F59E0B"
                            },
                            label = { Text("Sky Blue") }
                        )
                        FilterChip(
                            selected = primaryHex == "#059669",
                            onClick = {
                                primaryHex = "#059669"
                                secondaryHex = "#0284C7"
                                accentHex = "#D97706"
                            },
                            label = { Text("Emerald Fin") }
                        )
                        FilterChip(
                            selected = primaryHex == "#4F46E5",
                            onClick = {
                                primaryHex = "#4F46E5"
                                secondaryHex = "#7C3AED"
                                accentHex = "#EC4899"
                            },
                            label = { Text("Indigo Royal") }
                        )
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Themes & Visual Style", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = isDark, onCheckedChange = { isDark = it })
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (isDark) "Dark Theme Mode Enabled" else "Light Theme Mode Enabled")
                    }

                    Text("Interface Card Architecture:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = visualStyle == "Modern Professional",
                            onClick = { visualStyle = "Modern Professional" },
                            label = { Text("Modern Pro") }
                        )
                        FilterChip(
                            selected = visualStyle == "Rounded-Card Style",
                            onClick = { visualStyle = "Rounded-Card Style" },
                            label = { Text("Rounded Card") }
                        )
                        FilterChip(
                            selected = visualStyle == "Compact Dashboard",
                            onClick = { visualStyle = "Compact Dashboard" },
                            label = { Text("Compact") }
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.updateAppearanceAndBranding(
                        appName = appName,
                        brandName = brandName,
                        primaryHex = primaryHex,
                        secondaryHex = secondaryHex,
                        accentHex = accentHex,
                        visualStyle = visualStyle,
                        isDark = isDark,
                        promotionalBanner = banner
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("publish_appearance_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publish Appearance & Branding Remotely", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminFeatureFlagsSection(
    config: AppConfiguration?,
    viewModel: SmartCreditViewModel
) {
    val cfg = config ?: AppConfiguration()
    var autoPay by remember { mutableStateOf(cfg.featureCustomerAutoPay) }
    var subAutoPay by remember { mutableStateOf(cfg.featureMerchantSubscriptionAutoPay) }
    var pdf by remember { mutableStateOf(cfg.featurePdfStatements) }
    var wa by remember { mutableStateOf(cfg.featureWhatsAppShare) }
    var sms by remember { mutableStateOf(cfg.featureSmsNotifications) }
    var staff by remember { mutableStateOf(cfg.featureStaffManagement) }
    var maintenanceNotice by remember { mutableStateOf(cfg.maintenanceNotice) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Central Feature Toggles & System Flags", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Instantly enable or safely suspend operations across all merchant instances.", fontSize = 12.sp, color = Color(0xFF64748B))
        }

        item {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Customer UPI AutoPay Repayments", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Allows merchants to set up recurring UPI mandates with customers", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = autoPay, onCheckedChange = { autoPay = it }, modifier = Modifier.testTag("toggle_autopay"))
                    }
                    Divider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Merchant Subscription AutoPay", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Monthly automated recurring payment for Premium", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = subAutoPay, onCheckedChange = { subAutoPay = it })
                    }
                    Divider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PDF Statements Generator", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Generate printable A4 account statements", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = pdf, onCheckedChange = { pdf = it })
                    }
                    Divider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("WhatsApp Statement Sharing", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Dispatch PDF statement securely via Android Sharesheet", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = wa, onCheckedChange = { wa = it })
                    }
                    Divider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Transactional SMS Reminders", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Send transaction alerts and overdue reminders", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = sms, onCheckedChange = { sms = it })
                    }
                    Divider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Store Staff Access Delegation", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Permit merchant owners to add cashiers and accountants", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = staff, onCheckedChange = { staff = it })
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Emergency Maintenance Broadcast", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = maintenanceNotice,
                        onValueChange = { maintenanceNotice = it },
                        label = { Text("Maintenance Text (Leave blank if system is normal)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.updateFeatureFlags(
                        autoPay = autoPay,
                        subscriptionAutoPay = subAutoPay,
                        pdfStatements = pdf,
                        whatsappShare = wa,
                        smsNotifications = sms,
                        staffManagement = staff,
                        maintenanceNotice = maintenanceNotice
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_feature_flags_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Feature Flags & State", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminPricingSection(
    config: AppConfiguration?,
    viewModel: SmartCreditViewModel
) {
    val cfg = config ?: AppConfiguration()
    var premiumPriceStr by remember { mutableStateOf(cfg.premiumMonthlyPriceRupees.toString()) }
    var feePercentStr by remember { mutableStateOf(cfg.freePlanPlatformFeePercent.toString()) }
    var settlementUpi by remember { mutableStateOf(cfg.proposedSettlementUpiId) }
    var auditReason by remember { mutableStateOf("Periodic commercial fee review") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Pricing & Platform Fee Rules", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Admin controls for Premium monthly price and Free-plan platform fee percentage (0.0% to 10.0%). Historical completed transactions are strictly immutable.", fontSize = 12.sp, color = Color(0xFF64748B))
        }

        item {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("1. Premium Plan Subscription Price", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = premiumPriceStr,
                        onValueChange = { premiumPriceStr = it },
                        label = { Text("Monthly Price in Rupees (₹) [Default: 500]") },
                        modifier = Modifier.fillMaxWidth().testTag("pricing_premium_input")
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. Free Plan Platform Fee Percentage (0.0% – 10.0%)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = feePercentStr,
                        onValueChange = { feePercentStr = it },
                        label = { Text("Platform Fee % (e.g. 0.0, 1.25, 2.0, 5.0, 10.0)") },
                        modifier = Modifier.fillMaxWidth().testTag("pricing_fee_percent_input")
                    )

                    // Quick percentage chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("0.0", "1.0", "2.0", "5.0", "10.0").forEach { preset ->
                            FilterChip(
                                selected = feePercentStr == preset,
                                onClick = { feePercentStr = preset },
                                label = { Text("$preset%") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("3. Settlement UPI Destination", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = settlementUpi,
                        onValueChange = { settlementUpi = it },
                        label = { Text("Proposed Platform Settlement UPI ID") },
                        modifier = Modifier.fillMaxWidth().testTag("pricing_settlement_upi")
                    )

                    OutlinedTextField(
                        value = auditReason,
                        onValueChange = { auditReason = it },
                        label = { Text("Audited Regulatory Justification *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val price = premiumPriceStr.toIntOrNull() ?: 500
                    val pct = feePercentStr.toDoubleOrNull() ?: 2.0
                    viewModel.updatePricingAndFees(price, pct, settlementUpi, auditReason)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_pricing_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Publish Pricing Update", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminReleasesSection(
    releases: List<AppRelease>,
    viewModel: SmartCreditViewModel
) {
    var showNewReleaseDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Software Releases & Versions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Manage compiled app releases and version policies.", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            Button(
                onClick = { showNewReleaseDialog = true },
                modifier = Modifier.testTag("publish_new_release_btn")
            ) {
                Text("New Release")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(releases) { rel ->
                Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("v${rel.versionName} (Build ${rel.versionCode})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            if (rel.isMandatory) {
                                Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(4.dp)) {
                                    Text("MANDATORY UPDATE", color = Color(0xFFDC2626), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(rel.releaseNotes, fontSize = 12.sp, color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Published By: ${rel.publishedBy}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }

    if (showNewReleaseDialog) {
        var vName by remember { mutableStateOf("1.1.0") }
        var vCodeStr by remember { mutableStateOf("2") }
        var notes by remember { mutableStateOf("Security hardening and new repayment analytics.") }
        var isMandatory by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showNewReleaseDialog = false },
            title = { Text("Publish App Release Metadata") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = vName,
                        onValueChange = { vName = it },
                        label = { Text("Version Name (e.g. 1.1.0)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = vCodeStr,
                        onValueChange = { vCodeStr = it },
                        label = { Text("Version Code (Integer)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Release Notes / What's New") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isMandatory, onCheckedChange = { isMandatory = it })
                        Text("Force Mandatory Update", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val vc = vCodeStr.toIntOrNull() ?: 2
                        viewModel.publishAppRelease(vc, vName, notes, isMandatory)
                        showNewReleaseDialog = false
                    }
                ) {
                    Text("Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewReleaseDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminAuditTrailSection(auditLogs: List<AuditLog>) {
    val shortDate = SimpleDateFormat("dd MMM, HH:mm:ss", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Immutable Administrative Audit Trail", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Legally defensible log of all configuration adjustments, actor IDs, timestamps, and justifications.", fontSize = 12.sp, color = Color(0xFF64748B))

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(auditLogs) { log ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.actionType, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0284C7))
                            Text(shortDate.format(Date(log.timestamp)), fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(log.reasonOrDetails, fontSize = 12.sp, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Actor: ${log.actorEmailOrId} (${log.actorRole}) • Target: ${log.targetEntity}/${log.targetEntityId}", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}
