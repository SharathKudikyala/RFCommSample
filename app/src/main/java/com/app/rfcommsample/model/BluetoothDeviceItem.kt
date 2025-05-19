package com.app.rfcommsample.model

import android.bluetooth.BluetoothDevice

data class BluetoothDeviceItem(
    val name: String,
    val address: String,
    val isPaired: Boolean,
    val device: BluetoothDevice
)
