package org.samo_lego.canta.ui

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.samo_lego.canta.R
import org.samo_lego.canta.ui.component.AppList
import org.samo_lego.canta.ui.component.CantaTopBar
import org.samo_lego.canta.ui.dialog.ExplainBadgesDialog
import org.samo_lego.canta.ui.dialog.UninstallAppsDialog
import org.samo_lego.canta.ui.navigation.Screen
import org.samo_lego.canta.ui.screen.LogsPage
import org.samo_lego.canta.ui.viewmodel.AppListViewModel
import org.samo_lego.canta.util.Filter
import org.samo_lego.canta.util.ShizukuData
import org.samo_lego.canta.util.ShizukuInfo

@Composable
fun CantaApp(
        launchShizuku: () -> Unit,
        uninstallApp: (String) -> Boolean,
        reinstallApp: (String) -> Boolean,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainContent(
                    launchShizuku = launchShizuku,
                    uninstallApp = uninstallApp,
                    reinstallApp = reinstallApp,
                    navigateToLogs = { navController.navigate(Screen.Logs.route) }
            )
        }
        composable(route = Screen.Logs.route) {
            LogsPage(onNavigateBack = { navController.navigateUp() })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainContent(
        launchShizuku: () -> Unit,
        uninstallApp: (String) -> Boolean,
        reinstallApp: (String) -> Boolean,
        navigateToLogs: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Selected tab
    var selectedAppsType by remember { mutableStateOf(AppsType.INSTALLED) }

    val appListViewModel = viewModel<AppListViewModel>()

    var showBadgeInfoDialog by remember { mutableStateOf(false) }
    var showUninstallConfirmDialog by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { AppsType.entries.size })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            selectedAppsType = AppsType.entries[page]
            appListViewModel.selectedFilter = Filter.any
        }
    }

    Scaffold(
            topBar = {
                CantaTopBar(
                        openBadgesInfoDialog = { showBadgeInfoDialog = true },
                        openLogsScreen = navigateToLogs
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                        // Make the FAB hidden if no apps are selected
                        visible = appListViewModel.selectedApps.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                ) {
                    FloatingActionButton(
                            containerColor =
                                    when (selectedAppsType) {
                                        AppsType.INSTALLED ->
                                                MaterialTheme.colorScheme.errorContainer
                                        AppsType.UNINSTALLED ->
                                                MaterialTheme.colorScheme.tertiaryContainer
                                    },
                            shape = RoundedCornerShape(32.dp),
                            modifier = Modifier.padding(16.dp).navigationBarsPadding(),
                            onClick = {
                                if (selectedAppsType == AppsType.INSTALLED) {
                                    showUninstallConfirmDialog =
                                            appListViewModel.selectedApps.isNotEmpty()
                                    return@FloatingActionButton
                                }
                                uninstallOrReinstall(
                                        context = context,
                                        coroutineScope = coroutineScope,
                                        launchShizuku = launchShizuku,
                                        uninstallApp = uninstallApp,
                                        reinstallApp = reinstallApp,
                                        selectedAppsType = selectedAppsType,
                                        appListViewModel = appListViewModel,
                                )
                            },
                    ) {
                        when (selectedAppsType) {
                            AppsType.INSTALLED ->
                                    Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.uninstall)
                                    )
                            AppsType.UNINSTALLED ->
                                    Icon(
                                            Icons.Default.InstallMobile,
                                            contentDescription = stringResource(R.string.reinstall)
                                    )
                        }
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.SpaceBetween
        ) {
            TabRow(
                    selectedTabIndex = selectedAppsType.ordinal,
                    contentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                AppsType.entries.forEach { currentTab ->
                    Tab(
                            selected = selectedAppsType == currentTab,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(currentTab.ordinal)
                                    appListViewModel.selectedFilter = Filter.any
                                }
                                selectedAppsType = currentTab
                            },
                            icon = {
                                Icon(currentTab.icon, contentDescription = currentTab.toString())
                            },
                    )
                }
            }
            HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
            ) { page ->
                if (showBadgeInfoDialog) {
                    ExplainBadgesDialog(onDismissRequest = { showBadgeInfoDialog = false })
                } else if (showUninstallConfirmDialog) {
                    UninstallAppsDialog(
                            appCount = appListViewModel.selectedApps.size,
                            onDismiss = { showUninstallConfirmDialog = false },
                            onAgree = {
                                showUninstallConfirmDialog = false

                                // Trigger uninstall
                                uninstallOrReinstall(
                                        context = context,
                                        coroutineScope = coroutineScope,
                                        launchShizuku = launchShizuku,
                                        uninstallApp = uninstallApp,
                                        reinstallApp = reinstallApp,
                                        selectedAppsType = selectedAppsType,
                                        appListViewModel = appListViewModel,
                                )
                            }
                    )
                }
                AppList(appType = AppsType.entries[page])
            }
        }
    }
}

fun uninstallOrReinstall(
        context: Context,
        coroutineScope: CoroutineScope,
        launchShizuku: () -> Unit,
        uninstallApp: (String) -> Boolean,
        reinstallApp: (String) -> Boolean,
        selectedAppsType: AppsType,
        appListViewModel: AppListViewModel,
) {
    coroutineScope.launch {
        when (ShizukuData.checkShizukuActive(context.packageManager)) {
            ShizukuInfo.NOT_INSTALLED -> {
                Toast.makeText(
                                context,
                                context.getString(
                                        R.string.please_install_shizuku_and_authorise_canta
                                ),
                                Toast.LENGTH_SHORT
                        )
                        .show()
                return@launch
            }
            ShizukuInfo.NOT_ACTIVE -> {
                Toast.makeText(
                                context,
                                context.getString(R.string.please_start_shizuku),
                                Toast.LENGTH_SHORT
                        )
                        .show()
                launchShizuku()
                return@launch
            }
            ShizukuInfo.ACTIVE -> {
                // Check shizuku permission
                ShizukuData.checkShizukuPermission { permResult ->
                    val permission = permResult == PackageManager.PERMISSION_GRANTED

                    if (!permission) {
                        Toast.makeText(
                                        context,
                                        context.getString(
                                                R.string.please_allow_shizuku_access_for_canta
                                        ),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                    } else {
                        // Proceed with the action
                        when (selectedAppsType) {
                            AppsType.INSTALLED -> {
                                appListViewModel.selectedApps.forEach {
                                    val uninstalled = uninstallApp(it.key)
                                    if (uninstalled) {
                                        appListViewModel.changeAppStatus(it.key)
                                        appListViewModel.selectedApps.remove(it.key)
                                    }
                                }
                            }
                            AppsType.UNINSTALLED -> {
                                appListViewModel.selectedApps.forEach {
                                    val installed = reinstallApp(it.key)
                                    if (installed) {
                                        appListViewModel.changeAppStatus(it.key)
                                        appListViewModel.selectedApps.remove(it.key)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class AppsType(val icon: ImageVector) {
    INSTALLED(Icons.Default.AutoDelete),
    UNINSTALLED(Icons.Default.DeleteForever),
}
