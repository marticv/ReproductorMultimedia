package com.marticurto.reproductormultimedia

import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.lang.Thread.sleep

/**
 * App para reproducir musica y video. Musica obtenida de https://file-examples.com/ y video de https://www.learningcontainer.com/
 *
 * @author Martí Curto Vendrell
 *
 */
class MainActivity : AppCompatActivity() {
    private lateinit var spSource:Spinner
    private lateinit var btPlay: Button
    private lateinit var btPause: Button
    private lateinit var btStop: Button
    private lateinit var ibRewind: ImageButton
    private lateinit var ibFastForward: ImageButton
    private lateinit var tvState : TextView
    private lateinit var tvTimer:TextView
    private lateinit var tvDuration:TextView
    private lateinit var tvMetadata:TextView
    private lateinit var tvMetadata2:TextView
    private lateinit var seekBar: SeekBar
    private lateinit var videoView:VideoView

    private lateinit var mpMusic1: MediaPlayer
    private lateinit var mpMusic2: MediaPlayer




    /* si necesitamos pedir permiso al usuario para leer y escribir archivos,
    los podemos hacer asi


    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )*/

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
        tvMetadata = findViewById(R.id.tvMetadata)
        tvMetadata2 = findViewById(R.id.tvMetadata2)
        tvTimer = findViewById(R.id.tvTimer)
        tvDuration =findViewById(R.id.tvDuration)
        seekBar = findViewById(R.id.seekBar)
        videoView = findViewById(R.id.videoView)


