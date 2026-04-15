package com.io.dronecontroller.ui.ble

import androidx.compose.runtime.Composable

@Composable
actual fun rememberBlePermissionLauncher(onGranted: () -> Unit): () -> Unit = { onGranted() }
