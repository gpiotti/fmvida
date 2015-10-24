package com.fmvida;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.content.Context;
import android.content.Intent;

import android.media.AudioManager;


import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    //Audiomanager
    AudioManager audioManager;

    //Buttons y UI
    ImageButton reconectButton;
    ImageButton powerButton;
    ImageButton muteButton;
    ImageButton playStopButton;
    SeekBar volumeControl;
    TextView marquesina;
    Animation reconnectBlink;


    public static final String STATUS_PLAYING = "ez.streaming.action.STATUS_PLAYING";
    public static final String STATUS_STOP = "ez.streaming.action.STATUS_STOP";
    public static final String STATUS_CONNECTING = "ez.streaming.action.STATUS_CONNECTING";
    public static final String STATUS_LOST_STREAM = "ez.streaming.action.SATUS_LOST_STREAM";

    public static final String ACTION_START = "ez.streaming.action.ACTION_START";
    public static final String ACTION_STOP = "ez.streaming.action.ACTION_STOP";

    public static final String VOLUME_CHANGE = "ez.streaming.action.VOLUME_CHANGE";
    public static final String MUTE = "ez.streaming.action.MUTE";
    public static final String UNMUTE = "ez.streaming.action.UNMUTE";

    public static final String FACEBOOK_URL = "https://www.facebook.com/juancruz.rubino.12";
    public static final String TWITTER_URL = "https://twitter.com/fmvida1035";

    private String player_status = STATUS_STOP;
    boolean muted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("sendMessage"));

        Log.i("MyActivity", "Activity On Create ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reconectButton = (ImageButton) findViewById(R.id.reconnecting);
        reconnectBlink = AnimationUtils.loadAnimation(this, R.anim.reconect_blink);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        powerButton = (ImageButton) findViewById(R.id.power);
        muteButton = (ImageButton) findViewById(R.id.mMuteButton);
        muteButton.setEnabled(false);
        muteButton.setAlpha(64);

        playStopButton = (ImageButton) findViewById(R.id.playStopButton);
        volumeControl = (SeekBar) findViewById(R.id.volBar);
        volumeControl.setEnabled(false);
        volumeControl.setMax(maxVolume);
        Log.i("MyActivity", "Max Volumen : " + maxVolume);
        volumeControl.setProgress((int)Math.round( maxVolume * 0.6));
        marquesina = (TextView) findViewById(R.id.programa);

        marquesina.setSelected(true);
        marquesina.setVisibility(View.INVISIBLE);

            playStopButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    if (player_status == STATUS_PLAYING) {
                        player_status = STATUS_STOP;
                        startService(new Intent(getApplicationContext(), Music_service.class).setAction(ACTION_STOP));
                        muted=false;
                        muteButton.setImageResource(R.drawable.ic_volume_up_black_36dp);
                        playStopButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                    } else if (player_status == STATUS_STOP) {
                        startService(new Intent(getApplicationContext(), Music_service.class).setAction(ACTION_START).putExtra("vol", calcularVolumen()));
                        Log.i("MyActivity", "Arrancando con volumen : "+ calcularVolumen());
                        playStopButton.setImageResource(R.drawable.ic_stop_white_48dp);
                        player_status = STATUS_PLAYING;
                    }
                }
            });

        muteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (muted) {
                    Log.i("MyActivity", "Desmuteado ");
                    muted = false;
                    volumeControl.setEnabled(true);
                    Intent serviceIntent = new Intent(getApplicationContext(), Music_service.class);
                    serviceIntent.setAction(UNMUTE);
                    serviceIntent.putExtra("lastVolume", calcularVolumen());
                    startService(serviceIntent);
                    muteButton.setImageResource(R.drawable.ic_volume_up_black_36dp);
                } else {
                    Log.i("MyActivity", "Muteado ");
                    muted = true;
                    volumeControl.setEnabled(false);
                    startService(new Intent(getApplicationContext(), Music_service.class).setAction(MainActivity.MUTE));
                    muteButton.setImageResource(R.drawable.ic_volume_off_black_36dp);
                }

            }
        });

        powerButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                startService(new Intent(getApplicationContext(), Music_service.class).setAction(ACTION_STOP));
                finish();
            }
        });


        volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    if (!muted) {
                        Log.i("MyActivity", "Progress " + progress);
                        Intent serviceIntent = new Intent(getApplicationContext(), Music_service.class);
                        serviceIntent.setAction(VOLUME_CHANGE);
                        serviceIntent.putExtra("vol", calcularVolumen());
                        startService(serviceIntent);
                    }
            }
        });

        ImageView imgFacebook = (ImageView)findViewById(R.id.facebook);
        imgFacebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(FACEBOOK_URL));
                startActivity(intent);
            }
        });

        ImageView imgTwitter = (ImageView)findViewById(R.id.twitter);
        imgTwitter.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(TWITTER_URL));
                startActivity(intent);
            }
        });

        if (savedInstanceState != null) {
            Log.i("MyActivity", "Activity On Saveed ");
            // Restore value of members from saved state
            player_status = savedInstanceState.getString("player_status");
            muted = savedInstanceState.getBoolean("mute_button_state");
            if (player_status.equals(STATUS_PLAYING) || player_status.equals(STATUS_CONNECTING)){
                player_status = STATUS_PLAYING;
                volumeControl.setEnabled(true);
                muteButton.setAlpha(255);
                muteButton.setEnabled(true);
                playStopButton.setImageResource(R.drawable.ic_stop_white_48dp);
            }
            if (muted){
                muteButton.setImageResource(R.drawable.ic_volume_off_black_36dp);
            }
            else{
                muteButton.setImageResource(R.drawable.ic_volume_up_black_36dp);
            }
            volumeControl.setEnabled(savedInstanceState.getBoolean("volume_bar_state"));
        }
    }

    private Float calcularVolumen(){
        int maxVolume = 16;
        float log1 = (float) (Math.log(maxVolume - volumeControl.getProgress()) / Math.log(maxVolume));
        return 1-log1;
    }
    private void updateUI(String command){
        switch(command) {
            case  STATUS_CONNECTING:
                reconectButton.setVisibility(View.VISIBLE);
                reconectButton.startAnimation(reconnectBlink);
                muteButton.setEnabled(false);
                muteButton.setAlpha(64);
                volumeControl.setEnabled(false);

                break;
            case  STATUS_PLAYING:
                reconectButton.clearAnimation();
                reconectButton.setVisibility(View.INVISIBLE);
                muteButton.setEnabled(true);
                muteButton.setAlpha(255);
                volumeControl.setEnabled(true);

                break;
            case STATUS_LOST_STREAM:
                reconectButton.setVisibility(View.VISIBLE);
                reconectButton.startAnimation(reconnectBlink);
                muteButton.setEnabled(false);
                muteButton.setAlpha(64);
                volumeControl.setEnabled(false);
                player_status=STATUS_STOP;

                break;
            case STATUS_STOP:
                Log.i("MyActivity", "UPDATE UI STOP ");
                reconectButton.clearAnimation();
                reconectButton.setVisibility(View.INVISIBLE);
                muteButton.setEnabled(false);
                muteButton.setAlpha(64);
                player_status = STATUS_STOP;
                playStopButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                volumeControl.setEnabled(false);

                break;
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch(intent.getStringExtra("command")) {
                case STATUS_CONNECTING:   updateUI(STATUS_CONNECTING);
                    Log.i("MyActivity", "STATUS_CONNECTING ");
                break;
                case STATUS_PLAYING:   updateUI(STATUS_PLAYING);
                    Log.i("MyActivity", "STATUS_PLAYING ");
                break;
                case STATUS_STOP:   updateUI(STATUS_STOP);
                    Log.i("MyActivity", "STATUS_STOP ");
                    break;
            }
        }
    };

    @Override

    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current  state
        savedInstanceState.putString("player_status", player_status);
        savedInstanceState.putBoolean("mute_button_state", muted);
        savedInstanceState.putBoolean("volume_bar_state", volumeControl.isEnabled());

        Log.i("MyActivity", "saveInstanceState ");
        // Always call the superclass
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override

    protected void onStop() {
        Log.i("MyActivity", "Activity On Stop ");
        super.onStop();
        if (player_status == STATUS_STOP){
            stopService(new Intent(getApplicationContext(), Music_service.class));
        }




    }
    protected void onRestart() {
        super.onRestart();
        Log.i("MyActivity", "Activity On Restart ");
    }
    protected void onDestroy() {
        super.onDestroy();  // Always call the superclass method first
        Log.i("MyActivity", "Activity On Destroy ");

        stopService(new Intent(getApplicationContext(), Music_service.class));
        finish();


    }
}


