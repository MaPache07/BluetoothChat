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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetooth.OnlineChat
import com.example.bluetooth.constantes
import com.mapache.bluetoothchat.adapters.adapterMensajes
import com.mapache.bluetoothchat.database.viewModel.viewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var dialog: Dialog


    private val REQUEST_ENABLE_BLUETOOTH = 1
    lateinit var connectingDevice: BluetoothDevice
    lateinit var discoveredDevicesAdapter: ArrayAdapter<String>
    var chatController: OnlineChat? = null

    private lateinit var viewModel : viewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_username.setOnClickListener {
            tv_username.text = ed_username.text.toString()
            ed_username.setText("")
        }

        btn_enviar.setOnClickListener {
            if (tv_username.text.toString() != "Username"){
                findViewsByIds()
            } else{
                Toast.makeText(this, "Debe ingresar un usuario", Toast.LENGTH_SHORT).show()
            }
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no disponible!!!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btn_conexion.setOnClickListener { showPrinterPickDialog() }

        viewModel = ViewModelProviders.of(this).get(com.mapache.bluetoothchat.database.viewModel.viewModel::class.java)

        var adapter = adapterMensajes(emptyList())

        rv_mensajes.adapter = adapter
        rv_mensajes.layoutManager = LinearLayoutManager(this)

        viewModel.getAll().observe(this, Observer { mensajes ->
            mensajes?.let {
                adapter.setMensaje(it)
            }
        })
    }

    //En este metodo se envian y se escriben los mensajes, al igual que se hace la coneccion con el dispositivo
    private val handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            constantes.MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                constantes.STATE_CONNECTED -> {
                    setStatus("Conectado a: " + connectingDevice.name)
                    btn_conexion.isEnabled = false
                }
                constantes.STATE_CONNECTING -> {
                    setStatus("Conectando")
                    btn_conexion.isEnabled = false
                }
                constantes.STATE_LISTEN, PlaybackState.STATE_NONE -> setStatus("Desconectado")
            }
            constantes.MESSAGE_WRITE -> {
                val writeBuf = msg.obj as ByteArray

                val writeMessage = String(writeBuf)

                var time = Calendar.getInstance().time.hours.toString() + ":" + Calendar.getInstance().time.minutes.toString()
                viewModel.insertMessage(com.mapache.bluetoothchat.database.entities.Message(0, "1", writeMessage, time))
            }
            constantes.MESSAGE_READ -> {
                val readBuf = msg.obj as ByteArray

                val readMessage = String(readBuf, 0, msg.arg1)

                var time = Calendar.getInstance().time.hours.toString() + ":" + Calendar.getInstance().time.minutes.toString()
                viewModel.insertMessage(com.mapache.bluetoothchat.database.entities.Message(0, "2", readMessage, time))
            }
            constantes.MESSAGE_DEVICE_OBJECT -> {
                connectingDevice = msg.data.getParcelable(constantes.DEVICE_OBJECT)!!
                Toast.makeText(applicationContext, "Conectado a" + connectingDevice.name,
                        Toast.LENGTH_SHORT).show()
            }
            constantes.MESSAGE_TOAST -> Toast.makeText(applicationContext, msg.getData().getString("toast"),
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
                if (ed_mensaje.text.toString() == "") {
                    Toast.makeText(this@MainActivity, "Escribe un mensaje", Toast.LENGTH_SHORT).show()
                } else {
                    sendMessage(tv_username.text.toString() + ": " + ed_mensaje.text.toString())
                    ed_mensaje.setText("")
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

        val pairedDevicesAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        discoveredDevicesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        val listView = dialog.findViewById(R.id.pairedDeviceList) as ListView
        val listView2 = dialog.findViewById(R.id.discoveredDeviceList) as ListView
        listView.adapter = pairedDevicesAdapter
        listView2.adapter = discoveredDevicesAdapter

        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoveryFinishReceiver, filter)

        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(discoveryFinishReceiver, filter)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = bluetoothAdapter!!.bondedDevices

        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                pairedDevicesAdapter.add(device.name + "\n" + device.address)
            }
        } else {
            pairedDevicesAdapter.add("No se emparejó a ningún dispositivo")
        }
        0
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
        if (chatController!!.getState() != constantes.STATE_CONNECTED) {
            Toast.makeText(this, "Se perdió la conexión", Toast.LENGTH_SHORT).show()
            btn_conexion.isEnabled = true
            return
        }

        if (message.isNotEmpty()) {
            val send = message.toByteArray()
            chatController!!.write(send)
        }
    }
}
