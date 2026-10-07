package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.AccentTeal
import com.example.viewmodel.Screen
import com.example.viewmodel.SmartCreditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDashboardScreen(viewModel: SmartCreditViewModel) {
    BackHandler {
        viewModel.signOut()
    }

    val merchant by viewModel.activeMerchant.collectAsState()
    val config by viewModel.appConfig.collectAsState()

    if (merchant == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Session expired. Please sign in again.")
        }
        return
    }

    val m = merchant!!
    val customers by viewModel.getMerchantCustomers(m.id).collectAsState(initial = emptyList())
    val transactions by viewModel.getMerchantTransactions(m.id).collectAsState(initial = emptyList())
    val instalmentItems by viewModel.getMerchantInstalmentItems(m.id).collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) }
    // 0: Home / Dashboard
    // 1: Customers
    // 2: Instalments
    // 3: AutoPay
    // 4: Ledger

    var showAddCustomerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = m.businessName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Merchant ID: ${m.id} • Plan: ${m.planType.name}",
                            fontSize = 11.sp,
                            color = AccentTeal
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.MerchantStaffScreen) },
                        modifier = Modifier.testTag("nav_staff_btn")
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = "Staff", tint = Color.White)
                    }
                    IconButton(
                        onClick = { viewModel.signOut() },
                        modifier = Modifier.testTag("merchant_signout_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = Color(0xFF94A3B8))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Customers") },
                    label = { Text("Customers") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Instalments") },
                    label = { Text("Instalments") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Autorenew, contentDescription = "AutoPay") },
                    label = { Text("AutoPay") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Ledger") },
                    label = { Text("Ledger") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 1 || selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showAddCustomerDialog = true },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = { Text("Add Customer") },
                    containerColor = PrimaryTeal,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_customer")
                )
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> MerchantHomeTab(
                    merchant = m,
                    customers = customers,
                    transactions = transactions,
                    instalmentItems = instalmentItems,
                    viewModel = viewModel,
                    onAddCustomerClick = { showAddCustomerDialog = true }
                )
                1 -> MerchantCustomersTab(
                    customers = customers,
                    onCustomerClick = { cust ->
                        viewModel.navigateTo(Screen.CustomerDetail(cust.id))
                    },
                    onCreditClick = { cust ->
                        viewModel.navigateTo(Screen.GiveCredit(cust.id))
                    },
                    onRepayClick = { cust ->
                        viewModel.navigateTo(Screen.AcceptRepayment(cust.id))
                    },
                    onAutoPayClick = { cust ->
                        viewModel.navigateTo(Screen.AutoPaySetup(cust.id))
                    }
                )
                2 -> InstalmentsScheduleTab(
                    instalmentItems = instalmentItems,
                    customers = customers,
                    onPlanNew = {
                        if (customers.isNotEmpty()) {
                            viewModel.navigateTo(Screen.CreateInstalment(customers.first().id))
                        } else {
                            viewModel.showToast("Please register a customer first.")
                        }
                    }
                )
                3 -> AutoPayManagementTab(
                    customers = customers,
                    viewModel = viewModel
                )
                4 -> MerchantTransactionsTab(
                    transactions = transactions,
                    customers = customers,
                    onReverse = { txnId ->
                        viewModel.reverseTransaction(txnId, "Merchant requested reversal")
                    }
                )
            }
        }
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onConfirm = { name, phone, address ->
                viewModel.addNewCustomer(name, phone, address, 0.0, "")
                showAddCustomerDialog = false
            }
        )
    }
}

