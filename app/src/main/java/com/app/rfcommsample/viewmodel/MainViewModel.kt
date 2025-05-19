package com.app.rfcommsample.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.app.rfcommsample.data.BluetoothDeviceRepository
import com.app.rfcommsample.model.BluetoothDeviceItem

class MainViewModel : ViewModel() {

    private val deviceRepo = BluetoothDeviceRepository()

    val devices: LiveData<List<BluetoothDeviceItem>> = deviceRepo.devices

    fun addDevice(device: BluetoothDevice, isPaired: Boolean) {
        deviceRepo.addDevice(device, isPaired)
    }

    fun clearDevices() {
        deviceRepo.clear()
    }
}
