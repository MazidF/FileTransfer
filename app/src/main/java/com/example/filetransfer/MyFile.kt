package com.example.filetransfer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.io.Serializable
import java.net.Socket


data class MyFile(var name: String, var type: String, val uri: Uri): Serializable {
    companion object {
        private const val serialVersionUID: Long = -17845413316L
    }
}

data class MySocket(var socket: Socket) : Serializable

class QrCodeScanner : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private lateinit var mScannerView: ZXingScannerView
    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        // Programmatically initialize the scanner view
        mScannerView = ZXingScannerView(this)
        // Set the scanner view as the content view
        setContentView(mScannerView)
    }

    public override fun onResume() {
        super.onResume()
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this)
        // Start camera on resume
        mScannerView.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        // Stop camera on pause
        mScannerView.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        // Do something with the result here
        //If you would like to resume scanning, call this method below:
//        mScannerView.resumeCameraPreview(this)
        val intent = Intent()
        intent.putExtra("SCAN_RESULT", rawResult.text)
        setResult(RESULT_OK, intent)
        finish()
    }
}