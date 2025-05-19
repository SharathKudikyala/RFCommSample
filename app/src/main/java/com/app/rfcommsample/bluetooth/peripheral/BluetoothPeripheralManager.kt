package com.app.rfcommsample.bluetooth.peripheral

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import com.app.rfcommsample.util.BluetoothConstants
import com.app.rfcommsample.util.Logger
import java.io.IOException

class BluetoothPeripheralManager(
    private val adapter: BluetoothAdapter,
    private val onClientConnected: (BluetoothSocket) -> Unit
) {

    private var serverThread: Thread? = null
    private var serverSocket: BluetoothServerSocket? = null

    fun start() {
        serverThread = Thread {
            try {
                Logger.log(TAG, "Server waiting for connection")
                serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(
                    "BTServer",
                    BluetoothConstants.APP_UUID
                )

                val socket: BluetoothSocket? = serverSocket?.accept()
                Logger.log(TAG, "Client connected: ${socket?.remoteDevice?.name}")

                socket?.let { onClientConnected(it) }

            } catch (e: IOException) {
                Logger.log(TAG, "Server error: ${e.message}")
            } finally {
                runCatching { serverSocket?.close() }
            }
        }
        serverThread?.start()
    }

    fun stop() {
        serverThread?.interrupt()
        runCatching { serverSocket?.close() }
        serverThread = null
        Logger.log(TAG, "Peripheral server stopped")
    }

    companion object {
        private const val TAG = "PeripheralManager"
    }
}
