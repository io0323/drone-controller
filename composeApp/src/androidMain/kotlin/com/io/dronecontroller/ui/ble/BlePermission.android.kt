package com.io.dronecontroller.ui.ble

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberBlePermissionLauncher(onGranted: () -> Unit): () -> Unit {
    val context = LocalContext.current

    val permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            if (results.values.all { it }) onGranted()
        }

    return remember(context) {
        {
            val allGranted =
                permissions.all {
                    context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
                }
            if (allGranted) {
                onGranted()
            } else {
                launcher.launch(permissions)
            }
        }
    }
}
