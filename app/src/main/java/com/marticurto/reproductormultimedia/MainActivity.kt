package com.marticurto.reproductormultimedia

import android.Manifest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/**
 * App para reproducir musica y video. Musica obtenida de https://file-examples.com/ y video de https://www.learningcontainer.com/
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
    lateinit var seekBar: SeekBar
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
        seekBar = findViewById(R.id.seekBar)
        videoView = findViewById(R.id.videoView)


        //creamos recursos audiovisuales
        val myUri: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.music)
        var mpMusic1 = MediaPlayer.create(this, myUri)

        val myUri2: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.music2)
        var mpMusic2 = MediaPlayer()
        mpMusic2.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mpMusic2.setDataSource(applicationContext,myUri2)
        mpMusic2.prepare()

        val videoUri:Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.video)
        videoView.setVideoURI(videoUri)

        val vidControl = MediaController(this)
        vidControl.setAnchorView(videoView)
        videoView.setMediaController(vidControl)


        //preparamos la parte visual
        btPause.isEnabled=false
        btStop.isEnabled=false
        fillSpinner(spSource)

        //creamos un observador para controlar que pasa cuando se clica en el spinner
        spSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                initialState(mpMusic1,mpMusic2, videoView)
                initializeSeekbar(mpMusic1,mpMusic2,videoView)
            }
        }

        controlSound(mpMusic1,mpMusic2,videoView)


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
     * Deja el estado inicial de los botones y los mediaplayers preparado
     *
     */
    private fun initialState(mp1: MediaPlayer, mp2: MediaPlayer, video: VideoView){
        btPlay.isEnabled=true
        btStop.isEnabled=false
        btPause.isEnabled=false

        if(mp1.isPlaying){
            mp1.stop()
            mp1.prepare()
        }
        if (mp2.isPlaying){
            mp2.stop()
            mp2.prepare()
        }
        if(video.isPlaying){
            video.stopPlayback()
            video.resume()
        }

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
            tvDuration.text=getDuration(mp2)
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
    private fun rewind(mp1: MediaPlayer, mp2: MediaPlayer, video: VideoView) {
        if (obtainSource(spSource) == 0) {
            val position = mp1.currentPosition
            if (position > 1000) {
                mp1.seekTo(position - 10000)
            } else {
                mp1.seekTo(0)
            }
        } else if (obtainSource(spSource) == 1) {
            val position = mp2.currentPosition
            if (position > 1000) {
                mp2.seekTo(position - 10000)
            } else {
                mp2.seekTo(0)
            }
        } else {
            val position = videoView.currentPosition
            video.seekTo(position - 10000)
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
            val position=mp2.currentPosition
            mp2.seekTo(position+10000)
        }else{
            val position =videoView.currentPosition
            video.seekTo(position+10000)
        }
    }

    /*
funcionalidad de la barra de progreso
 */

    private fun controlSound(mp1: MediaPlayer, mp2: MediaPlayer, video: VideoView) {

        initializeSeekbar(mp1, mp2, videoView)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if(spSource.selectedItemPosition==0)mp1.seekTo(progress)
                    if(spSource.selectedItemPosition==1)mp2.seekTo(progress)
                    if(spSource.selectedItemPosition==2)video.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initializeSeekbar(mp1: MediaPlayer, mp2: MediaPlayer, video: VideoView){
        if(spSource.selectedItemPosition==0){
            seekBar.max =mp1.duration
        }else if(spSource.selectedItemPosition==1){
            seekBar.max =mp2.duration
        }else{
            seekBar.max =video.duration
        }

        val handler = Handler()
        handler.postDelayed(object :Runnable{
            override fun run() {
                try {
                    if(spSource.selectedItemPosition==0) {
                        seekBar.progress = mp1.currentPosition
                        tvTimer.text = (mp1.currentPosition / 1000).toString()
                    }else if(spSource.selectedItemPosition==1){
                        seekBar.progress = mp2.currentPosition
                        tvTimer.text = (mp2.currentPosition/1000).toString()
                    }else{
                        seekBar.progress = video.currentPosition
                        tvTimer.text = (video.currentPosition/1000).toString()
                    }
                    handler.postDelayed(this,0)
                }catch (e:Exception){
                    seekBar.progress=0
                }
            }
        },0)

    }
}