@Composable
fun MerchantHomeTab(
    merchant: MerchantProfile,
    customers: List<Customer>,
    transactions: List<LedgerTransaction>,
    instalmentItems: List<InstalmentItem>,
    viewModel: SmartCreditViewModel,
    onAddCustomerClick: () -> Unit
) {
    val totalOutstandingPaise = customers.sumOf { it.currentBalancePaise }
    val todayStart = remember {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.timeInMillis
    }
    val todayRepaymentsPaise = transactions.filter {
        (it.transactionType == TransactionType.CASH_REPAYMENT || it.transactionType == TransactionType.ONLINE_REPAYMENT) && it.timestamp >= todayStart
    }.sumOf { it.amountPaise }

    val activeMandatesCount = customers.count { it.autoPayMandateStatus == MandateStatus.ACTIVE }
    val pendingMandatesCount = customers.count {
        it.autoPayMandateStatus == MandateStatus.PENDING_CUSTOMER_APPROVAL ||
                it.autoPayMandateStatus == MandateStatus.PENDING_PROVIDER_CONFIRMATION ||
                it.autoPayMandateStatus == MandateStatus.ACTIVATION_LINK_SENT
    }

    val upcomingInstalmentsCount = instalmentItems.count { it.status == InstalmentStatus.UPCOMING }
    val overdueInstalmentsCount = instalmentItems.count { it.status == InstalmentStatus.OVERDUE }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-Impact Financial Overview Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "TOTAL OUTSTANDING CREDIT (UDHAAR)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${totalOutstandingPaise / 100.0}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFF87171)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color(0xFF233554))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Credit Sales", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("₹${merchant.totalCreditPaise / 100.0}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Confirmed Repayments", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("₹${merchant.totalRepaymentPaise / 100.0}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AccentTeal)
                        }
                        Column {
                            Text("Today's Collections", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("₹${todayRepaymentsPaise / 100.0}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Native Financial Bar Breakdown Visualization
                    val totalVolume = (merchant.totalCreditPaise + merchant.totalRepaymentPaise).coerceAtLeast(1L)
                    val creditRatio = (merchant.totalCreditPaise.toFloat() / totalVolume).coerceIn(0.05f, 0.95f)

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Portfolio Ratio Breakdown", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("Credit ${(creditRatio * 100).toInt()}% • Repaid ${((1 - creditRatio) * 100).toInt()}%", fontSize = 10.sp, color = AccentTeal, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF233554))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(creditRatio)
                                    .background(Color(0xFFEF4444))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f - creditRatio)
                                    .background(AccentTeal)
                            )
                        }
                    }
                }
            }
        }

        // Secondary Metric Grid: Instalments & AutoPay Status
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Active AutoPay", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        Text("$activeMandatesCount Mandates", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryTeal)
                        Text("$pendingMandatesCount pending activations", fontSize = 10.sp, color = Color(0xFFF59E0B))
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Instalments", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        Text("$upcomingInstalmentsCount Upcoming", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("$overdueInstalmentsCount overdue instalments", fontSize = 10.sp, color = if (overdueInstalmentsCount > 0) Color(0xFFDC2626) else Color(0xFF64748B))
                    }
                }
            }
        }

        // Quick Action Buttons
        item {
            Text(
                text = "Quick Actions",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionChip(
                    icon = Icons.Default.PersonAdd,
                    label = "Add Customer",
                    onClick = onAddCustomerClick,
                    modifier = Modifier.weight(1f).testTag("quick_add_customer")
                )
                QuickActionChip(
                    icon = Icons.Default.RemoveCircleOutline,
                    label = "Give Credit",
                    onClick = { viewModel.navigateTo(Screen.GiveCredit()) },
                    modifier = Modifier.weight(1f).testTag("quick_give_credit")
                )
                QuickActionChip(
                    icon = Icons.Default.AddCircleOutline,
                    label = "Repayment",
                    onClick = { viewModel.navigateTo(Screen.AcceptRepayment()) },
                    modifier = Modifier.weight(1f).testTag("quick_repayment")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionChip(
                    icon = Icons.Default.Calculate,
                    label = "Instalment Plan",
                    onClick = {
                        if (customers.isNotEmpty()) viewModel.navigateTo(Screen.CreateInstalment(customers.first().id))
                        else viewModel.showToast("Add a customer first")
                    },
                    modifier = Modifier.weight(1f).testTag("quick_instalment_plan")
                )
                QuickActionChip(
                    icon = Icons.Default.Autorenew,
                    label = "Set Up AutoPay",
                    onClick = {
                        if (customers.isNotEmpty()) viewModel.navigateTo(Screen.AutoPaySetup(customers.first().id))
                        else viewModel.showToast("Add a customer first")
                    },
                    modifier = Modifier.weight(1f).testTag("quick_setup_autopay")
                )
                QuickActionChip(
                    icon = Icons.Default.CardMembership,
                    label = "Subscription",
                    onClick = { viewModel.upgradeMerchantToPremium() },
                    modifier = Modifier.weight(1f).testTag("quick_subscription")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cloud Firestore Backup & Restore Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionChip(
                    icon = Icons.Default.CloudUpload,
                    label = "Backup to Cloud",
                    onClick = { viewModel.backupDataToFirestore { _, _ -> } },
                    modifier = Modifier.weight(1f).testTag("quick_cloud_backup")
                )
                QuickActionChip(
                    icon = Icons.Default.CloudDownload,
                    label = "Restore Cloud",
                    onClick = { viewModel.restoreDataFromFirestore { _, _ -> } },
                    modifier = Modifier.weight(1f).testTag("quick_cloud_restore")
                )
            }
        }

        // Customer Ledger List Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customer Ledgers",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "${customers.size} Registered",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        if (customers.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No customers registered yet", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Tap 'Add Customer' to start recording credit & instalment plans.", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }
        } else {
            items(customers.take(5)) { cust ->
                CustomerLedgerCard(
                    customer = cust,
                    onCreditClick = { viewModel.navigateTo(Screen.GiveCredit(cust.id)) },
                    onRepayClick = { viewModel.navigateTo(Screen.AcceptRepayment(cust.id)) },
                    onCardClick = { viewModel.navigateTo(Screen.CustomerDetail(cust.id)) },
                    onAutoPayClick = { viewModel.navigateTo(Screen.AutoPaySetup(cust.id)) }
                )
            }
        }
    }
}

