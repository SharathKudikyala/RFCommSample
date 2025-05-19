package com.app.rfcommsample.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.rfcommsample.databinding.ItemBluetoothDeviceBinding
import com.app.rfcommsample.model.BluetoothDeviceItem

class BluetoothDeviceAdapter(
    private var devices: List<BluetoothDeviceItem>,
    private val onClick: (BluetoothDeviceItem) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(private val binding: ItemBluetoothDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BluetoothDeviceItem) {
            binding.tvDeviceName.text = item.name
            binding.tvDeviceStatus.text = if (item.isPaired) "Paired" else "New"
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemBluetoothDeviceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount() = devices.size

    fun updateDevices(newDevices: List<BluetoothDeviceItem>) {
        devices = newDevices
        notifyDataSetChanged()
    }
}
