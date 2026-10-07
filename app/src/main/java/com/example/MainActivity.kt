package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.Screen
import com.example.viewmodel.SmartCreditViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SmartCreditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appConfig by viewModel.appConfig.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val userMessage by viewModel.userMessage.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(userMessage) {
                userMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearUserMessage()
                }
            }

            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (val scr = currentScreen) {
                            is Screen.Startup -> StartupScreen(viewModel = viewModel, config = appConfig)
                            is Screen.AdminLogin -> AdminLoginScreen(viewModel = viewModel)
                            is Screen.MerchantLogin -> MerchantLoginScreen(viewModel = viewModel)
                            is Screen.MerchantRegistration -> MerchantRegistrationScreen(viewModel = viewModel)
                            is Screen.SuperAdminDashboard -> SuperAdminDashboardScreen(viewModel = viewModel)
                            is Screen.MerchantDashboard -> MerchantDashboardScreen(viewModel = viewModel)
                            is Screen.CustomerDetail -> CustomerDetailScreen(customerId = scr.customerId, viewModel = viewModel)
                            is Screen.MerchantStaffScreen -> MerchantStaffScreen(viewModel = viewModel)
                            is Screen.GiveCredit -> GiveCreditScreen(preselectedCustomerId = scr.preselectedCustomerId, viewModel = viewModel)
                            is Screen.CreateInstalment -> CreateInstalmentScreen(customerId = scr.customerId, principalRupees = scr.principalRupees, viewModel = viewModel)
                            is Screen.AutoPaySetup -> AutoPaySetupScreen(customerId = scr.customerId, viewModel = viewModel)
                            is Screen.AutoPayDashboard -> AutoPayManagementTab(customers = viewModel.globalCustomers.collectAsState().value, viewModel = viewModel)
                            is Screen.InstalmentsScheduleScreen -> InstalmentsScheduleTab(
                                instalmentItems = viewModel.getMerchantInstalmentItems(viewModel.activeMerchant.collectAsState().value?.id ?: "").collectAsState(initial = emptyList()).value,
                                customers = viewModel.globalCustomers.collectAsState().value,
                                onPlanNew = {
                                    val c = viewModel.globalCustomers.value.firstOrNull()
                                    if (c != null) viewModel.navigateTo(Screen.CreateInstalment(c.id))
                                }
                            )
                            is Screen.AcceptRepayment -> AcceptRepaymentScreen(preselectedCustomerId = scr.preselectedCustomerId, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
