package com.app.rfcommsample.bluetooth.common

import android.bluetooth.BluetoothSocket
import com.app.rfcommsample.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BluetoothChatChannel(private val socket: BluetoothSocket) {

    private val input = BufferedReader(InputStreamReader(socket.inputStream))
    private val output = OutputStreamWriter(socket.outputStream)
    private var receiveJob: Job? = null

    fun startReceiving(onMessageReceived: (String) -> Unit) {
        receiveJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                while (isActive) {
                    val line = input.readLine()
                    if (line != null) {
                        withContext(Dispatchers.Main) {
                            onMessageReceived(line)
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.log("BluetoothChat", "Receive error: ${e.message}")
            }
        }
    }

    fun send(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                output.write("$message\n")
                output.flush()
            } catch (e: Exception) {
                Logger.log("BluetoothChat", "Send error: ${e.message}")
            }
        }
    }

    fun close() {
        receiveJob?.cancel()
        runCatching { input.close() }
        runCatching { output.close() }
        runCatching { socket.close() }
    }
}