package com.example.filetransfer


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.net.Socket
import java.util.*


fun chooseFile(activity: AppCompatActivity, requestCode: Int) {
    var intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = "*/*"
    intent = Intent.createChooser(intent, "Choose File")
    activity.startActivityForResult(intent, requestCode)
}

fun uriToByte(uri: Uri): ByteArray? {
    val input: FileInputStream
    try {
        uri.path?.let {
            input = FileInputStream(File(it))
            return input.readBytes()
        }
    } catch (e: Exception) {
        Log.e("fileT", e.stackTraceToString())
    }
    return null
}

fun uriToByte2(uri: Uri): ByteArray? {
    if (uri.path == null) return null
    val baos = ByteArrayOutputStream()
    val fis: FileInputStream
    try {
        fis = FileInputStream(File(uri.path!!))
        val buf = ByteArray(1024)
        var n: Int
        while (-1 != fis.read(buf).also { n = it }) baos.write(buf, 0, n)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return baos.toByteArray()
}

fun uriToByte3(uri: Uri, activity: AppCompatActivity): ByteArray? {
    return activity.contentResolver.openInputStream(uri)?.let { readBytes(it) }
}

fun readBytes(inputStream: InputStream): ByteArray? {
    // this dynamically extends to take the bytes you read
    val byteBuffer = ByteArrayOutputStream()

    // this is storage overwritten on each iteration with bytes
    val bufferSize = 1024
    val buffer = ByteArray(bufferSize)

    // we need to know how may bytes were read to write them to the byteBuffer
    var len = 0
    while (inputStream.read(buffer).also { len = it } != -1) {
        byteBuffer.write(buffer, 0, len)
    }

    // and then we can return your byte array.
    return byteBuffer.toByteArray()
}

fun getType(activity: AppCompatActivity, uri: Uri): String? {
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri))
}

fun sendObject(activity: MainActivity, myFile: MyFile) {
    doTask {
        activity.out.writeObject(myFile)
        activity.out.flush()
    }
}

@RequiresApi(Build.VERSION_CODES.N)
fun sendObjectPython(activity: MainActivity, myFileIndex: Int, delay: Long = 3000) {
    if (myFileIndex >= activity.list.size) {
        activity.list.clear()
        return
    }

    val myFile = activity.list[myFileIndex]
    val inputStream = activity.contentResolver.openInputStream(myFile.uri)!!
    doTask {
        activity.output.write("${myFile.name}.${myFile.type}SIZE:${inputStream.available()}".encodeToByteArray())
        activity.output.flush()
    }
    timer(1500) {
        doTask {
            val byteArray = ByteArray(1_000_000)
            var read = inputStream.read(byteArray)
            while (read != -1) {
                activity.output.write(byteArray, 0, read)
                activity.output.flush()
                read = inputStream.read(byteArray)
                Log.e("size", read.toString())
            }
            activity.output.write("END__".encodeToByteArray())
            activity.output.flush()
            Log.e("done", "done")
        }
        activity.runOnUiThread {
            val linearLayout = activity.binding.listFiles
            val view = linearLayout.findViewWithTag<View>(myFile)
            linearLayout.removeView(view)
            timer(delay) {
                sendObjectPython(activity, myFileIndex + 1)
            }
        }
    }
}

fun timer(millieSeconds: Long, task: () -> Unit) {
    Timer().schedule(object : TimerTask() {
        override fun run() {
            task()
        }
    }, millieSeconds)
}

fun doTask(task: () -> Unit) {
    val a =
        @SuppressLint("StaticFieldLeak")
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg p0: Void?): Void? {
                task()
                return null
            }
        }
    a.execute()
}
