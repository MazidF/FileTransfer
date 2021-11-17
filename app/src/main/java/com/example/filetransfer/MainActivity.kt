package com.example.filetransfer

import android.R.attr
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.filetransfer.databinding.ActivityMainBinding
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import android.content.res.AssetFileDescriptor
import android.R.attr.data
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    val list = ArrayList<MyFile>()
    //    private var host = "127.0.0.1"
    //    private var host = "10.0.2.2"
    private var host = "192.168.1.3"
    private var port = 9999
    private lateinit var filesAdapter: ArrayAdapter<String>
    private lateinit var typesAdapter: ArrayAdapter<String>
    lateinit var out: ObjectOutputStream
    lateinit var output: OutputStream
    lateinit var socketObject: Socket
    private val fileRequestCode = 1
    private val scanRequestCode = 2
    lateinit var dialog: Dialog
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        getIpAddress()
    }

    private fun getIpAddress() {
        try {
            val intent = Intent("com.google.zxing.client.android.SCAN")
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE") // "PRODUCT_MODE for bar codes
            startActivityForResult(intent, scanRequestCode)
        } catch (e: Exception) {
            Log.e("scan", e.stackTraceToString())
            val marketUri = Uri.parse("market://details?id=com.google.zxing.client.android")
            val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
            startActivity(marketIntent)
        }
//        startActivityForResult(Intent(this, QrCodeScanner::class.java), scanRequestCode)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("Send").setOnMenuItemClickListener {
            sendObjectPython(this, 0)
            true
        }.setShowAsAction(1)
        menu.add("Exit").setOnMenuItemClickListener {
            onClose()
            finish()
            true
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun onClose() {
        doTask {
            try {
                output.write("Exit".encodeToByteArray())
                output.flush()
            } catch (e: java.lang.Exception) {}
        }
    }

    private fun spinnerInit(spinnerKind: Spinner, spinnerType: Spinner, type: String) {
        var list = (resources.getStringArray(R.array.file_list).iterator()).asSequence().toList()
        list = ArrayList<String>().apply {
            this.addAll(list)
            this.add(0, type)
        }
        filesAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list) {
            override fun isEnabled(position: Int): Boolean {
                if (position == 0) return false
                return super.isEnabled(position)
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                return super.getDropDownView(position, convertView, parent).apply {
                    (this as TextView).setTextColor (if (position == 0) Color.GRAY else Color.BLACK)
                }
            }
        }
        filesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKind.adapter = filesAdapter
        spinnerKind.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                Toast.makeText(this@MainActivity, "$position", Toast.LENGTH_SHORT).show()
                typesAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, resources.getStringArray(when (position - 1) {
                        0 -> {
                            R.array.Image_types
                        }
                        1 -> {
                            R.array.Document_types
                        }
                        2 -> {
                            R.array.Video_types
                        }
                        3 -> {
                            R.array.Presentation_types
                        }
                        4 -> {
                            R.array.Audio_types
                        }
                        else -> {
                            return
                        }
                    })
                )
                spinnerType.visibility = View.VISIBLE
                spinnerType.adapter = typesAdapter
                spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {}

                    override fun onNothingSelected(p0: AdapterView<*>?) {}

                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    fun onClick(view: View) {
        chooseFile(this, fileRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return
        when (requestCode) {
            fileRequestCode -> {
                data.data?.let {
                    var type = getType(this, it)
                    if (type == null) {
                        type = "Unknown"
                    }
                    addView(it, type)
                }
            }
            scanRequestCode -> {
                val contents = data.getStringExtra("SCAN_RESULT") ?: return
                this.host = contents.split(":")[0]
                this.port = contents.split(":")[1].toInt()
                val address = InetSocketAddress(this.host, this.port)
                handler(address)
            }
        }
    }

    private fun handler(address: InetSocketAddress) {
        val progressDialog = ProgressDialog(this@MainActivity).apply {
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            this.setCancelable(false)
        }
        doTask {
            socketObject = Socket()
            try {
                runOnUiThread{ progressDialog.show() }
                socketObject.connect(address, 2000)
                socketObject.sendBufferSize = 1_000_000
                output = socketObject.getOutputStream()
                out = ObjectOutputStream(output)
                runOnUiThread { progressDialog.dismiss() }
            } catch (e: Exception) {
                socketObject.close()
                runOnUiThread { progressDialog.dismiss() }
                Log.e("socketException", e.stackTraceToString())
                this@MainActivity.runOnUiThread {
                    val dialog = Dialog(this@MainActivity).apply {
                        this.setCancelable(false)
                        this.setContentView(R.layout.ip_address_dialog)
                        val hostAddress =
                            findViewById<EditText>(R.id.host_address).apply { setText(this@MainActivity.host) }
                        val portAddress =
                            findViewById<EditText>(R.id.port_address).apply { setText(this@MainActivity.port.toString()) }
                        findViewById<Button>(R.id.save_button).setOnClickListener {
                            val host = hostAddress.text.toString().trim()
                            val port = portAddress.text.toString().trim()
                            if (host.isEmpty()) {
                                hostAddress.error = "wrong host"
                                hostAddress.requestFocus()
                            }
                            if (port.isEmpty()) {
                                portAddress.error = "wrong port"
                                if (host.isNotEmpty()) {
                                    portAddress.requestFocus()
                                }
                                return@setOnClickListener
                            } else {
                                if (host.isNotEmpty()) {
                                    this@MainActivity.host = host
                                    this@MainActivity.port = port.toInt()
                                    this.dismiss()
                                    runOnUiThread{ progressDialog.show() }
                                    doTask {
                                        try {
                                            socketObject = Socket()
                                            socketObject.connect(
                                                InetSocketAddress(
                                                    this@MainActivity.host,
                                                    this@MainActivity.port
                                                ), 2000
                                            )
                                            socketObject.sendBufferSize = 1_000_000
                                            output = socketObject.getOutputStream()
                                            out = ObjectOutputStream(output)
                                            runOnUiThread { progressDialog.dismiss() }
                                        } catch (e: Exception) {
                                            socketObject.close()
                                            runOnUiThread { progressDialog.dismiss() }
                                            Log.e("socketException", e.stackTraceToString())
                                            runOnUiThread { this.show() }
                                        }
                                    }
                                }
                            }
                        }
                        findViewById<ImageView>(R.id.scan).setOnClickListener {
                            this.dismiss()
                            getIpAddress()
                        }
                    }
                        .apply {
                            val lp = WindowManager.LayoutParams()
                            lp.copyFrom(this.window?.attributes)
                            lp.width = WindowManager.LayoutParams.MATCH_PARENT
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                            this.show()
                            this.window?.attributes = lp
                        }
                    this.dialog = dialog
                    dialog.show()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addView(uri: Uri, type: String) {
        val view = View.inflate(this, R.layout.file_layout, null)
        val fileName = view.findViewById<EditText>(R.id.file_name)
        val fileKind = view.findViewById<Spinner>(R.id.file_kind)
        val fileType = view.findViewById<Spinner>(R.id.file_type)
        val fileDescriptor = applicationContext.contentResolver.openAssetFileDescriptor(uri, "r")
        val fileSize = view.findViewById<TextView>(R.id.file_size).apply {
            var size = (fileDescriptor!!.length / 1000_000f).toString()
            if (size.length > 4) {
                size = size.substring(0, size.length - 4)
                this.text = size
            } else {
                this.text = "near zero"
            }
        }
        val delete = view.findViewById<Button>(R.id.delete_file).apply {
            setOnClickListener {
                if (view.tag != null) {
                    list.remove(view.tag as MyFile)
                }
                binding.listFiles.removeView(view)
            }
        }
        val save = view.findViewById<Button>(R.id.save_file).apply {
            setOnClickListener {
                var name: String? = fileName.text.toString().trim()
                if (name!!.isEmpty()) {
                    fileName.error = "wrong name"
                    fileName.requestFocus()
                    name = null
                }
                val typeInput: String
                val position = fileKind.selectedItemPosition
                if (position == 0) {
                    if (type == "Unknown") {
                        (fileKind.selectedItem as TextView).error = "Choose an item"
                        return@setOnClickListener
                    } else {
                        typeInput = type
                    }
                } else {
                    typeInput = fileType.selectedItem.toString()
                }
                if (name != null && type != "Unknown") {
                    val myFile: MyFile
                    if (view.tag != null){
                        myFile = view.tag as MyFile
                        myFile.name = name
                        myFile.type = type
                    } else {
                        myFile = MyFile(name, typeInput, uri)
                        view.tag = myFile
                        list.add(myFile)
                    }
                    val miniView = View.inflate(this@MainActivity, R.layout.mini_file_layout, null)
                    miniView.findViewById<EditText>(R.id.file_full_name).setText("$name.$type")
                    miniView.findViewById<ImageView>(R.id.imageView2).setOnClickListener {
                        val index = binding.listFiles.indexOfChild(miniView)
                        binding.listFiles.addView(view, index)
                        binding.listFiles.removeView(miniView)
                    }
                    val index = binding.listFiles.indexOfChild(view)
                    binding.listFiles.addView(miniView, index)
                    binding.listFiles.removeView(view)
                    miniView.tag = myFile
                    binding.page.requestFocus()
                }
            }
        }
        spinnerInit(fileKind, fileType, type)
        binding.listFiles.addView(view)
        binding.scroll.fullScroll(View.FOCUS_DOWN)
        fileName.requestFocus()
    }
}
