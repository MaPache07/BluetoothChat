package com.mapache.bluetoothchat

import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.session.PlaybackState
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import com.example.bluetooth.OnlineChat
import com.example.bluetooth.UtilClass
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var dialog: Dialog
    lateinit var chatAdapter: ArrayAdapter<String>
    lateinit var chatMessages: ArrayList<String>


    private val REQUEST_ENABLE_BLUETOOTH = 1
    lateinit var connectingDevice: BluetoothDevice
    lateinit var discoveredDevicesAdapter: ArrayAdapter<String>
    var chatController: OnlineChat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_username.setOnClickListener {
            tv_username.text = ed_username.text.toString()
            ed_username.setText("")
        }


        findViewsByIds()

        //TODO: check device support bluetooth or not
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "El Bluetooth no está disponible", Toast.LENGTH_SHORT).show();
            finish()
        }
        //TODO: show bluetooth devices dialog when click connect button
        btn_conexion.setOnClickListener { showPrinterPickDialog() }

        //TODO: set chat adapter
        chatMessages = ArrayList()
        chatAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, chatMessages)
        list.adapter = chatAdapter
    }



    //En este metodo se envian y se escriben los mensajes, al igual que se hace la coneccion con el dispositivo
    private val handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            UtilClass.MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                UtilClass.STATE_CONNECTED -> {
                    setStatus("Conectado a: " + connectingDevice.name)
                    btn_conexion.isEnabled = false
                }
                UtilClass.STATE_CONNECTING -> {
                    setStatus("Conectando")
                    btn_conexion.isEnabled = false
                }
                UtilClass.STATE_LISTEN, PlaybackState.STATE_NONE -> setStatus("Desconectado")
            }
            UtilClass.MESSAGE_WRITE -> {
                val writeBuf = msg.obj as ByteArray

                val writeMessage = String(writeBuf)
                chatMessages.add("Me: $writeMessage")
                chatAdapter.notifyDataSetChanged()
            }
            UtilClass.MESSAGE_READ -> {
                val readBuf = msg.obj as ByteArray

                val readMessage = String(readBuf, 0, msg.arg1)
                chatMessages.add(connectingDevice.name + ":  " + readMessage)
                chatAdapter.notifyDataSetChanged()
            }
            UtilClass.MESSAGE_DEVICE_OBJECT -> {
                connectingDevice = msg.data.getParcelable(UtilClass.DEVICE_OBJECT)!!
                Toast.makeText(applicationContext, "Conectado a" + connectingDevice.name,
                        Toast.LENGTH_SHORT).show()
            }
            UtilClass.MESSAGE_TOAST -> Toast.makeText(applicationContext, msg.getData().getString("toast"),
                    Toast.LENGTH_SHORT).show()
        }
        false
    })

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BLUETOOTH -> if (resultCode == Activity.RESULT_OK) {
                chatController = OnlineChat(this, handler)
            } else {
                Toast.makeText(this, "El bluetooth sigue desactivado, cierra la app", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    public override fun onResume() {
        super.onResume()

        if (chatController?.getState() == PlaybackState.STATE_NONE) {
            chatController?.start()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        chatController!!.stop()
    }

    override fun onStart() {

        //TODO: Checking if BLUETOOTH is enabled when we launch the activity
        super.onStart()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "El Bluetooth no está disponible", Toast.LENGTH_SHORT).show()
            finish()
        }


        if (!bluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH)
        } else {
            chatController = OnlineChat(this, handler)


        }
    }

    private fun setStatus(s: String) {
        tv_conexion.text = s
    }

    private fun findViewsByIds() {
        btn_enviar.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (tv_mensaje.text.toString() == "") {
                    Toast.makeText(this@MainActivity, "Escribe un mensaje", Toast.LENGTH_SHORT).show()
                } else {
                    //TODO: here
                    sendMessage(tv_mensaje.text.toString())
                    tv_mensaje.setText("")
                }
            }
        })
    }

    private fun showPrinterPickDialog() {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.modal_layout)
        dialog.setTitle("Dispositivos Bluetooth")

        if (bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.cancelDiscovery()
        }
        bluetoothAdapter!!.startDiscovery()

        //TODO: Initializing bluetooth adapters
        val pairedDevicesAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        discoveredDevicesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        //TODO: locate listviews and attatch the adapters
        val listView = dialog.findViewById(R.id.pairedDeviceList) as ListView
        val listView2 = dialog.findViewById(R.id.discoveredDeviceList) as ListView
        listView.adapter = pairedDevicesAdapter
        listView2.adapter = discoveredDevicesAdapter

        //TODO:  Register for broadcasts when a device is discovered
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoveryFinishReceiver, filter)

        //TODO:  Register for broadcasts when discovery has finished
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(discoveryFinishReceiver, filter)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = bluetoothAdapter!!.bondedDevices

        //TODO:  If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                pairedDevicesAdapter.add(device.name + "\n" + device.address)
            }
        } else {
            pairedDevicesAdapter.add("No se emparejó a ningún dispositivo")
        }

        //TODO: Handling listview item click event
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, view, _, _ ->
            bluetoothAdapter!!.cancelDiscovery()
            val info = (view as TextView).text.toString()
            val address = info.substring(info.length - 17)

            connectToDevice(address)
            dialog.dismiss()
        }

        listView2.onItemClickListener = AdapterView.OnItemClickListener { _, view, _, _ ->
            bluetoothAdapter!!.cancelDiscovery()
            val info = (view as TextView).text.toString()
            val address = info.substring(info.length - 17)

            connectToDevice(address)
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.cancelButton).setOnClickListener(View.OnClickListener { dialog.dismiss() })
        dialog.setCancelable(false)
        dialog.show()
    }

    private val discoveryFinishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                    discoveredDevicesAdapter.add(device.name + "\n" + device.address)
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                if (discoveredDevicesAdapter.count == 0) {
                    discoveredDevicesAdapter.add("No se encontraron dispositivos")
                }
            }
        }
    }

    private fun connectToDevice(deviceAddress: String) {
        bluetoothAdapter!!.cancelDiscovery()
        val device = bluetoothAdapter!!.getRemoteDevice(deviceAddress)
        chatController!!.connect(device)
    }

    //Aqui se envia el mensaje si es que esta disponible la conexion
    private fun sendMessage(message: String) {
        if (chatController!!.getState() != UtilClass.STATE_CONNECTED) {
            Toast.makeText(this, "Se perdió la conexión", Toast.LENGTH_SHORT).show()
            return
        }

        if (message.isNotEmpty()) {
            val send = message.toByteArray()
            chatController!!.write(send)
        }
    }
}
