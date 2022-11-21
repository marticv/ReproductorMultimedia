package com.marticurto.reproductormultimedia

import android.Manifest
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/**
 * App para reproducir musica y video
 *
 * @author Martí Curto Vendrell
 *
 */
class MainActivity : AppCompatActivity() {
    lateinit var spSource:Spinner
    lateinit var btPlay: Button
    lateinit var btPause: Button
    lateinit var btStop: Button
    lateinit var ibRewind: ImageButton
    lateinit var ibFastForward: ImageButton
    lateinit var tvState : TextView
    lateinit var tvTimer:TextView
    lateinit var tvDuration:TextView
    lateinit var progresBar:ProgressBar
    lateinit var videoView:VideoView



    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // asignamos variables a views
        spSource = findViewById(R.id.spSource)
        btPlay = findViewById(R.id.btplay)
        btPause = findViewById(R.id.btPause)
        btStop = findViewById(R.id.btStop)
        ibRewind = findViewById(R.id.ibRewind)
        ibFastForward = findViewById(R.id.ibFastForward)
        tvState = findViewById(R.id.tvState)
        tvTimer = findViewById(R.id.tvTimer)
        tvDuration =findViewById(R.id.tvDuration)
        progresBar = findViewById(R.id.progressBar)
        videoView = findViewById(R.id.videoView)


        val myUri: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.music)
        var mpMusic1 = MediaPlayer.create(this, myUri)

        var mpMusic2 = MediaPlayer()
        mpMusic2.setDataSource(applicationContext,myUri)

        var mpVideo = MediaPlayer()


        val videoUri:Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.video)
        videoView.setVideoURI(videoUri)

        val vidControl = MediaController(this)
        vidControl.setAnchorView(videoView)
        videoView.setMediaController(vidControl)


        //preparamos la parte visual
        fillSpinner(spSource)
        btPause.isEnabled=false
        btStop.isEnabled=false


        //damos funcionalidad a los botones
        btPlay.setOnClickListener { starPlay(mpMusic1,mpMusic2,videoView) }
        btPause.setOnClickListener { pauseReproduction(mpMusic1,mpMusic2,videoView) }
        btStop.setOnClickListener { stopReproduction(mpMusic1,mpMusic2,videoView) }
        ibRewind.setOnClickListener { rewind(mpMusic1,mpMusic2,videoView) }
        ibFastForward.setOnClickListener { fastFroward(mpMusic1,mpMusic2,videoView) }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    /*
    funciones para la parte visual
     */

    /**
     * Rellena el spinner con Strings prediseñadas
     *
     * @param sp
     */
    private fun fillSpinner(sp:Spinner){
        val sources = arrayOf("Musica mediante \"create\"","Musica mediante \"setDataSource\"","Video")
        val adapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, sources)
        sp.adapter=adapter
    }

    /**
     * Obtenemos la posicion del item seleccionado en el spinner
     *
     * @param sp
     * @return
     */
    private fun obtainSource(sp:Spinner):Int{
        return sp.selectedItemPosition
    }

    /**
     * Obtenemos la duracion de la cancion
     *
     * @param mp
     * @return
     */
    private fun getDuration(mp: MediaPlayer):String{
        val duration:Long = (mp.duration.toLong() / 1000)
        return duration.toString()
    }

    /*
    funciones para la reproduccion
     */

    private fun starPlay(mp1:MediaPlayer, mp2:MediaPlayer, video:VideoView) {
        tvState.text = "Playing"

        //habilitamos los botones stop y pause y deshabilitamos el play
        btPlay.isEnabled = false
        btPause.isEnabled = true
        btStop.isEnabled = true

        if (obtainSource(spSource) == 0) {
            tvDuration.text=getDuration(mp1)

            //empezamos a reproducir
            mp1.start()
        }else if(obtainSource(spSource)==1){
            tvDuration.text=getDuration(mp1)
            mp2.prepare()
            mp2.start()
        }else{
            val duration = video.duration.toLong()/1000
            tvDuration.text=duration.toString()
            video.start()
        }
    }
    private fun pauseReproduction(mp1:MediaPlayer, mp2:MediaPlayer, video:VideoView){
        tvState.text="Paused"

        //deshabilitamos el boton facilitar la vida al usuario
        btPause.isEnabled=false
        btPlay.isEnabled=true
        btStop.isEnabled=true

        if (obtainSource(spSource) == 0) {
            //pausamos
            mp1.pause()
        }else if(obtainSource(spSource)==1){
            mp2.pause()
        }else{
            video.pause()
        }
    }

    private fun stopReproduction(mp1:MediaPlayer, mp2:MediaPlayer, video:VideoView){
        tvState.text="Stoped"

        //deshabilitamos los botones para que inidicar al usuario que puede hacer
        btStop.isEnabled=false
        btPause.isEnabled=false
        btPlay.isEnabled=true

        if (obtainSource(spSource) == 0) {
            //empezamos a reproducir
            mp1.stop()
            mp1.prepare()
        }else if(obtainSource(spSource)==1){
            mp2.stop()
            mp2.prepare()
        }else{
            video.stopPlayback()
            video.resume()
        }
    }

    /**
     * Retrocede la musica 10s
     *
     * @param mp
     */
    private fun rewind(mp1:MediaPlayer, mp2:MediaPlayer, video:VideoView){
        if(obtainSource(spSource)==0){
        val position =mp1.currentPosition
        if(position>1000){
            mp1.seekTo(position-10000)
        }else{
            mp1.seekTo(0)
        }
        }else if(obtainSource(spSource)==1){

        }else{
            val position =videoView.currentPosition
            video.seekTo(position-10000)
        }

    }

    /**
     * avanza la musica 10s
     *
     * @param mp
     */
    private fun fastFroward(mp1:MediaPlayer, mp2:MediaPlayer, video:VideoView){
        if(obtainSource(spSource)==0){
            val position =mp1.currentPosition
            mp1.seekTo(position+10000)
        }else if(obtainSource(spSource)==1){

        }else{
            val position =videoView.currentPosition
            video.seekTo(position+10000)
        }
    }

    /*
    funcionalidad de la barra de progreso
     */

    /*

    inner class MediaObserver : Runnable {
        // Se define el stop para poder parar el thread desde la aplicación
        private val stop: AtomicBoolean = AtomicBoolean(false)


        fun stop() {
            stop.set(true)
        }

        override fun run() {
            progresBar.progress=mp.curr


        }
    }*/

}