package com.example.a7minworkoutapp

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_exercise.*
import kotlinx.android.synthetic.main.dialog_custom_back.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var restTimer: CountDownTimer? =
            null // Variable for Rest Timer and later on we will initialize it.
    private var restProgress =
            0 // Variable for timer progress. As initial the rest progress is set to 0. As we are about to start.

    private var exerciseTimer: CountDownTimer? = null // Variable for Exercise Timer and later on we will initialize it.
    private var exerciseProgress = 0 // Variable for exercise timer progress. As initial the exercise progress is set to 0. As we are about to start.

    private var exerciseList: ArrayList<ExerciseModel>? = null // We will initialize the list later.
    private var currentExercisePosition = -1 // Current Position of Exercise.

    private var tts:TextToSpeech?= null

    private var player:MediaPlayer?= null

    private var exerciseAdapter :ExerciseStatusAdapter ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)

        setSupportActionBar(toolbar_exercise_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Navigate the activity on click on back button of action bar.
        toolbar_exercise_activity.setNavigationOnClickListener {
            customDialogForBAckButton()
        }

        tts = TextToSpeech(this,this)


        exerciseList = Constants.defaultExerciseList()
        setupExerciseSatusRecyclerView()

        setupRestView() // REST View is set in this function


    }

    /**
     * Here is Destroy function we will reset the rest timer to initial if it is running.
     */
    public override fun onDestroy() {
        if (restTimer != null) {
            restTimer!!.cancel()
            restProgress = 0
        }

        if (exerciseTimer != null) {
            exerciseTimer!!.cancel()
            exerciseProgress = 0
        }

        if(tts!=null){
            tts!!.stop()
            tts!!.shutdown()
        }

        if(player!=null){
            player!!.stop()
        }
        super.onDestroy()
    }

    /**
     * Function is used to set the timer for REST.
     */
    private fun setupRestView() {
        try {
            player = MediaPlayer.create(applicationContext,R.raw.press_start)
            player!!.isLooping = false
            player!!.start()

        }catch (e:Exception){
            e.printStackTrace()

        }


        // Here according to the view make it visible as this is Rest View so rest view is visible and exercise view is not.
        llRestView.visibility = View.VISIBLE
        llExerciseView.visibility = View.GONE

        /**
         * Here firstly we will check if the timer is running the and it is not null then cancel the running timer and start the new one.
         * And set the progress to initial which is 0.
         */
        if (restTimer != null) {
            restTimer!!.cancel()
            restProgress = 0
        }

        // TODO (Step 2 - Setting the upcoming exercise name in the UI element.)
        // START
        // Here we have set the upcoming exercise name to the text view
        // Here as the current position is -1 by default so to selected from the list it should be 0 so we have increased it by +1.
        tvUpcomingExerciseName.text = exerciseList!![currentExercisePosition + 1].getName()
        // ENDc

        // This function is used to set the progress details.
        setRestProgressBar()
    }

    /**
     * Function is used to set the progress of timer using the progress
     */
    private fun setRestProgressBar() {

        progress_bar.progress = restProgress // Sets the current progress to the specified value.

        /**
         * @param millisInFuture The number of millis in the future from the call
         *   to {#start()} until the countdown is done and {#onFinish()}
         *   is called.
         * @param countDownInterval The interval along the way to receive
         *   {#onTick(long)} callbacks.
         */
        // Here we have started a timer of 10 seconds so the 10000 is milliseconds is 10 seconds and the countdown interval is 1 second so it 1000.
        restTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                restProgress++ // It is increased to ascending order
                progress_bar.progress = 10 - restProgress // Indicates progress bar progress
                tvTimer.text =
                        (10 - restProgress).toString()  // Current progress is set to text view in terms of seconds.
            }

            override fun onFinish() {
                currentExercisePosition++

                exerciseList!![currentExercisePosition].setIsSelected(true)
                exerciseAdapter!!.notifyDataSetChanged()

                setupExerciseView()
            }
        }.start()
    }

    /**
     * Function is used to set the progress of timer using the progress for Exercise View.
     */
    private fun setupExerciseView() {

        // Here according to the view make it visible as this is Exercise View so exercise view is visible and rest view is not.
        llRestView.visibility = View.GONE
        llExerciseView.visibility = View.VISIBLE

        /**
         * Here firstly we will check if the timer is running the and it is not null then cancel the running timer and start the new one.
         * And set the progress to initial which is 0.
         */
        if (exerciseTimer != null) {
            exerciseTimer!!.cancel()
            exerciseProgress = 0
        }

        speakOut(exerciseList!![currentExercisePosition].getName())
        setExerciseProgressBar()

        /**
         * Here current exercise name and image is set to exercise view.
         */

        Glide.with(this).load(exerciseList!![currentExercisePosition].getImage()).into(ivImage)
//        ivImage.setImageResource(exerciseList!![currentExercisePosition].getImage())
        tvExerciseName.text = exerciseList!![currentExercisePosition].getName()


    }

    /**
     * Function is used to set the progress of timer using the progress for Exercise View for 30 Seconds
     */
    private fun setExerciseProgressBar() {

        progressBarExercise.progress = exerciseProgress

        exerciseTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                exerciseProgress++
                progressBarExercise.progress = 30 - exerciseProgress
                ExerciseTvTimer.text = (30 - exerciseProgress).toString()
            }

            override fun onFinish() {
                if (currentExercisePosition < 11) {
                    exerciseList!![currentExercisePosition].setIsSelected(false)
                    exerciseList!![currentExercisePosition].setIsCompleted(true)
                    exerciseAdapter!!.notifyDataSetChanged()

                    setupRestView()

                } else {
                    finish()
                    val intent = Intent(this@ExerciseActivity,FinishActivity::class.java)
                    startActivity(intent)


                }
            }
        }.start()
    }

    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS){
            val result = tts!!.setLanguage(Locale.US)
            if(result == TextToSpeech.LANG_MISSING_DATA || result
            == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS","The Language is not supported")
            }
        }else{
            Log.e("TTS","Initialization failed")
        }
    }

    private fun speakOut(text:String){
        tts!!.speak(text,TextToSpeech.QUEUE_FLUSH,null,"")
    }

    private fun setupExerciseSatusRecyclerView(){
        rvExercseStatus.layoutManager = LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL,
        false)

        exerciseAdapter = ExerciseStatusAdapter(exerciseList!!,this)
        rvExercseStatus.adapter = exerciseAdapter
    }

    private fun customDialogForBAckButton(){
        val customDialog = Dialog(this)
        customDialog.setContentView(R.layout.dialog_custom_back)
        customDialog.tvYes.setOnClickListener {
            finish()
            customDialog.dismiss()
        }
        customDialog.tvNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()


    }
}