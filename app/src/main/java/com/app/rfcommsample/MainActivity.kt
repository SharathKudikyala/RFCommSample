package com.app.rfcommsample

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.app.rfcommsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), Logger.LogListener {
    enum class DeviceMode(val tag: String) {
        CENTRAL("Client"), PERIPHERAL("Server"), NONE("None")
    }

    private val requiredPermissions = arrayOf(
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val enableBluetoothResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Logger.log(TAG, "Bluetooth Enabled")
        }

    }

    private lateinit var binding: ActivityMainBinding
    private var deviceMode = DeviceMode.NONE
    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkBluetoothSupport()
        setListeners()

        Logger.log(TAG, "App Version : $APP_VERSION")
    }

    private fun checkBluetoothSupport() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter?.let {
            bluetoothAdapter = it
        } ?: run {
            Toast.makeText(
                this@MainActivity,
                "Device does not support Bluetooth",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }
    }

    private fun setListeners() {
        Logger.setLogListener(this)

        binding.btnCentralMode.setOnClickListener {
            switchMode(DeviceMode.CENTRAL)
        }

        binding.btnPeripheralMode.setOnClickListener {
            switchMode(DeviceMode.PERIPHERAL)
        }

    }

    private fun ensurePermissionsAndBluetoothOn(): Boolean {
        if (!hasPermissions()) {
            requestAllPermissions()
            return false
        }

        if (!bluetoothAdapter.isEnabled) {
            promptToEnableBluetooth()
            return false
        }
        return true

    }

    private fun hasPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PermissionChecker.PERMISSION_GRANTED
        }
    }

    private fun promptToEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            enableBluetoothResultLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private fun requestAllPermissions() {
        ActivityCompat.requestPermissions(
            this,
            requiredPermissions,
            REQUEST_CODE_ALL_PERMISSIONS
        )
    }

    private fun switchMode(newMode: DeviceMode) {
        if (!ensurePermissionsAndBluetoothOn()) return

        when (newMode) {
            DeviceMode.CENTRAL -> {
                stopPeripheral()
                startCentral()
            }

            DeviceMode.PERIPHERAL -> {
                stopCentral()
                startPeripheral()
            }

            DeviceMode.NONE -> Unit
        }

        deviceMode = newMode
        Logger.log(TAG, "Switched to ${newMode.tag}")
    }

    private fun startCentral() {

    }

    private fun stopCentral() {

    }

    private fun startPeripheral() {

    }

    private fun stopPeripheral() {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_ALL_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Logger.log(TAG, "All permissions granted")
                promptToEnableBluetooth()
            } else {
                Logger.log(TAG, "permission(s) denied")
            }
        }
    }

    override fun onLog(message: String) {
        runOnUiThread {
            binding.tvLog.append("\n$message")
            binding.scrollView.post {
                binding.scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_ALL_PERMISSIONS = 1001
    }
}