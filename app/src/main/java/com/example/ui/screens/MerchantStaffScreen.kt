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
import com.example.data.model.MerchantStaff
import com.example.viewmodel.Screen
import com.example.viewmodel.SmartCreditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantStaffScreen(viewModel: SmartCreditViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val merchant by viewModel.activeMerchant.collectAsState()
    val staffList by viewModel.getMerchantStaff(merchant?.id ?: "").collectAsState(initial = emptyList())

    var showAddStaffDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Store Staff & Permissions") },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("staff_screen_back")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddStaffDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Add Staff Member") },
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White,
                modifier = Modifier.testTag("add_staff_fab")
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Granular Access Delegation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Owner controls which staff can give credit, collect repayments, or manage customers.",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (staffList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No staff registered yet", fontWeight = FontWeight.SemiBold)
                        Text("Add staff members to let them record sales and accept repayments.", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(staffList) { staff ->
                        StaffCard(staff = staff)
                    }
                }
            }
        }
    }

    if (showAddStaffDialog) {
        AddStaffDialog(
            onDismiss = { showAddStaffDialog = false },
            onConfirm = { name, phone, role, canCredit, canRepay, canStaff ->
                viewModel.addStaffMember(name, phone, role, canCredit, canRepay, canStaff)
                showAddStaffDialog = false
            }
        )
    }
}

@Composable
fun StaffCard(staff: MerchantStaff) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth().testTag("staff_card_${staff.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(staff.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("+91 ${staff.mobileNumber} • Role: ${staff.roleName}", fontSize = 12.sp, color = Color(0xFF64748B))
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (staff.isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                ) {
                    Text(
                        text = if (staff.isActive) "ACTIVE" else "DISABLED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (staff.isActive) Color(0xFF16A34A) else Color(0xFFDC2626),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            Text("Configured Permissions:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (staff.canGiveCredit) {
                    Surface(color = Color(0xFFE0F2FE), shape = RoundedCornerShape(4.dp)) {
                        Text("Give Credit", fontSize = 10.sp, color = Color(0xFF0369A1), modifier = Modifier.padding(4.dp))
                    }
                }
                if (staff.canRecordCashRepayments) {
                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(4.dp)) {
                        Text("Cash Repayments", fontSize = 10.sp, color = Color(0xFF15803D), modifier = Modifier.padding(4.dp))
                    }
                }
                if (staff.canManageStaff) {
                    Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(4.dp)) {
                        Text("Manager", fontSize = 10.sp, color = Color(0xFFB45309), modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddStaffDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, role: String, canCredit: Boolean, canRepay: Boolean, canStaff: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Counter Cashier") }
    var canCredit by remember { mutableStateOf(true) }
    var canRepay by remember { mutableStateOf(true) }
    var canManageStaff by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Store Staff Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Staff Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_staff_name")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10) phone = it },
                    label = { Text("10-Digit Mobile *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_staff_phone")
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role (e.g. Cashier, Manager) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = canCredit, onCheckedChange = { canCredit = it })
                    Text("Can record credit (Udhaar)", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = canRepay, onCheckedChange = { canRepay = it })
                    Text("Can record cash repayments", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = canManageStaff, onCheckedChange = { canManageStaff = it })
                    Text("Can manage other staff", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.length == 10) {
                        onConfirm(name, phone, role, canCredit, canRepay, canManageStaff)
                    }
                },
                modifier = Modifier.testTag("add_staff_confirm")
            ) {
                Text("Save Staff")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
