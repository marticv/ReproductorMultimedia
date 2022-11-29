package com.marticurto.reproductormultimedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.ImageButton

class PopUpSubcribe : AppCompatActivity() {
    private lateinit var btClose:ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pop_up_subcribe)

        //obtenemos las medidas de la ventana y las modificamos
        var medidas:DisplayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(medidas)
        val width = medidas.widthPixels
        val height = medidas.heightPixels
        window.setLayout((width*0.85).toInt(), (height*0.5).toInt())

        //damos funcionalidad al boton de cerrar
        btClose =findViewById(R.id.btClose)
        btClose.setOnClickListener {finish()}
    }
}