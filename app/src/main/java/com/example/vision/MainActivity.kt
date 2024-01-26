package com.example.vision

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.vision.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener

class MainActivity : AppCompatActivity() {
    private lateinit var b : ActivityMainBinding
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var targetBluetoothAddress = "" // Replace with your target Bluetooth address
    private val discoveredDevices = mutableListOf<Pair<String, Int>>()

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        // Check if the discovered device matches the target address
                        discoveredDevices.add(Pair(device.address, intent.getIntExtra(BluetoothDevice.EXTRA_RSSI, 0)))
                    }
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        requestPermissions()

        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            // Handle accordingly
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            // Bluetooth is not enabled, you may want to request the user to enable it
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        // Register for Bluetooth discovery events
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothReceiver, filter)




        b.button.setOnClickListener {
            val currentLocation = getCurrentLocation()
            startBluetoothDiscovery()

            if (discoveredDevices.size == 0){
                Toast.makeText(this,"No RSSI Found",Toast.LENGTH_SHORT).show()
            }else {
                val currentRssi = discoveredDevices[0].second
                targetBluetoothAddress = discoveredDevices[0].first
                b.textView5.text = "$currentRssi"
            }
            // Use the currentLocation as needed
            if (currentLocation != null) {
                val latitude = currentLocation.latitude
                val longitude = currentLocation.longitude
                b.textView.text = "$latitude , $longitude"
                // Do something with latitude and longitude
            }
        }

        b.button2.setOnClickListener {
            val currentLocation2 = getCurrentLocation()
            startBluetoothDiscovery()

            val currentRssi2 = getRssiForDevice(targetBluetoothAddress)
            b.textView5.text = "$currentRssi2"

            // Use the currentLocation as needed
            if (currentLocation2 != null) {
                val latitude2 = currentLocation2.latitude
                val longitude2 = currentLocation2.longitude
                b.textView2.text = "$latitude2 , $longitude2"
                // Do something with latitude and longitude
            }
        }

        b.button3.setOnClickListener {
            val currentLocation3 = getCurrentLocation()
            startBluetoothDiscovery()

            val currentRssi3 = getRssiForDevice(targetBluetoothAddress)
            b.textView5.text = "$currentRssi3"

            // Use the currentLocation as needed
            if (currentLocation3 != null) {
                val latitude3 = currentLocation3.latitude
                val longitude3 = currentLocation3.longitude
                b.textView3.text = "$latitude3 , $longitude3"
                // Do something with latitude and longitude
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermissions() {

        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.INTERNET
            )

    }
    private fun getCurrentLocation(): Location? {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                // Get the last known location from the GPS provider
                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (e: SecurityException) {
                // Handle the exception
                e.printStackTrace()
            }
        }

        // You may want to handle cases where GPS is not enabled or location is not available
        return null
    }

    private fun startBluetoothDiscovery() {
        if (bluetoothAdapter?.isDiscovering == true) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            bluetoothAdapter.cancelDiscovery()
        }

        // Start discovery
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothAdapter?.startDiscovery()
    }

    private fun getRssiForDevice(address: String): Int {
        // Find the device in the list and return its RSSI value
        val foundDevice = discoveredDevices.find { it.first == address }
        return foundDevice?.second ?: 0
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the broadcast receiver when the activity is destroyed
        unregisterReceiver(bluetoothReceiver)
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }
}