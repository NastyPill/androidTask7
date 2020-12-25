package com.example.lab7task3

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    private var done = false
    private val messenger = Messenger(ClientHandler(this))
    private var boundMessenger: Messenger? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            boundMessenger = null
            done = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            boundMessenger = Messenger(service)
            done = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, ImageService::class.java), serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (done) {
            unbindService(serviceConnection)
            done = false
        }
    }

    fun onClick(view: View) {
        val message = Message.obtain(null, 1).apply {
            replyTo = messenger
            data = Bundle().apply {
                putString(
                    "link",
                    "https://i.pinimg.com/600x315/87/49/0a/87490af3ef84dd02e8de84537eeb9cdd.jpg")
            }
        }
        boundMessenger?.send(message)
    }
}

private class ClientHandler(
    context: MainActivity,
    private val activityReference: WeakReference<MainActivity> = WeakReference(
        context)) : Handler() {
    override fun handleMessage(message: Message) {
        when (message.what) {
            2 -> {
                val activity = activityReference.get()
                if (activity != null && !activity.isFinishing)
                    activity.findViewById<TextView>(R.id.text).text =
                        message.data.getString("response")
            }
        }
    }
}