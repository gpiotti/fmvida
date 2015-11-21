package com.fmvida;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    //Audiomanager
    AudioManager audioManager;

    //Buttons y UI

    ImageButton powerButton;
    ImageButton muteButton;
    ImageButton playStopButton;
    Button showButton;
    SeekBar volumeControl;
    Animation reconnectBlink;
    TextView connectingText;
    AnimationDrawable  frameAnimation;


    public static final String STATUS_PLAYING = "com.fmvida..action.STATUS_PLAYING";
    public static final String STATUS_STOP = "com.fmvida.action.STATUS_STOP";
    public static final String STATUS_STOPPING = "com.fmvida.action.STATUS_STOPPING";
    public static final String STATUS_CONNECTING = "com.fmvida.action.STATUS_CONNECTING";
    public static final String STATUS_LOST_STREAM = "com.fmvida.action.SATUS_LOST_STREAM";

    public static final String ACTION_START = "com.fmvida.action.ACTION_START";
    public static final String ACTION_STOP = "com.fmvida.action.ACTION_STOP";

    public static final String VOLUME_CHANGE = "com.fmvida.action.VOLUME_CHANGE";
    public static final String MUTE = "com.fmvida.action.MUTE";
    public static final String UNMUTE = "com.fmvida.action.UNMUTE";

    public static final String FACEBOOK_URL = "https://www.facebook.com/juancruz.rubino.12";
    public static final String FACEBOOK_ID = "fb://profile/100005868240617";
    public static final String TWITTER_URL = "https://twitter.com/fmvida1035";
    public static final String TWITTER_ID = "twitter://user?user_id=1395651954";
    public static final String INSTAGRAM_URL = "https://instagram.com/Fmvida103.5";
    public static final String INSTAGRAM_ID = "http://instagram.com/_u/Fmvida103.5";




    public static final String WHATSAPP_NUMBER = "+5492281572830";

    private String player_status = STATUS_STOP;
    boolean muted = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MyActivity", "Activity On Create ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Load the ImageView that will host the animation and
        // set its background to our AnimationDrawable XML resource.
        ImageView img = (ImageView)findViewById(R.id.logo);

        //img.setAdjustViewBounds(true);
        //img.setScaleType(ImageView.ScaleType.FIT_XY);
        img.setImageDrawable(getResources().getDrawable(R.drawable.anim_late));

        this.frameAnimation = (AnimationDrawable) img.getDrawable();
        this.frameAnimation.stop();





        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("sendMessage"));



        //reconectButton = (ImageButton) findViewById(R.id.reconnecting);
        connectingText = (TextView) findViewById(R.id.connectingText);
        reconnectBlink = AnimationUtils.loadAnimation(this, R.anim.reconect_blink);


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        powerButton = (ImageButton) findViewById(R.id.power);
        muteButton = (ImageButton) findViewById(R.id.mMuteButton);
        muteButton.setEnabled(false);
        muteButton.setAlpha(64);

        playStopButton = (ImageButton) findViewById(R.id.playStopButton);
        volumeControl = (SeekBar) findViewById(R.id.volBar);
        volumeControl.setEnabled(false);
        volumeControl.setMax(maxVolume);
        Log.i("MyActivity", "Max Volumen : " + maxVolume);
        volumeControl.setProgress((int) Math.round(maxVolume * 0.6));
        playStopButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i("MyActivity", "player_status = " + player_status);
                if (player_status.equals(STATUS_PLAYING)) {
                    player_status = STATUS_STOP;
                    startService(new Intent(getApplicationContext(), Music_service.class).setAction(ACTION_STOP));
                    muted = false;
                    muteButton.setImageResource(R.drawable.ic_volume_up_black_36dp);
                    playStopButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                } else if (player_status.equals(STATUS_STOP)) {
                    startService(new Intent(getApplicationContext(), Music_service.class).setAction(ACTION_START).putExtra("vol", calcularVolumen()));
                    Log.i("MyActivity", "Arrancando con volumen : " + calcularVolumen());
                    playStopButton.setImageResource(R.drawable.ic_stop_white_48dp);
                    player_status = STATUS_PLAYING;
                }
                else{
                    Log.i("MyActivity", "entro en el else " + player_status);
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
                if (player_status == STATUS_PLAYING || player_status == STATUS_CONNECTING) {
                    startService(new Intent(getApplicationContext(), Music_service.class).setAction(ACTION_STOP));
                    finish();
                } else {
                    finish();
                }

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

                PackageManager pm = getPackageManager();
                try {
                    Intent faceIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK_ID));
                    pm.getPackageInfo("com.facebook.katana", PackageManager.GET_META_DATA);
                    startActivity(faceIntent);

                } catch (Exception e) {
                    Intent faceIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK_URL));
                    startActivity(faceIntent);
                }



            }
        });

        ImageView imgTwitter = (ImageView)findViewById(R.id.twitter);
        imgTwitter.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                PackageManager pm=getPackageManager();
                try {
                    // get the Twitter app if possible
                    pm.getPackageInfo("com.twitter.android", 0);
                    Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TWITTER_ID));
                    startActivity(twitterIntent);
                } catch (Exception e) {
                    // no Twitter app, revert to browser
                    Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TWITTER_URL));
                    startActivity(twitterIntent);
                }



            }
        });

        ImageView imgWhatsapp = (ImageView)findViewById(R.id.whatsapp);
        imgWhatsapp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri uri = Uri.parse("smsto:" + WHATSAPP_NUMBER);

                PackageManager pm=getPackageManager();
                try {
                    Intent waIntent = new Intent(Intent.ACTION_SENDTO, uri);
                    pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                    waIntent.setPackage("com.whatsapp");
                    startActivity(waIntent);
                } catch (PackageManager.NameNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "WhatsApp no instalado", Toast.LENGTH_SHORT)
                            .show();
                }


            }
        });


        ImageView imgInstagram = (ImageView)findViewById(R.id.instagram);
        imgInstagram.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                PackageManager pm=getPackageManager();
                try {
                    // get the Twitter app if possible
                    pm.getPackageInfo("com.instagram.android", 0);
                    Intent instagramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_ID));
                    startActivity(instagramIntent);
                } catch (Exception e) {
                    // no Twitter app, revert to browser
                    Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL));
                    startActivity(twitterIntent);
                }



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

    protected void onStart(){
        super.onStart();

        Log.i("MyActivity", "onStart ");
    }

    private Float calcularVolumen(){
        int maxVolume = 16;
        float log1 = (float) (Math.log(maxVolume - volumeControl.getProgress()) / Math.log(maxVolume));
        return 1-log1;
    }
    private void updateUI(String command){
        switch(command) {
            case  STATUS_CONNECTING:
                Log.i("MyActivity", "UPDATE UI CONNECTINGH ");
                connectingText.setVisibility(View.VISIBLE);
                connectingText.startAnimation(reconnectBlink);
                playStopButton.setEnabled(false);
                playStopButton.setAlpha(64);
                muteButton.setEnabled(false);
                muteButton.setAlpha(64);
                volumeControl.setEnabled(false);

                break;
            case  STATUS_STOPPING:
                Log.i("MyActivity", "UPDATE UI STOPPINg ");
                connectingText.setVisibility(View.INVISIBLE);
                playStopButton.setEnabled(false);
                playStopButton.setAlpha(64);
                muteButton.setEnabled(false);
                muteButton.setAlpha(64);
                volumeControl.setEnabled(false);

                frameAnimation.stop();
                frameAnimation.selectDrawable(0);


                break;
            case  STATUS_PLAYING:
                Log.i("MyActivity", "UPDATE UI PLAYING ");
                connectingText.clearAnimation();
                connectingText.setVisibility(View.INVISIBLE);
                muteButton.setEnabled(true);
                muteButton.setAlpha(255);
                volumeControl.setEnabled(true);
                playStopButton.setEnabled(true);
                playStopButton.setAlpha(255);

                frameAnimation.start();

                break;
            case STATUS_LOST_STREAM:
                Log.i("MyActivity", "UPDATE UI LOST STREAM ");
                connectingText.setVisibility(View.VISIBLE);
                connectingText.startAnimation(reconnectBlink);
                muteButton.setEnabled(false);
                muteButton.setAlpha(64);
                volumeControl.setEnabled(false);
                player_status=STATUS_STOP;

                frameAnimation.stop();
                frameAnimation.selectDrawable(0);

                break;
            case STATUS_STOP:
                Log.i("MyActivity", "UPDATE UI STOP ");
                connectingText.clearAnimation();
                connectingText.setVisibility(View.INVISIBLE);
                muteButton.setEnabled(false);
                muteButton.setAlpha(64);
                player_status = STATUS_STOP;
                playStopButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                volumeControl.setEnabled(false);
                playStopButton.setEnabled(true);
                playStopButton.setAlpha(255);

                frameAnimation.stop();
                frameAnimation.selectDrawable(0);
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
                case STATUS_STOPPING:   updateUI(STATUS_STOPPING);
                    Log.i("MyActivity", "STATUS_STOPPING ");
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

        //Stop the analytics tracking
      //  GoogleAnalytics.getInstance(this).reportActivityStop(this);



    }

    public void showProg(View view) {
        Intent intent = new Intent(this, progActivity.class);
        startActivity(intent);
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


