package hr.ferit.soundpoolapp

import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*


//prema stranici 113 iz prirucnika i https://www.youtube.com/watch?v=fIWPSni7kUk
class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var MAX_STREAMS: Int = 10

    private  lateinit var mSoundPool: SoundPool
    private var mLoaded: Boolean = false
    var mSoundMap: HashMap<Int, Int> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpUI()
        loadSounds()
    }

    private fun setUpUI() {
        this.ibZdravkoM.setOnClickListener(this)
        this.ibMarkZ.setOnClickListener(this)
        this.ibTomislavK.setOnClickListener(this)
    }

    private fun loadSounds() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mSoundPool = SoundPool.Builder().setMaxStreams(MAX_STREAMS).build()
        }
        else {
            this.mSoundPool = SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0)
        }

        this.mSoundPool.setOnLoadCompleteListener{_, _, _ -> mLoaded = true}
        this.mSoundMap[R.raw.zdravko] = this.mSoundPool.load(this, R.raw.zdravko, 1)
        this.mSoundMap[R.raw.mark] = this.mSoundPool.load(this, R.raw.mark, 1)
        this.mSoundMap[R.raw.tomislav] = this.mSoundPool.load(this, R.raw.tomislav, 1)
    }

    override fun onClick(v: View?) {
        if (this.mLoaded == false) return
        when (v?.id) { //alt+enter fix
            R.id.ibZdravkoM -> playSound(R.raw.zdravko)
            R.id.ibMarkZ -> playSound(R.raw.mark)
            R.id.ibTomislavK -> playSound(R.raw.tomislav)
        }
    }

    fun playSound(selectedSound: Int) {
        val soundID = this.mSoundMap[selectedSound] ?: 0
        this.mSoundPool.play(soundID, 1f, 1f, 1, 0, 1f)
    }
}