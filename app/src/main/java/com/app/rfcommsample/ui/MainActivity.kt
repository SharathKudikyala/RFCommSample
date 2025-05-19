package com.app.rfcommsample.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.rfcommsample.adapter.BluetoothDeviceAdapter
import com.app.rfcommsample.bluetooth.central.BluetoothCentralManager
import com.app.rfcommsample.bluetooth.common.BluetoothChatChannel
import com.app.rfcommsample.bluetooth.peripheral.BluetoothPeripheralManager
import com.app.rfcommsample.databinding.ActivityMainBinding
import com.app.rfcommsample.util.AppConstants
import com.app.rfcommsample.util.BluetoothPermissionManager
import com.app.rfcommsample.util.Logger
import com.app.rfcommsample.viewmodel.MainViewModel

class MainActivity : AppCompatActivity(), Logger.LogListener {
    enum class DeviceMode(val tag: String) {
        CENTRAL("Client"), PERIPHERAL("Server"), NONE("None")
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var centralManager: BluetoothCentralManager
    private lateinit var peripheralManager: BluetoothPeripheralManager
    private var deviceMode = DeviceMode.NONE
    private var chatChannel: BluetoothChatChannel? = null

    private val enableBluetoothResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Logger.log(TAG, "Bluetooth Enabled")
        }
    }

    private val bluetoothCentralListener = object : BluetoothCentralManager.Listener {
        override fun onPairedDeviceFound(device: BluetoothDevice) {
            viewModel.addDevice(device, true)
        }

        override fun onNewDeviceFound(device: BluetoothDevice) {
            viewModel.addDevice(device, false)
        }

        override fun onScanTimeout() {
            Logger.log(TAG, "Scan timed out", Logger.LogLevel.DEBUG)
        }

        override fun onDeviceConnected(device: BluetoothDevice) {
            Logger.log(TAG, "Connected to ${device.name}")
            centralManager.getSocket()?.let { setupChatChannel(it) }
        }

        override fun onConnectionFailed(device: BluetoothDevice, reason: String) {
            Logger.log(
                TAG,
                "Failed to connect to ${device.name}: $reason",
                Logger.LogLevel.ERROR
            )
            Log.d(TAG, "onConnectionFailed: $reason")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Logger.setLogListener(this)
        checkBluetoothSupport()

        centralManager = BluetoothCentralManager(this, bluetoothAdapter, bluetoothCentralListener)
        peripheralManager = BluetoothPeripheralManager(bluetoothAdapter) { socket ->
            runOnUiThread {
                Logger.log(TAG, "Client connected")
                setupChatChannel(socket)
            }
        }

        setupUI()
        observeViewModel()

        Logger.log(TAG, "App Version : ${AppConstants.VERSION}")
    }

    private fun checkBluetoothSupport() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter?.let {
            bluetoothAdapter = it
        } ?: run {
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupUI() {
        binding.btnCentralMode.setOnClickListener { switchMode(DeviceMode.CENTRAL) }
        binding.btnPeripheralMode.setOnClickListener { switchMode(DeviceMode.PERIPHERAL) }
        binding.rvBluetoothDevices.layoutManager = LinearLayoutManager(this)

        binding.ibSendMessage.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                chatChannel?.send(message)
                binding.etMessage.text.clear()
                logToUI("Me: $message")
            }
        }
    }

    private fun observeViewModel() {
        val adapter = BluetoothDeviceAdapter(emptyList()) { item ->
            if (deviceMode == DeviceMode.CENTRAL) centralManager.connectToDevice(item.device)
        }
        binding.rvBluetoothDevices.adapter = adapter

        viewModel.devices.observe(this) {
            adapter.updateDevices(it)
        }
    }

    private fun switchMode(newMode: DeviceMode) {
        if (!BluetoothPermissionManager.hasAllPermissions(this)) {
            BluetoothPermissionManager.requestAllPermissions(this, REQUEST_CODE_ALL_PERMISSIONS)
            return
        }
        if (!BluetoothPermissionManager.ensureBluetoothEnabled(bluetoothAdapter) {
                enableBluetoothResultLauncher.launch(it)
            }) return

        when (newMode) {
            DeviceMode.CENTRAL -> {
                peripheralManager.stop()
                viewModel.clearDevices()
                centralManager.startScan()
            }

            DeviceMode.PERIPHERAL -> {
                centralManager.clear()
                viewModel.clearDevices()
                peripheralManager.start()
            }

            DeviceMode.NONE -> Unit
        }
        deviceMode = newMode
        Logger.log(TAG, "Switched to ${newMode.tag}")
    }

    private fun setupChatChannel(socket: BluetoothSocket) {
        chatChannel?.close()
        chatChannel = BluetoothChatChannel(socket).also { channel ->
            channel.startReceiving { message ->
                logToUI("Remote: $message")
            }
        }
    }

    private fun logToUI(message: String) {
        runOnUiThread {
            binding.tvLog.append("\n$message")
            binding.scrollView.post {
                binding.scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    override fun onLog(message: String) {
        logToUI(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        centralManager.clear()
        peripheralManager.stop()
        chatChannel?.close()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_ALL_PERMISSIONS = 1001
    }
}