        //creamos recursos audiovisuales
        val myUri: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.music)
        mpMusic1 = MediaPlayer.create(this, myUri)

        val myUri2: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.music2)
        mpMusic2 = MediaPlayer()
        mpMusic2.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mpMusic2.setDataSource(applicationContext,myUri2)
        mpMusic2.prepare()

        val videoUri:Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.video)
        videoView.setVideoURI(videoUri)

        /*
        si queremos unos controles pror defecto podemos activar esto
        en nuestro caso no sera necesario ya que los creamos nosotros

        val vidControl = MediaController(this)
        vidControl.setAnchorView(videoView)
        videoView.setMediaController(vidControl)*/


        //preparamos la parte visual
        btPlay.text="play"
        btPause.text="Pause"
        btPause.isEnabled=false
        btStop.text="stop"
        btStop.isEnabled=false
        tvTimer.text="00:00"
        fillSpinner(spSource)
        tvDuration.text=getDuration(mpMusic1)

        //creamos un observador para controlar que pasa cuando se clica en el spinner
        spSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //cuando se cambie la seleccion del spinner reinicializamos los botones,textView y seekbar
                initialState(mpMusic1,mpMusic2, videoView)
                initializeSeekbar(mpMusic1,mpMusic2,videoView)
            }
        }

        //controlamos la barra de progreso
        controlSound(mpMusic1,mpMusic2,videoView)

        //damos funcionalidad a los botones
        btPlay.setOnClickListener { starPlay(mpMusic1,mpMusic2,videoView) }
        btPause.setOnClickListener { pauseReproduction(mpMusic1,mpMusic2,videoView) }
        btStop.setOnClickListener { stopReproduction(mpMusic1,mpMusic2,videoView) }
        ibRewind.setOnClickListener { rewind(mpMusic1,mpMusic2,videoView) }
        ibFastForward.setOnClickListener { fastFroward(mpMusic1,mpMusic2,videoView) }


        Handler().postDelayed({
            startActivity(Intent(MainActivity@this,PopUpSubcribe::class.java))
        }, 5000)

    }

    /**
     * Paramos la reproduccion y liberamos los recursos
     *
     */
    override fun onDestroy() {
        super.onDestroy()
        mpMusic1.stop()
        mpMusic2.stop()
        videoView.stopPlayback()
        videoView.resume()
        //si giramos la pantalla la activi se destrye y se vuelve a crear
        //asi que no podemos usar mpX.release() para desligar el recurso
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
        //solo dejamos el boton play activo, paramos cualquier reproduccion
        //y dejamos las musicas/video preparados
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

        if(spSource.selectedItemPosition==0)tvDuration.text=getDuration(mp1)
        if(spSource.selectedItemPosition==1)tvDuration.text=getDuration(mp2)
        if(spSource.selectedItemPosition==2) {

            var duration = video.duration.toLong()/1000
            tvDuration.text=duration.toString()
        }



        val myMetadata=obtainMetadata()
        tvMetadata.text=myMetadata[0]
        tvMetadata2.text=myMetadata[1]
    }

    /**
     * obtenemos metadoatos de las fuentes
     *
     * @return
     */
    private fun obtainMetadata():Array<String>{
        //creamos variables necesarias
        var metadata= MediaMetadataRetriever()
        val myMetaData:Array<String>
        val title:String?
        val genre:String?

        /*
        Para cada fuente obtenemos los metadatos a partir de la uri
         */
        if(spSource.selectedItemPosition==0) {
            metadata.setDataSource(
                MainActivity@ this,
                Uri.parse("android.resource://" + packageName + "/" + R.raw.music)
            )
            title = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            genre= metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            myMetaData = arrayOf("Titulo: $title","Genero: $genre")
        }else if(spSource.selectedItemPosition==1){
            metadata.setDataSource(
                MainActivity@ this,
                Uri.parse("android.resource://" + packageName + "/" + R.raw.music2)
            )
            title = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            genre= metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            myMetaData = arrayOf("Titulo: $title","Genero: $genre")
        }else{
            metadata.setDataSource(
                MainActivity@ this,
                Uri.parse("android.resource://" + packageName + "/" + R.raw.video)
            )
            val videoMetadata:String?=metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val videoMetadata2:String?=metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            myMetaData = arrayOf("Titulo: $videoMetadata","ancho del video: $videoMetadata2")
        }
        return myMetaData
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

    /**
     * Empieza a reproducir una musica o video
     *
     * @param mp1
     * @param mp2
     * @param video
     */
    private fun starPlay(mp1:MediaPlayer, mp2:MediaPlayer, video:VideoView) {
        tvState.text = "Playing"

        //habilitamos los botones stop y pause y deshabilitamos el play
        btPlay.isEnabled = false
        btPause.isEnabled = true
        btStop.isEnabled = true

        //iniciamos la reproduccion y modificamos el campo de duracion
        //segun la fuente seleccionada
        if (obtainSource(spSource) == 0) {
            mp1.start()
        }else if(obtainSource(spSource)==1){
            mp2.start()
        }else{
            video.start()
        }
    }

    /**
     * Tpausa la reproduccion del medio seleccionado
     *
     * @param mp1
     * @param mp2
     * @param video
     */
    private fun pauseReproduction(mp1:MediaPlayer, mp2:MediaPlayer, video:VideoView){
        tvState.text="Paused"

        //deshabilitamos el boton facilitar la vida al usuario
        btPause.isEnabled=false
        btPlay.isEnabled=true
        btStop.isEnabled=true

        //pausamos la reproduccion segun la fuente seleccionada
        if (obtainSource(spSource) == 0) {
            mp1.pause()
        }else if(obtainSource(spSource)==1){
            mp2.pause()
        }else{
            video.pause()
        }
    }

    /**
     * Paramos la reproduccion del medio que este seleccionado
     *
     * @param mp1
     * @param mp2
     * @param video
     */
    private fun stopReproduction(mp1:MediaPlayer, mp2:MediaPlayer, video:VideoView){
        tvState.text="Stopped"

        //deshabilitamos los botones para que inidicar al usuario que puede hacer
        btStop.isEnabled=false
        btPause.isEnabled=false
        btPlay.isEnabled=true

        //pausamos la reproduccion segun la fuente seleccionada
        if (obtainSource(spSource) == 0) {
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
     * @param mp1
     * @param mp2
     * @param video
     */
    private fun rewind(mp1: MediaPlayer, mp2: MediaPlayer, video: VideoView) {
        //retrocede la posicion de la fuente 10000ms
        if (obtainSource(spSource) == 0) {
            val position = mp1.currentPosition
            mp1.seekTo(position - 10000)
        } else if (obtainSource(spSource) == 1) {
            val position = mp2.currentPosition
            mp2.seekTo(position - 10000)
        } else {
            val position = videoView.currentPosition
            video.seekTo(position - 10000)
        }
    }

    /**
     * avanza la musica 10s
     *
     * @param mp1
     * @param mp2
     * @param video
     */
    private fun fastFroward(mp1:MediaPlayer, mp2:MediaPlayer, video:VideoView){
        //retrocede la posicion de la fuente 10000ms
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

    /**
     * Controla la posicion de la barra de posicion
     *
     * @param mp1
     * @param mp2
     * @param video
     */
    private fun controlSound(mp1: MediaPlayer, mp2: MediaPlayer, video: VideoView) {

        //inicializamos la seekbar segun la fuente de datos
        initializeSeekbar(mp1, mp2, videoView)

        //creamos un listener para que la barra reaccione a cambios del usuario
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    //desplazamos la musica al punto que este la barra de progreso
                    if(spSource.selectedItemPosition==0)mp1.seekTo(progress)
                    if(spSource.selectedItemPosition==1)mp2.seekTo(progress)
                    if(spSource.selectedItemPosition==2)video.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /**
     * prepara la barra de reproduccion y modifica el texto segun sea necesario
     *
     * @param mp1
     * @param mp2
     * @param video
     */
    private fun initializeSeekbar(mp1: MediaPlayer, mp2: MediaPlayer, video: VideoView){
        //definimos el maximo segun la fuente
        when (spSource.selectedItemPosition) {
            0 -> {
                seekBar.max =mp1.duration
            }
            1 -> {
                seekBar.max =mp2.duration
            }
            else -> {
                seekBar.max =video.duration
            }
        }

        //actualizamos la barra de progreso y el texto cada 0,1s
        val handler = Handler()
        handler.postDelayed(object :Runnable{
            override fun run() {
                try {
                    when (spSource.selectedItemPosition) {
                        0 -> {
                            seekBar.progress = mp1.currentPosition
                            tvTimer.text = (mp1.currentPosition / 1000).toString()
                        }
                        1 -> {
                            seekBar.progress = mp2.currentPosition
                            tvTimer.text = (mp2.currentPosition/1000).toString()
                        }
                        else -> {
                            seekBar.progress = video.currentPosition
                            tvTimer.text = (video.currentPosition/1000).toString()
                        }
                    }
                    handler.postDelayed(this,100)
                }catch (e:Exception){
                    seekBar.progress=0
                }
            }
        },0)
    }
}