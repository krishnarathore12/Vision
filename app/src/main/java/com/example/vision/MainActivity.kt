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
import android.os.Handler
import kotlin.math.pow
import kotlin.math.sqrt
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
    private var lastDevice = ""

    private val deviceList: MutableList<Pair<String?, Int>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        requestPermissions()

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions()
                return
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        if (bluetoothAdapter == null) {
            // Bluetooth is not supported
            return
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
        var rssi1 : Double = 0.0;
        var rssi2 : Double = 0.0;
        var rssi3 : Double = 0.0;
        var x1 : Double = 0.0;
        var y1 : Double = 0.0;
        var x2 : Double = 0.0;
        var y2 : Double = 0.0;
        var x3 : Double = 0.0;
        var y3 : Double = 0.0;
        b.button.setOnClickListener {
            val currentLocation = getCurrentLocation()

            if (currentLocation != null) {
                val latitude = currentLocation.latitude.toDouble()
                val longitude = currentLocation.longitude.toDouble()
                b.textView.text = "$latitude , $longitude"
                x1 = latitude
                y1 = longitude
                // Do something with latitude and longitude
            }

            startBluetoothDiscovery{
                rssi1 = deviceList.first().second.toDouble()
                b.textView5.text = "$rssi1"
            }




            // Use the currentLocation as needed

        }

        b.button2.setOnClickListener {
            val currentLocation2 = getCurrentLocation()

            if (currentLocation2 != null) {
                val latitude2 = currentLocation2.latitude.toDouble()
                val longitude2 = currentLocation2.longitude.toDouble()
                b.textView2.text = "$latitude2 , $longitude2"
                x2 = latitude2
                y2 = longitude2
                // Do something with latitude and longitude
            }

            startBluetoothDiscovery{
                rssi2 = deviceList.first().second.toDouble()
                if(deviceList.size == null){
                    b.textView6.text = "null"
                }
                b.textView6.text = "$rssi2"
            }

            // Use the currentLocation as needed

        }

        b.button3.setOnClickListener {
            val currentLocation3 = getCurrentLocation()

            if (currentLocation3 != null) {
                val latitude3 = currentLocation3.latitude.toDouble()
                val longitude3 = currentLocation3.longitude.toDouble()
                b.textView3.text = "$latitude3 , $longitude3"
                x3 = latitude3
                y3 = longitude3
                // Do something with latitude and longitude
            }

            startBluetoothDiscovery{
                rssi3 = deviceList.first().second.toDouble()
                if(deviceList.size == null){
                    b.textView7.text = "null"
                }
                b.textView7.text = "$rssi3"
            }

            // Use the currentLocation as needed

        }

        var dist1 = calculateDistance(rssi1)
        var dist2 = calculateDistance(rssi2)
        var dist3 = calculateDistance(rssi3)
        var p : Pair<Double, Double> = findCoordinates(x1,y1,x2,y2,x3,y3,dist1,dist2,dist3)
        var p1 = p.first
        var p2 = p.second

        b.textView4.text = "$p1, $p2"
    }

    private fun requestPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.INTERNET
            )
            .withListener(CompositeMultiplePermissionsListener(
                DialogOnAnyDeniedMultiplePermissionsListener.Builder
                    .withContext(this)
                    .withTitle("Permissions Needed")
                    .withMessage("Please grant the required permissions to use Bluetooth and location services.")
                    .build()
            ))
            .check()
    }


    private fun stopBluetoothDiscovery() {
        // Stop Bluetooth discovery
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
            return
        }
        if (bluetoothAdapter != null && bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.cancelDiscovery()
        }
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceHardwareAddress = device?.address // MAC address
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, 0).toInt()

                    // Append device address and RSSI value to the list
                    addDeviceToList(deviceHardwareAddress, rssi)
                }
            }
        }
    }

    private fun getCurrentLocation(): Location? {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            try {
                // Get the last known location from the GPS provided
                return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            } catch (e: SecurityException) {
                // Handle the exception
                e.printStackTrace()
            }
        }
        return null
    }

    private fun startBluetoothDiscovery(callback: () -> Unit) {
        // Check if discovery is already in progress
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
            return
        }
        if (bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.cancelDiscovery()
        }

        // Clear the device list before starting a new discovery
        deviceList.clear()

        // Start discovery
        bluetoothAdapter!!.startDiscovery()

        // Stop discovery after 5 seconds
        Handler().postDelayed({
            stopBluetoothDiscovery()
            callback.invoke()
        }, DISCOVERY_DURATION_MILLIS)
    }



    private fun findCoordinates(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double, d1: Double, d2: Double, d3: Double) : Pair<Double ,Double >{
        val p: Double
        val q: Double
        val k = d2.pow(2) + x3.pow(2) + y3.pow(2) - d3.pow(2) - x2.pow(2) - y2.pow(2)

        if ((y3 - y2) == 0.0) {
            val p1 = (d2.pow(2) - d3.pow(2) - x2.pow(2) + x3.pow(2)) / (2 * (x3 - x2))
            val q1 = y2 + sqrt(d2.pow(2) - (p1 - x2).pow(2))
            val q2 = y2 - sqrt(d2.pow(2) - (p1 - x2).pow(2))

            if (kotlin.math.abs((p1 - x1).pow(2) + (q2 - y1).pow(2) - d1.pow(2)) <= 0.001) {
                return Pair(p1,q2);
                println("%.6f %.6f".format(p1, q2))
            } else {
                return Pair(p1,q1);
                println("%.6f %.6f".format(p1, q1))
            }
        } else {
            val b1 = 2 * y2 * (x3 - x2) / (y3 - y2) - 2 * x2 - k * (x3 - x2) / (y3 - y2).pow(2)
            val a1 = 1.0 + (x3 - x2).pow(2) / (y3 - y2).pow(2)
            val c1 = k.pow(2) / (4 * (y3 - y2).pow(2)) - k * y2 / (y3 - y2) + x2.pow(2) + y2.pow(2) - d2.pow(2)

            val p1 = (-b1 + sqrt(b1.pow(2) - 4 * a1 * c1)) / (2 * a1)
            val p2 = (-b1 - sqrt(b1.pow(2) - 4 * a1 * c1)) / (2 * a1)

            val q11 = y2 + sqrt(d2.pow(2) - (p1 - x2).pow(2))
            val q12 = y2 - sqrt(d2.pow(2) - (p1 - x2).pow(2))
            val q21 = y2 + sqrt(d2.pow(2) - (p2 - x2).pow(2))
            val q22 = y2 - sqrt(d2.pow(2) - (p2 - x2).pow(2))

            when {
                kotlin.math.abs((p1 - x1).pow(2) + (q11 - y1).pow(2) - d1.pow(2)) <= 0.001 -> {
                    return Pair(p1,q11);
                    println("%.6f %.6f".format(p1, q11))
                }
                kotlin.math.abs((p1 - x1).pow(2) + (q12 - y1).pow(2) - d1.pow(2)) <= 0.001 -> {
                    return Pair(p1,q12);
                    println("%.6f %.6f".format(p1, q12))
                }
                kotlin.math.abs((p2 - x1).pow(2) + (q21 - y1).pow(2) - d1.pow(2)) <= 0.001 -> {
                    return Pair(p2,q21);
                    println("%.6f %.6f".format(p2, q21))
                }
                else -> {
                    return Pair(p2,q22);
                    println("%.6f %.6f".format(p2, q22))
                }
            }
        }
    }



    private fun addDeviceToList(deviceAddress: String?, rssi: Int) {
        // Append device address and RSSI value to the list
        deviceList.add(Pair(deviceAddress, rssi))
    }

    private fun calculateDistance(rssi: Double): Double {
        val txPower = 4.0
        val ratio = rssi / txPower
        if (rssi == 0.0) { // Cannot determine accuracy, return -1.
            return -1.0
        } else if (ratio < 1.0) { //default ratio
            return Math.pow(ratio, 10.0)
        }//rssi is greater than transmission strength
        return (0.89976) * Math.pow(ratio, 7.7095)+0.111
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }



    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val DISCOVERY_DURATION_MILLIS = 5000L
    }
}