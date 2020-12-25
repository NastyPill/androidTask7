package com.example.lab7task3

import android.app.IntentService
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import java.io.FileOutputStream
import java.lang.ref.WeakReference

class ImageService : IntentService("ImageService") {
    private lateinit var messenger: Messenger

    override fun onHandleIntent(intent: Intent?) =
        sendBroadcast(
            Intent("broadcastBoundImagePath").putExtra(
                "message",
                downloadPath(intent?.getStringExtra("link"))))

    override fun onBind(intent: Intent): IBinder? {
        messenger = Messenger(ServiceHandler(this))
        return messenger.binder
    }

    private fun downloadPath(url: String?): String {
        return try {
            val fileOutputStream: FileOutputStream =
                openFileOutput("img2.png", MODE_PRIVATE)
            val bitmap = BitmapFactory.decodeStream(java.net.URL(url).openStream())
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()
            getFileStreamPath("img2.png").absolutePath
        } catch (e: Exception) {
            ""
        }
    }
    fun send(url: String, messenger: Messenger?) {
        val path = downloadPath(url)
        val message = Message.obtain(null, 2).apply {
            data = Bundle().apply { putString("response", path) }
        }
        messenger?.send(message)
    }
}

private class ServiceHandler(private val serviceContext: ImageService) : Handler() {
    override fun handleMessage(message: Message) {
        when (message.what) {
            1 -> {
                serviceContext.send(
                    message.data.getString(
                        "link",
                        "https://i.pinimg.com/600x315/87/49/0a/87490af3ef84dd02e8de84537eeb9cdd.jpg"), message.replyTo)
            }
        }
    }
}