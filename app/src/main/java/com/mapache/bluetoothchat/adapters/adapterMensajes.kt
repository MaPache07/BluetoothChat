package com.mapache.bluetoothchat.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mapache.bluetoothchat.R
import com.mapache.bluetoothchat.database.entities.Message
import kotlinx.android.synthetic.main.recyclermensajes.view.*

class adapterMensajes(var mensaje : List<Message>) : RecyclerView.Adapter<adapterMensajes.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclermensajes, parent, false))
    }

    override fun getItemCount(): Int = mensaje.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.onBind(mensaje[position])

    internal fun setMensaje(mensaje : List<Message>){
        this.mensaje = mensaje
        //notifyDataSetChanged()
        notifyItemChanged(0, this.itemCount)
    }

    class ViewHolder(var view : View) : RecyclerView.ViewHolder(view){
        fun onBind(mensaje : Message){
            view.tv_mensaje.text = mensaje.text
            view.tv_hora.text = mensaje.time

            if (mensaje.userName == "1"){
                view.linearLayout.setBackgroundColor(Color.rgb(63, 162, 55))
            } else{
                view.linearLayout.setBackgroundColor(Color.rgb(162, 116, 55))
            }
        }
    }
}