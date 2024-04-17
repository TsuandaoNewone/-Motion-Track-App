package com.example.motiontrackapp;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    final int speed = 10;
    int secondsPass = 0;
    ConstraintLayout layout;
    SeekBar seekBar;
    TextView textView,textView_record;
    Button button_record,button_play,button_playback;
    RadioGroup radioGroup;
    ArrayList<Point> pointsList;
    Timer timer_capture,timer_time;
    boolean on_record = false;
    boolean on_play = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //initialize widgets
        layout = findViewById(R.id.main);
        textView = findViewById(R.id.textview);
        textView_record = findViewById(R.id.textview_record);
        button_record = findViewById(R.id.button_record);
        button_play = findViewById(R.id.button_play);
        button_playback = findViewById(R.id.button_playback);
        radioGroup = findViewById(R.id.radiogroup_speed);
        seekBar = findViewById(R.id.seekBar);
        pointsList = new ArrayList<>();

        //set onTouchListener
        textView.setOnTouchListener(new OnTouchEvent());

        button_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //on play, reject record
                if(on_play){
                    Toast.makeText(getApplicationContext(),
                            "Please wait for play to stop",Toast.LENGTH_SHORT).show();
                    return;
                }
                //start recoding
                if(!on_record){
                    pointsList.clear();
                    button_record.setText("end");
                    textView_record.setText("recording");
                    //set motion-capture timer
                    timer_capture = new Timer();
                    timer_capture.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            pointsList.add(new Point(textView.getX(),textView.getY()));
                        }
                    },0,speed);
                    //set time count timer
                    timer_time = new Timer();
                    timer_time.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            secondsPass++;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // renew time display
                                    String timeString = String.format("%02d:%02d", secondsPass / 60, secondsPass % 60);
                                    textView_record.setText(timeString);
                                }
                            });
                        }
                    },0,1000);
                    on_record = !on_record;
                }
                //stop recording
                else{
                    button_record.setText("record");
                    textView_record.setText("not recording");
                    //clear timer
                    if(timer_capture !=null){
                        timer_capture.cancel();
                        timer_capture.purge();
                        timer_capture = null;
                    }
                    if(timer_time !=null){
                        timer_time .cancel();
                        timer_time .purge();
                        timer_time  = null;
                    }
                    secondsPass = 0;
                    on_record = !on_record;
                }
            }
        });
        //play button function
        button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(false);
            }
        });
        //play back button function
        button_playback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(true);
            }
        });
    }

    private class OnTouchEvent implements View.OnTouchListener{
        //update textView position, but not recording positions
        @Override
        public boolean onTouch(View view, MotionEvent e){
            //.d(TAG,);
            System.out.println("eventAction:"+e.getAction());
            float xDown = 0,yDown = 0;
            switch (e.getAction()){
                case MotionEvent.ACTION_DOWN:
                    xDown = e.getX();
                    yDown = e.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float xMove = e.getX();
                    float yMove = e.getY();
                    textView.setX(textView.getX() + (xMove - xDown)-textView.getWidth()/2);
                    textView.setY(textView.getY() + (yMove - yDown)-textView.getHeight()/2);
            }
            if(on_record){
                pointsList.add(new Point(textView.getX(),textView.getY()));
            }
            return true;
        }
    }
    public void play(boolean play_back){
        float playspeed;
        int id = radioGroup.getCheckedRadioButtonId();
        if(id == R.id.half_speed){
            playspeed = 0.5f;
        } else if (id == R.id.normal_speed) {
            playspeed = 1f;
        }else{
            playspeed = 2f;
        }
        //on play, reject play
        if(on_play){
            Toast.makeText(getApplicationContext(),
                    "please wait for playing to end",Toast.LENGTH_SHORT).show();
            return;
        }
        //on recording, reject play
        if(on_record){
            Toast.makeText(getApplicationContext(),
                    "Please stop recording",Toast.LENGTH_SHORT).show();
            return;
        }
        //not recorded, reject play
        if(pointsList.isEmpty()){
            Toast.makeText(getApplicationContext(),
                    "Please start recording",Toast.LENGTH_SHORT).show();
            return;
        }
        // start play
        else{
            System.out.println("Array Size："+pointsList.size());
            seekBar.setMax(pointsList.size());
            on_play = true;
            timer_capture = new Timer();
            timer_capture.schedule(new TimerTask() {
                int start = play_back?pointsList.size()-1:0;
                int end = play_back? 0: pointsList.size()-1;
                int next = play_back? -1: 1;
                int progress = 0;
                @Override
                public void run() {
                    float x = pointsList.get(start).x;
                    float y = pointsList.get(start).y;
                    textView.setX(x);
                    textView.setY(y);
                    start += next;
                    seekBar.setProgress(progress++);
                    if(start == end){
                        timer_capture.cancel();
                        timer_capture.purge();
                        timer_capture = null;
                        on_play = false;
                        seekBar.setProgress(0);
                    }
                }
            },0,(int)((float)speed/playspeed));//control speed
        }
    }
    class Point{
        public float x,y;
        public Point(float x, float y){
            this.x = x;
            this.y = y;
        }
    }
}