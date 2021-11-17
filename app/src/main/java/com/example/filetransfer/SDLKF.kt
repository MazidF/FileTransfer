package com.example.filetransfer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.StringBuilder


class WifiAPController : AppCompatActivity() {
    var a = 2
    var b = 3
    var password: String? = null
    var APname: String? = null
    private var wifiManager: WifiManager? = null
    private val logTAG = "WifiAP"
    private var wifiState: Int
    private val o: Boolean

    @SuppressLint("StaticFieldLeak")
    internal open inner class wifiControllerTask(
        var wifiAPControllerClass: WifiAPController,
        var a: Boolean,
        var b: Boolean,
        var mContext: Context
    ) :
        AsyncTask<Any?, Any?, Any?>() {
        private fun a(arg3: Array<Void?>?): Void? {
            try {
                wifiToggle(wifiAPControllerClass, this.a)
            } catch (v0: Exception) {
            }
            return null
        }

        fun a() {
            val sdkCurrentVersion = 21
            try {
                if (this.a) {
                    if (Build.VERSION.SDK_INT < sdkCurrentVersion) {
                        return
                    }
                    wifiAPControllerClass.wifiToggle(mContext)
                    return
                }
                if (Build.VERSION.SDK_INT < sdkCurrentVersion) {
                    return
                }
            } catch (v0: Exception) {
                Log.e("noti error", v0.message!!)
            }
        }

        private fun a(arg2: Void?) {
            super.onPostExecute(arg2)
            try {
                this.a()
            } catch (v0: IllegalArgumentException) {
                try {
                    this.a()
                } catch (v0_1: Exception) {
                }
            }
            if (this.b) {
                wifiAPControllerClass.finish()
            }
        }

        override fun doInBackground(arg2: Array<Any?>): Any? {
            return this.a(arg2 as Array<Void?>)
        }

        override fun onPostExecute(arg1: Any?) {
            this.a(arg1 as Void?)
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }
    }

    companion object {
        private var g = 0
        private var h = 0
        private var i = 0
        private var j = 0
        fun wifiToggle(wifiAPController: WifiAPController, wifiToggleFlag: Boolean): Int {
            return wifiAPController.wifiToggle(wifiToggleFlag)
        }

        init {
            g = 0
            h = 0
            i = 1
            j = 4
        }
    }

    private fun initWifiAPConfig(wifiConfiguration: WifiConfiguration) {
        wifiConfiguration.SSID = "SomeName"
        wifiConfiguration.preSharedKey = "SomeKey1"
        wifiConfiguration.hiddenSSID = false
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
        wifiConfiguration.allowedKeyManagement.set(4)
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
    }

    private fun wifiToggle(wifiToggleFlag: Boolean): Int {
        var wifiState: Int
        var stateString: String
        var message: StringBuilder
        var sleepTimeout: Long = 500
        var maxAttemptCount = 10
        val errorState = -1
        Log.d(logTAG, "*** setWifiApEnabled CALLED **** $wifiToggleFlag")
        val wifiConfiguration = WifiConfiguration()
        initWifiAPConfig(wifiConfiguration)
        if (wifiToggleFlag && this.wifiState == errorState) {
            this.wifiState = wifiManager!!.wifiState
        }
        if (!(!wifiToggleFlag || wifiManager!!.connectionInfo == null)) {
            Log.d(logTAG, "disable wifi: calling")
            wifiManager!!.isWifiEnabled = false
            var attemptCount = maxAttemptCount
            while (attemptCount > 0) {
                if (wifiManager!!.wifiState == 1) {
                    break
                }
                Log.d(logTAG, "disable wifi: waiting, pass: " + (10 - attemptCount))
                try {
                    Thread.sleep(sleepTimeout)
                    --attemptCount
                } catch (v4_1: Exception) {
                }
            }
            Log.d(logTAG, "disable wifi: done, pass: " + (10 - attemptCount))
        }
        try {
            message = StringBuilder()
            stateString = if (wifiToggleFlag) "enabling" else "disabling"
            Log.d(logTAG, message.append(stateString).append(" wifi ap: calling").toString())
            Log.d(logTAG, APname!!)
            Log.d(logTAG, password!!)
            Log.d(
                logTAG, "" + wifiManager!!.javaClass.getMethod(
                    "setWifiApEnabled",
                    WifiConfiguration::class.java,
                    Boolean::class.javaPrimitiveType
                ).invoke(wifiManager, wifiConfiguration, true).toString()
            )
            val res = wifiManager!!.addNetwork(wifiConfiguration)
            Log.d(logTAG, "" + res)
            wifiState =
                wifiManager!!.javaClass.getMethod("getWifiApState").invoke(wifiManager) as Int
            Log.d(logTAG, "" + wifiState)
        } catch (v0_1: Exception) {
            Log.e("wifi", v0_1.message!!)
            wifiState = errorState
        }
        while (maxAttemptCount > 0) {
            if (this.wifiToggle() != h && this.wifiToggle() != b && this.wifiToggle() != j) {
                break
            }
            message = StringBuilder()
            stateString = if (wifiToggleFlag) "enabling" else "disabling"
            Log.d(
                logTAG,
                message.append(stateString).append(" wifi ap: waiting, pass: ")
                    .append(10 - maxAttemptCount).toString()
            )
            sleepTimeout = 500
            try {
                Thread.sleep(sleepTimeout)
                --maxAttemptCount
            } catch (v0_1: Exception) {
            }
        }
        message = StringBuilder()
        stateString = if (wifiToggleFlag) "enabling" else "disabling"
        Log.d(
            logTAG,
            message.append(stateString).append(" wifi ap: done, pass: ")
                .append(10 - maxAttemptCount).toString()
        )
        if (!wifiToggleFlag) {
            if (this.wifiState >= WifiManager.WIFI_STATE_ENABLING && this.wifiState <= WifiManager.WIFI_STATE_UNKNOWN || o) {
                Log.d(logTAG, "enable wifi: calling")
                wifiManager!!.isWifiEnabled = true
            }
            this.wifiState = errorState
            return wifiState
        }
        return wifiState
    }

    private fun wifiToggle(): Int {
        val v4 = 10
        val result: Int = try {
            wifiManager!!.javaClass.getMethod("getWifiApState").invoke(wifiManager) as Int
        } catch (v0: Exception) {
            -1
        }
        if (result >= v4) {
            g = v4
        }
        h = g
        i = g + 1
        a = g + 2
        b = g + 3
        j = g + 4
        return result
    }

    fun wifiToggle(context: Context?) {
        val v0 = Intent(context, MainActivity::class.java)
    }

    fun wifiToggle(apname: String?, pass: String?, wifiManager: WifiManager?, context: Context) {
        var v2 = true
        if (this.wifiManager == null) {
            this.wifiManager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        }
        APname = apname
        password = pass
        val v0 = if (this.wifiToggle() == b || this.wifiToggle() == a) 1 else 0
        if (v0 != 0) {
            v2 = false
        }
        wifiControllerTask(this, v2, false, context).execute(*arrayOfNulls<Void>(0))
    }

    init {
        wifiState = -1
        o = false
    }
}