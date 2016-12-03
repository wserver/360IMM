package com.github.hiteshsondhi88.IMM360;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.IMM360.player.SphericalVideoPlayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SphericalPlayerActivity extends AppCompatActivity {

    //생성한 파일 디렉토리 받아오도록 변경할 것
    private final String SAMPLE_VIDEO_PATH = Environment.getExternalStorageDirectory()+"/DCIM/AccessoryCamera/360 CAM/20160727_211641.mp4";
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0x1;

    private SphericalVideoPlayer videoPlayer;

    private SeekBar playBar;
    private TextView playTime;
    private TextView playDuration;
    private ImageButton playOrStop_btn;

    private boolean VIDEO_ISEND = false;
    public boolean VIDEO_ISPLAYING = false;
    private int durationMax;
    private Window mWindow;
    private Resources mResoruce;

    // SeekBar 막대 움직이는 Thread
    class MyThread extends Thread {
        @Override
        public void run() {
            while(VIDEO_ISPLAYING) {
                playBar.setProgress(videoPlayer.getCurrentPosition());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);
        initialize();
        String uri_path = getIntent().getType();

        videoPlayer = (SphericalVideoPlayer) findViewById(R.id.spherical_video_player);
        videoPlayer.setVideoURIPath(uri_path);
        videoPlayer.playWhenReady();

        VIDEO_ISPLAYING = false;

        playOrStop_btn = (ImageButton)findViewById(R.id.play_stop1);
        playOrStop_btn.setBackground(mResoruce.getDrawable(R.drawable.playbutton_selector));

        playTime = (TextView) findViewById(R.id.playtime1);
        playDuration = (TextView) findViewById(R.id.playduration);

        playBar = (SeekBar)findViewById(R.id.playbar1);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestExternalStoragePermission();

        setupView();
    }

    private void initialize(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mWindow = getWindow();
        mResoruce = this.getResources();
    }

    private void setupView(){
        playOrStop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(VIDEO_ISPLAYING == true){
                    videoPlayer.pause();
                    VIDEO_ISPLAYING = false;

                    mWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    playOrStop_btn.setBackground(mResoruce.getDrawable(R.drawable.playbutton_selector));
                } else if(VIDEO_ISPLAYING == false){
                    if(VIDEO_ISEND == true){
                        videoPlayer.seekTo(0);}

                    videoPlayer.play();

                    durationMax = videoPlayer.getDuration() - 150;
                    playBar.setMax(durationMax);

                    int m = durationMax / 60000;
                    int s = (durationMax % 60000) / 1000;
                    String strTime = String.format("%02d:%02d", m, s);
                    playDuration.setText(strTime);

                    mWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    playOrStop_btn.setBackground(mResoruce.getDrawable(R.drawable.pausebutton_selector));

                    VIDEO_ISPLAYING = true;
                    new MyThread().start();
                }
            }
        });

        playBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    videoPlayer.seekTo(progress);
                }

                if(VIDEO_ISEND == true){
                    VIDEO_ISEND = false;
                }

                if(progress == durationMax) {
                    videoPlayer.pause();
                    VIDEO_ISPLAYING = false;
                    VIDEO_ISEND = true;

                    playOrStop_btn.setBackground(mResoruce.getDrawable(R.drawable.playbutton_selector));
                }

                int m = progress / 60000;
                int s = (progress % 60000) / 1000;
                String strTime = String.format("%02d:%02d", m, s);
                playTime.setText(strTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(VIDEO_ISPLAYING == true){
                    videoPlayer.pause(); }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int adjustedPostion = seekBar.getProgress(); // 사용자가 움직여놓은 위치
                videoPlayer.seekTo(adjustedPostion);

                if(VIDEO_ISPLAYING == true){
                    videoPlayer.play();
                }
            }
        });
    }

    //파일존재여부확인메소드
    //나중에 구현할것
    private boolean isFileExist(File file){
        boolean result;
        if(file!=null&&file.exists()){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    private void requestExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            init();
        }
    }

    public static void toast(Context context, String msg) {
        Toast.makeText(
                context,
                msg,
                Toast.LENGTH_SHORT).show();
    }

    private void init() {
        videoPlayer.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                videoPlayer.initRenderThread(surface, width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                videoPlayer.releaseResources();
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
        videoPlayer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        if (requestCode != PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            return;
        }

        if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            toast(this, "Access not granted for reading video file :(");
            return;
        }

        init();
    }

    public static String readRawTextFile(Context context, int resId) {
        InputStream is = context.getResources().openRawResource(resId);
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader buf = new BufferedReader(reader);
        StringBuilder text = new StringBuilder();
        try {
            String line;
            while ((line = buf.readLine()) != null) {
                text.append(line).append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    @Override
    public void onPause() {
        super.onPause();
        VIDEO_ISPLAYING = false;
        videoPlayer.pause();
        playOrStop_btn.setBackground(mResoruce.getDrawable(R.drawable.playbutton_selector));
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        videoPlayer.releaseResources();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}