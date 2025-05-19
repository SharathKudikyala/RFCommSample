// central/BluetoothCentralManager.kt
package com.app.rfcommsample.bluetooth.central

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.app.rfcommsample.util.BluetoothConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BluetoothCentralManager(
    context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val listener: Listener
) {

    interface Listener {
        fun onPairedDeviceFound(device: BluetoothDevice)
        fun onNewDeviceFound(device: BluetoothDevice)
        fun onScanTimeout()
        fun onDeviceConnected(device: BluetoothDevice)
        fun onConnectionFailed(device: BluetoothDevice, reason: String)
    }

    private val appContext = context.applicationContext
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var scanTimeoutJob: Job? = null
    private var isReceiverRegistered = false
    private var connectionThread: Thread? = null
    private var connectJob: Job? = null
    private var socket: BluetoothSocket? = null


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!bluetoothAdapter.bondedDevices.contains(it)) {
                        listener.onNewDeviceFound(it)
                    }
                }
            }
        }
    }

    fun startScan() {
        stopScan() // in case already scanning

        // Register receiver
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        appContext.registerReceiver(receiver, filter)
        isReceiverRegistered = true

        // Add paired devices first
        bluetoothAdapter.bondedDevices.forEach {
            listener.onPairedDeviceFound(it)
        }

        // Start discovery
        bluetoothAdapter.startDiscovery()

        // Timeout to stop scan after 15 sec
        scanTimeoutJob = coroutineScope.launch {
            delay(15000)
            stopScan()
            listener.onScanTimeout()
        }
    }

    fun stopScan() {
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        scanTimeoutJob?.cancel()

        if (isReceiverRegistered) {
            runCatching {
                appContext.unregisterReceiver(receiver)
            }
            isReceiverRegistered = false
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        stopScan()// ensure scanning is stopped before connecting

        connectJob?.cancel()
        connectJob = coroutineScope.launch(Dispatchers.IO) {
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(BluetoothConstants.APP_UUID)
                bluetoothAdapter.cancelDiscovery() // must cancel discovery before connecting
                socket?.connect()

                withContext(Dispatchers.Main) {
                    listener.onDeviceConnected(device)
                }

                // After connection, you can manage I/O on `socket?.inputStream` and `outputStream`
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    listener.onConnectionFailed(device, e.message ?: "Unknown error")
                }
            }
        }
    }

    fun getSocket(): BluetoothSocket? = socket

    fun closeConnection() {
        connectJob?.cancel()
        runCatching {
            socket?.close()
        }
        socket = null
    }

    fun clear() {
        stopScan()
        closeConnection()
        coroutineScope.cancel()
    }

    companion object {
        private const val TAG = "BluetoothCentralManager"
    }
}