@Composable
fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
        }
    }
}

@Composable
fun CustomerLedgerCard(
    customer: Customer,
    onCreditClick: () -> Unit,
    onRepayClick: () -> Unit,
    onCardClick: () -> Unit,
    onAutoPayClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("cust_card_${customer.mobileNumber}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.fullName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    // Prompt rule: Display Customer ID as Mobile Number
                    Text(
                        text = "Customer ID: ${customer.mobileNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryTeal
                    )
                    Text(
                        text = customer.fullAddress.take(45),
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Balance Due",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "₹${customer.currentBalancePaise / 100.0}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (customer.currentBalancePaise > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (customer.autoPayMandateStatus) {
                            MandateStatus.ACTIVE -> Color(0xFFDCFCE7)
                            MandateStatus.PENDING_CUSTOMER_APPROVAL,
                            MandateStatus.PENDING_PROVIDER_CONFIRMATION,
                            MandateStatus.ACTIVATION_LINK_SENT -> Color(0xFFFEF3C7)
                            else -> Color(0xFFF1F5F9)
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = if (customer.autoPayMandateStatus == MandateStatus.ACTIVE) "AutoPay Active"
                            else if (customer.autoPayMandateStatus == MandateStatus.NOT_SET_UP || customer.autoPayMandateStatus == MandateStatus.NOT_CONFIGURED) "AutoPay Not Set Up"
                            else "AutoPay Pending",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (customer.autoPayMandateStatus == MandateStatus.ACTIVE) Color(0xFF16A34A)
                            else if (customer.autoPayMandateStatus == MandateStatus.NOT_SET_UP || customer.autoPayMandateStatus == MandateStatus.NOT_CONFIGURED) Color(0xFF64748B)
                            else Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCreditClick,
                    modifier = Modifier.weight(1f).height(36.dp).testTag("card_credit_btn_${customer.mobileNumber}"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Give Credit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onRepayClick,
                    modifier = Modifier.weight(1f).height(36.dp).testTag("card_repay_btn_${customer.mobileNumber}"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Repayment", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onAutoPayClick,
                    modifier = Modifier.weight(1f).height(36.dp).testTag("card_autopay_btn_${customer.mobileNumber}"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF99F6E4)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("AutoPay", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun MerchantCustomersTab(
    customers: List<Customer>,
    onCustomerClick: (Customer) -> Unit,
    onCreditClick: (Customer) -> Unit,
    onRepayClick: (Customer) -> Unit,
    onAutoPayClick: (Customer) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = customers.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.mobileNumber.contains(searchQuery)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by customer name or mobile number...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryTeal) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("customer_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchQuery.isEmpty()) "No customers found" else "No matching customers for '$searchQuery'",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { cust ->
                    CustomerLedgerCard(
                        customer = cust,
                        onCreditClick = { onCreditClick(cust) },
                        onRepayClick = { onRepayClick(cust) },
                        onCardClick = { onCustomerClick(cust) },
                        onAutoPayClick = { onAutoPayClick(cust) }
                    )
                }
            }
        }
    }
}

@Composable
fun InstalmentsScheduleTab(
    instalmentItems: List<InstalmentItem>,
    customers: List<Customer>,
    onPlanNew: () -> Unit
) {
    val custMap = customers.associateBy { it.id }
    val shortDate = remember { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()) }

    var filterStatus by remember { mutableStateOf<InstalmentStatus?>(null) }

    val filtered = if (filterStatus == null) instalmentItems else instalmentItems.filter { it.status == filterStatus }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Instalment Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                Text("Monthly repayments and collection calendar", fontSize = 12.sp, color = Color(0xFF64748B))
            }

            Button(
                onClick = onPlanNew,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("new_instalment_plan_btn")
            ) {
                Text("+ New Plan", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Chips
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                selected = filterStatus == null,
                onClick = { filterStatus = null },
                label = { Text("All (${instalmentItems.size})") }
            )
            FilterChip(
                selected = filterStatus == InstalmentStatus.UPCOMING,
                onClick = { filterStatus = InstalmentStatus.UPCOMING },
                label = { Text("Upcoming") }
            )
            FilterChip(
                selected = filterStatus == InstalmentStatus.OVERDUE,
                onClick = { filterStatus = InstalmentStatus.OVERDUE },
                label = { Text("Overdue") }
            )
            FilterChip(
                selected = filterStatus == InstalmentStatus.PAID,
                onClick = { filterStatus = InstalmentStatus.PAID },
                label = { Text("Paid") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No instalments scheduled yet", fontWeight = FontWeight.SemiBold)
                    Text("Create an instalment plan for a customer from their profile.", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { item ->
                    val cust = custMap[item.customerId]
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(cust?.fullName ?: "Customer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Customer ID: ${cust?.mobileNumber ?: item.customerId} • Inst #${item.instalmentNumber}", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${item.amountDuePaise / 100.0}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryTeal)
                                    Text("Due: ${shortDate.format(java.util.Date(item.dueDate))}", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Principal: ₹${item.principalComponentPaise / 100.0} | Interest: ₹${item.interestComponentPaise / 100.0}", fontSize = 11.sp, color = Color(0xFF64748B))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (item.status) {
                                        InstalmentStatus.PAID -> Color(0xFFDCFCE7)
                                        InstalmentStatus.OVERDUE -> Color(0xFFFEE2E2)
                                        else -> Color(0xFFE0F2FE)
                                    }
                                ) {
                                    Text(
                                        text = item.status.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (item.status) {
                                            InstalmentStatus.PAID -> Color(0xFF16A34A)
                                            InstalmentStatus.OVERDUE -> Color(0xFFDC2626)
                                            else -> PrimaryTeal
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AutoPayManagementTab(
    customers: List<Customer>,
    viewModel: SmartCreditViewModel
) {
    val activeList = customers.filter { it.autoPayMandateStatus == MandateStatus.ACTIVE }
    val pendingList = customers.filter {
        it.autoPayMandateStatus == MandateStatus.PENDING_CUSTOMER_APPROVAL ||
                it.autoPayMandateStatus == MandateStatus.PENDING_PROVIDER_CONFIRMATION ||
                it.autoPayMandateStatus == MandateStatus.ACTIVATION_LINK_SENT ||
                it.autoPayMandateStatus == MandateStatus.SETUP_REQUIRED
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Customer UPI AutoPay Dashboard", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
            Text("Automated recurring collections authorised via NPCI e-Mandate / UPI AutoPay.", fontSize = 12.sp, color = Color(0xFF64748B))
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("ACTIVE MANDATES", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text("${activeList.size}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AccentTeal)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("PENDING APPROVAL", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text("${pendingList.size}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    }
                }
            }
        }

        item {
            Text("Active Mandates", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        if (activeList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No active AutoPay mandates yet", color = Color(0xFF64748B), fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(activeList) { cust ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(cust.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Customer ID: ${cust.mobileNumber} • Mandate: ${cust.autoPayMandateId ?: "UMN-ACTIVE"}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Button(
                            onClick = { viewModel.updateCustomerMandate(cust.id, MandateStatus.CANCELLED, null) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Cancel", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        item {
            Text("Pending Activations", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        if (pendingList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No pending AutoPay setups", color = Color(0xFF64748B), fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(pendingList) { cust ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(cust.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Customer ID: ${cust.mobileNumber} • Status: ${cust.autoPayMandateStatus.name}", fontSize = 11.sp, color = Color(0xFFF59E0B))
                        }
                        Button(
                            onClick = { viewModel.navigateTo(Screen.AutoPaySetup(cust.id)) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Setup", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// Dialog: Add Customer (Mandatory 3 fields in exact order)
@Composable
fun MerchantTransactionsTab(
    transactions: List<LedgerTransaction>,
    customers: List<Customer>,
    onReverse: (String) -> Unit
) {
    val custMap = customers.associateBy { it.id }
    val shortDate = remember { java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Append-Only Transaction History", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
        Text("All entries are mathematically audited and irreversible except via signed reversal.", fontSize = 12.sp, color = Color(0xFF64748B))

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions recorded yet.", color = Color(0xFF64748B))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions) { txn ->
                    val cust = custMap[txn.customerId]
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth().testTag("txn_item_${txn.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = cust?.fullName ?: txn.customerId,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Customer ID: ${cust?.mobileNumber ?: txn.customerId} • ${shortDate.format(java.util.Date(txn.timestamp))}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Text(
                                    text = (if (txn.transactionType == TransactionType.CREDIT_SALE) "+ " else "- ") + "₹${txn.amountPaise / 100.0}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (txn.transactionType == TransactionType.CREDIT_SALE) Color(0xFFDC2626) else Color(0xFF16A34A)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${txn.description} (Channel: ${txn.paymentSource.name})",
                                fontSize = 12.sp,
                                color = Color(0xFF334155)
                            )

                            if (txn.isReversed) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = Color(0xFFFEE2E2),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "REVERSED (${txn.linkedReversalTxnId})",
                                        color = Color(0xFFB91C1C),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (txn.transactionType != TransactionType.REVERSAL) {
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(
                                    onClick = { onReverse(txn.id) },
                                    modifier = Modifier.height(28.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Audited Reversal", fontSize = 11.sp, color = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Customer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Customer ID will automatically be set to the customer's mobile number.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                // Mandatory field 1: Customer Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("1. Customer Full Name * (Mandatory)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_cust_name")
                )

                // Mandatory field 2: Mobile Number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10) phone = it },
                    label = { Text("2. Mobile Number * (Displayed Customer ID)") },
                    prefix = { Text("+91 ", fontWeight = FontWeight.SemiBold) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_cust_phone")
                )

                // Mandatory field 3: Full Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("3. Full Address * (Mandatory)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("add_cust_address")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.length == 10 && address.isNotBlank()) {
                        onConfirm(name.trim(), phone.trim(), address.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                modifier = Modifier.testTag("add_cust_submit")
            ) {
                Text("Register Customer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
