package com.io.dronecontroller.ui.ble

import androidx.compose.runtime.Composable

@Composable
expect fun rememberBlePermissionLauncher(onGranted: () -> Unit): () -> Unit
