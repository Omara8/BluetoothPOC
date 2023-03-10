package com.example.bluetoothpoc

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

@Throws(IOException::class)
fun saveFile(context: Context, fileName: String, text: String, extension: String) {
    val outputStream: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + extension) // file name
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        val extVolumeUri: Uri = MediaStore.Files.getContentUri("external")
        val fileUri: Uri? = context.contentResolver.insert(extVolumeUri, values)
        context.contentResolver.openOutputStream(fileUri!!)
    } else {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "POC"
        val file = File(path, fileName + extension)
        FileOutputStream(file)
    }
    val bytes = text.toByteArray()
    outputStream?.write(bytes)
    outputStream?.close()
}