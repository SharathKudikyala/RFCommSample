package com.app.rfcommsample.util

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

object BluetoothPermissionManager {

    val requiredPermissions = arrayOf(
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    fun hasAllPermissions(context: Context): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PermissionChecker.PERMISSION_GRANTED
        }
    }

    fun requestAllPermissions(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, requiredPermissions, requestCode)
    }

    fun ensureBluetoothEnabled(
        adapter: BluetoothAdapter,
        launcher: (Intent) -> Unit
    ): Boolean {
        return if (!adapter.isEnabled) {
            launcher(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            false
        } else true
    }
}
