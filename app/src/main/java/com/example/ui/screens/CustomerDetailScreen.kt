package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.AccentTeal
import com.example.util.PdfStatementGenerator
import com.example.viewmodel.Screen
import com.example.viewmodel.SmartCreditViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: String,
    viewModel: SmartCreditViewModel
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val merchant by viewModel.activeMerchant.collectAsState()
    val transactions by viewModel.getCustomerTransactions(customerId).collectAsState(initial = emptyList())
    val instalmentPlans by viewModel.getCustomerInstalmentPlans(customerId).collectAsState(initial = emptyList())
    val instalmentItems by viewModel.getCustomerInstalmentItems(customerId).collectAsState(initial = emptyList())
    val allCustomers by viewModel.globalCustomers.collectAsState()
    val customer = allCustomers.find { it.id == customerId }

    var selectedTab by remember { mutableStateOf(0) }
    // 0: Overview, 1: Credit Ledger, 2: Instalment Schedule, 3: AutoPay, 4: Statement

    val shortDate = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val dayDate = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(customer?.fullName ?: "Customer Profile", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Customer ID: ${customer?.mobileNumber ?: ""}", fontSize = 11.sp, color = AccentTeal)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("customer_detail_back")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (customer != null && merchant != null) {
                        IconButton(
                            onClick = {
                                val pdfFile = PdfStatementGenerator.generateCustomerStatement(
                                    context = context,
                                    merchant = merchant!!,
                                    customer = customer,
                                    transactions = transactions
                                )
                                val shareIntent = PdfStatementGenerator.createShareIntent(
                                    context = context,
                                    pdfFile = pdfFile,
                                    customerPhone = customer.mobileNumber
                                )
                                context.startActivity(Intent.createChooser(shareIntent, "Share Statement PDF via WhatsApp"))
                            },
                            modifier = Modifier.testTag("share_pdf_statement_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share PDF", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        if (customer == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Tab Row: Overview, Credit Ledger, Instalments, AutoPay, Statement
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PrimaryTeal,
                edgePadding = 12.dp
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Overview") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Credit Ledger") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Instalments (${instalmentItems.size})") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("AutoPay") })
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("PDF Statement") })
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTab) {
                    0 -> CustomerOverviewSection(
                        customer = customer,
                        transactions = transactions,
                        plans = instalmentPlans,
                        onGiveCredit = { viewModel.navigateTo(Screen.GiveCredit(customer.id)) },
                        onAcceptRepayment = { viewModel.navigateTo(Screen.AcceptRepayment(customer.id)) },
                        onPlanInstalment = { viewModel.navigateTo(Screen.CreateInstalment(customer.id)) },
                        onSetupAutoPay = { viewModel.navigateTo(Screen.AutoPaySetup(customer.id)) }
                    )
                    1 -> CustomerLedgerSection(
                        customer = customer,
                        transactions = transactions,
                        shortDate = shortDate,
                        onGiveCredit = { viewModel.navigateTo(Screen.GiveCredit(customer.id)) },
                        onAcceptRepayment = { viewModel.navigateTo(Screen.AcceptRepayment(customer.id)) }
                    )
                    2 -> CustomerInstalmentScheduleSection(
                        customer = customer,
                        plans = instalmentPlans,
                        items = instalmentItems,
                        dayDate = dayDate,
                        onCreatePlan = { viewModel.navigateTo(Screen.CreateInstalment(customer.id)) }
                    )
                    3 -> CustomerAutoPaySection(
                        customer = customer,
                        onSetupAutoPay = { viewModel.navigateTo(Screen.AutoPaySetup(customer.id)) },
                        onUpdateMandate = { status -> viewModel.updateCustomerMandate(customer.id, status, null) }
                    )
                    4 -> CustomerPdfStatementSection(
                        customer = customer,
                        merchant = merchant,
                        transactions = transactions
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerOverviewSection(
    customer: Customer,
    transactions: List<LedgerTransaction>,
    plans: List<InstalmentPlan>,
    onGiveCredit: () -> Unit,
    onAcceptRepayment: () -> Unit,
    onPlanInstalment: () -> Unit,
    onSetupAutoPay: () -> Unit
) {
    val activePlan = plans.firstOrNull()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(customer.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                            Text("Customer ID: ${customer.mobileNumber}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryTeal)
                            Text("Address: ${customer.fullAddress}", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 2.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (customer.currentBalancePaise > 0) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = if (customer.currentBalancePaise > 0) "PAYMENT DUE" else "CLEAR",
                                color = if (customer.currentBalancePaise > 0) Color(0xFFDC2626) else Color(0xFF16A34A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Outstanding Balance", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("₹${customer.currentBalancePaise / 100.0}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626))
                        }
                        Column {
                            Text("Total Credit", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("₹${customer.totalCreditPaise / 100.0}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Total Repaid", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("₹${customer.totalRepaymentPaise / 100.0}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (activePlan != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF0FDFA),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF99F6E4)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Active Instalment Plan", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryTeal)
                                    Text("₹${activePlan.monthlyInstalmentPaise / 100.0}/mo (${activePlan.totalInstalments} months @ ${activePlan.interestRatePercent}%)", fontSize = 11.sp, color = Color(0xFF0F172A))
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFCCFBF1)) {
                                    Text("EMI ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryTeal, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onGiveCredit,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("Give Credit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onAcceptRepayment,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("Repayment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onPlanInstalment,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text("Plan EMI", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onSetupAutoPay,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text("Set Up AutoPay", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Automated WhatsApp Overdue Reminder & Quick Notice
                    if (customer.currentBalancePaise > 0) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Button(
                            onClick = {
                                val text = com.example.service.WhatsAppMessagingService.buildOverduePaymentReminderText(
                                    businessName = "Smart Credit Merchant",
                                    customer = customer,
                                    upiId = customer.mobileNumber + "@upi"
                                )
                                com.example.service.WhatsAppMessagingService.sendWhatsAppNotification(
                                    context = context,
                                    phone = customer.mobileNumber,
                                    message = text
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send WhatsApp Overdue Reminder", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerLedgerSection(
    customer: Customer,
    transactions: List<LedgerTransaction>,
    shortDate: SimpleDateFormat,
    onGiveCredit: () -> Unit,
    onAcceptRepayment: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Complete Ledger History", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
            Text("${transactions.size} entries", fontSize = 12.sp, color = Color(0xFF64748B))
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No ledger transactions recorded yet.", color = Color(0xFF64748B))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(transactions) { txn ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(shortDate.format(Date(txn.timestamp)), fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(
                                    text = (if (txn.transactionType == TransactionType.CREDIT_SALE) "+ " else "- ") + "₹${txn.amountPaise / 100.0}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (txn.transactionType == TransactionType.CREDIT_SALE) Color(0xFFDC2626) else Color(0xFF16A34A)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(txn.description, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Source: ${txn.paymentSource.name}", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text("Running Balance: ₹${txn.balanceAfterPaise / 100.0}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerInstalmentScheduleSection(
    customer: Customer,
    plans: List<InstalmentPlan>,
    items: List<InstalmentItem>,
    dayDate: SimpleDateFormat,
    onCreatePlan: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Instalment Schedule", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                Text("Repayment timeline & monthly instalments", fontSize = 12.sp, color = Color(0xFF64748B))
            }

            Button(
                onClick = onCreatePlan,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Create Plan", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (items.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No Instalment Plan Found", fontWeight = FontWeight.SemiBold)
                    Text("Convert customer's outstanding credit into an easy EMI instalment plan.", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items) { inst ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Instalment #${inst.instalmentNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Due: ${dayDate.format(Date(inst.dueDate))}", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text("Principal: ₹${inst.principalComponentPaise / 100.0} | Int: ₹${inst.interestComponentPaise / 100.0}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${inst.amountDuePaise / 100.0}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryTeal)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (inst.status == InstalmentStatus.PAID) Color(0xFFDCFCE7) else Color(0xFFE0F2FE),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = inst.status.name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (inst.status == InstalmentStatus.PAID) Color(0xFF16A34A) else PrimaryTeal,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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
fun CustomerAutoPaySection(
    customer: Customer,
    onSetupAutoPay: () -> Unit,
    onUpdateMandate: (MandateStatus) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Customer UPI AutoPay Mandate", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "NPCI recurring e-mandates allow automated monthly repayment collection directly from customer's UPI app.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Mandate Status:", fontSize = 13.sp, color = Color(0xFF64748B))
                    Text(
                        text = customer.autoPayMandateStatus.name,
                        fontWeight = FontWeight.Bold,
                        color = when (customer.autoPayMandateStatus) {
                            MandateStatus.ACTIVE -> Color(0xFF16A34A)
                            MandateStatus.CANCELLED -> Color(0xFFDC2626)
                            else -> Color(0xFFF59E0B)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Mandate UMN / Ref:", fontSize = 13.sp, color = Color(0xFF64748B))
                    Text(customer.autoPayMandateId ?: "Not Configured", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (customer.autoPayMandateStatus != MandateStatus.ACTIVE) {
                    Button(
                        onClick = onSetupAutoPay,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Initiate Customer AutoPay Setup", fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onUpdateMandate(MandateStatus.CANCELLATION_REQUESTED) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Request Mandate Cancellation")
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerPdfStatementSection(
    customer: Customer,
    merchant: MerchantProfile?,
    transactions: List<LedgerTransaction>
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Formal A4 PDF Account Statement", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                Text("Generate printable and legally authenticated statement of account including all historical credit, repayments, and closing balances.", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

                Button(
                    onClick = {
                        if (merchant != null) {
                            val pdfFile = PdfStatementGenerator.generateCustomerStatement(
                                context = context,
                                merchant = merchant,
                                customer = customer,
                                transactions = transactions
                            )
                            val shareIntent = PdfStatementGenerator.createShareIntent(
                                context = context,
                                pdfFile = pdfFile,
                                customerPhone = customer.mobileNumber
                            )
                            context.startActivity(Intent.createChooser(shareIntent, "Share Statement PDF via WhatsApp"))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download & Share Statement (WhatsApp)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        val csvFile = com.example.service.CsvExportService.exportLedgerToCsv(
                            context = context,
                            businessName = merchant?.businessName ?: "Smart Credit Merchant",
                            customer = customer,
                            transactions = transactions
                        )
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            csvFile
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Export Ledger CSV"))
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp), tint = DarkNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Customer History as CSV", fontWeight = FontWeight.Bold, color = DarkNavy)
                }
            }
        }
    }
}
