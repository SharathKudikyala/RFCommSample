package com.app.rfcommsample.data

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.rfcommsample.model.BluetoothDeviceItem

class BluetoothDeviceRepository {
    private val _devices = MutableLiveData<List<BluetoothDeviceItem>>(emptyList())
    val devices: LiveData<List<BluetoothDeviceItem>> = _devices

    private val internalList = mutableListOf<BluetoothDeviceItem>()

    fun addDevice(device: BluetoothDevice, isPaired: Boolean) {
        if (internalList.none { it.address == device.address }) {
            internalList.add(
                BluetoothDeviceItem(device.name ?: "Unknown", device.address, isPaired, device)
            )
            _devices.postValue(internalList.toList())
        }
    }

    fun clear() {
        internalList.clear()
        _devices.postValue(emptyList())
    }
}
