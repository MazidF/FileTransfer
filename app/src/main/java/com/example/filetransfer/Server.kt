package com.example.filetransfer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.LocalOnlyHotspotCallback
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.net.InetSocketAddress
import java.net.ServerSocket
import android.net.ConnectivityManager
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import android.content.pm.ActivityInfo

import android.content.pm.PackageInfo





class Server : AppCompatActivity() {
    lateinit var server: ServerSocket
    private var mReservation: LocalOnlyHotspotReservation? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)
        val wifiAPController = WifiAPController()
        wifiAPController.wifiToggle("mHotspot", "12345678", applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager, this)
//        Log.e("serverId", "${this.canToggleGPS()}")
//        turnOnHotspot()
//        serverIp()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
    }

    private fun init() {
        server = ServerSocket()
        server.bind(InetSocketAddress(9999))
        Toast.makeText(this, "server started", Toast.LENGTH_SHORT).show()
        doTask {
            val client = server.accept()
            val intent = Intent(this, MainApp::class.java)
            intent.putExtra("socketIp", MySocket(client))
            startActivity(intent)
        }
    }

    private fun serverIp(): String {
        val context = this.applicationContext
        val wm = context.getSystemService(WIFI_SERVICE) as WifiManager
        Log.e("severIp", wm.dhcpInfo.serverAddress.toString())
        Log.e("severIp", wm.dhcpInfo.gateway.toString())
        return Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 ) {
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]
                if (permission == Manifest.permission.ACCESS_FINE_LOCATION) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        init()
                    } else {
                        turnOnHotspot()
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun turnOnHotspot() {
        val manager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                1
            )
            return
        }
        turnGPSOn()
        manager.startLocalOnlyHotspot(object : LocalOnlyHotspotCallback() {
            override fun onStarted(reservation: LocalOnlyHotspotReservation) {
                super.onStarted(reservation)
                Log.d("serverIp", "onStarted: ")
                mReservation = reservation
            }

            override fun onStopped() {
                super.onStopped()
                Log.d("serverIp", "onStopped: ")
            }

            override fun onFailed(reason: Int) {
                super.onFailed(reason)
                Log.d("serverIp", "onFailed: ")
            }
        }, Handler())
    }

    private fun turnOffHotspot() {
        if (mReservation != null) {
            mReservation!!.close()
        }
    }

    private fun turnGPSOn() {
        val provider: String =
            Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
        if (!provider.contains("gps")) { //if gps is disabled
            val poke = Intent()
            poke.setClassName(
                "com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider"
            )
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
            poke.data = Uri.parse("3")
            sendBroadcast(poke)
        }
    }

    private fun turnGPSOff() {
        val provider: String =
            Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
        if (provider.contains("gps")) { //if gps is enabled
            val poke = Intent()
            poke.setClassName(
                "com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider"
            )
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
            poke.data = Uri.parse("3")
            sendBroadcast(poke)
        }
    }

    private fun canToggleGPS(): Boolean {
        val pacman = packageManager
        var pacInfo: PackageInfo? = null
        pacInfo = try {
            pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS)
        } catch (e: PackageManager.NameNotFoundException) {
            return false //package not found
        }
        if (pacInfo != null) {
            for (actInfo in pacInfo.receivers) {
                //test if receiver is exported. if so, we can toggle GPS.
                if (actInfo.name == "com.android.settings.widget.SettingsAppWidgetProvider" && actInfo.exported) {
                    return true
                }
            }
        }
        return false //default
    }
}
