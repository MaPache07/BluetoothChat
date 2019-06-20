package com.mapache.bluetoothchat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private val APP_NAME = "BluetoothChat"
    private val MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    private inner class Server : Thread() {
        private val serverSocket: BluetoothServerSocket?

        init {
            var tmp: BluetoothServerSocket? = null
            try {
                //Permiso de Bluetooth
                tmp = bluetoothAdapter!!.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            serverSocket = tmp
        }

        override fun run(){
            var socket: BluetoothSocket?

        }
    }
}
