package com.github.hiteshsondhi88.IMM360.filtermusic;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.hiteshsondhi88.IMM360.FFmpegConnector;
import com.github.hiteshsondhi88.IMM360.R;
import com.github.hiteshsondhi88.IMM360.player.SphericalVideoPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Frag4_musicApply extends Fragment {
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0x1;

    private String VIDEO_PATH ="";

    private SphericalVideoPlayer videoPlayer;
    private FilterMusicActivity filterMusicActivity;
    private View fragmentView;
    FFmpegConnector ff;

    private SeekBar playBar;
    private TextView playTime;
    private ImageButton playOrStop_btn;

    private boolean VIDEO_ISEND = false;
    public boolean VIDEO_ISPLAYING = false;
    private int durationMax;
    private Context mContext;
    private Window mWindow;
    private Resources mResoruce;

    public Frag4_musicApply() {
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.tab_fragment4, container, false);

        initialize();
        setupView();

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestExternalStoragePermission();

        return fragmentView;
    }

    private void initialize(){
        mContext = getActivity().getApplicationContext();
        mWindow = getActivity().getWindow();
        mResoruce = this.getResources();
        ff = new FFmpegConnector();
        VIDEO_PATH = Environment.getExternalStorageDirectory().getPath() + "/workspace/combine.mp4";

        videoPlayer = (SphericalVideoPlayer) fragmentView.findViewById(R.id.spherical_video_player1);
        videoPlayer.setVideoURIPath(VIDEO_PATH);
        videoPlayer.playWhenReady();
        VIDEO_ISPLAYING = false;

        playOrStop_btn = (ImageButton) fragmentView.findViewById(R.id.play_stop);
        playOrStop_btn.setBackground(mResoruce.getDrawable(R.drawable.playbutton_selector));

        playTime = (TextView) fragmentView.findViewById(R.id.playtime);
        playTime.setVisibility(TextView.VISIBLE);

        playBar = (SeekBar) fragmentView.findViewById(R.id.playbar);
        playBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void setupView(){
        playOrStop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(VIDEO_ISPLAYING == true){
                    videoPlayer.pause();
                    VIDEO_ISPLAYING = false;

                    filterMusicActivity.sendToFragment2(1,videoPlayer.getCurrentPosition());
                    mWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    playOrStop_btn.setBackground(mResoruce.getDrawable(R.drawable.playbutton_selector));
                } else if(VIDEO_ISPLAYING == false){
                    if(VIDEO_ISEND == true){
                        videoPlayer.seekTo(0);}

                    videoPlayer.play();

                    durationMax = videoPlayer.getDuration() - 150;
                    Log.d("sujin","getduration" + videoPlayer.getDuration());
                    playBar.setMax(durationMax);

                    filterMusicActivity.sendToFragment2(0,videoPlayer.getCurrentPosition());
                    mWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    playOrStop_btn.setBackground(getActivity().getResources().getDrawable(R.drawable.pausebutton_selector));

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

                    filterMusicActivity.sendToFragment2(1,videoPlayer.getCurrentPosition());
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
                    filterMusicActivity.sendToFragment2(0,videoPlayer.getCurrentPosition());
                }
            }
        });
    }

    public void playVideo(int position){
        VIDEO_ISPLAYING = false;

        playOrStop_btn.setBackground(mResoruce.getDrawable(R.drawable.pausebutton_selector));

        videoPlayer.setVideoURIPath(VIDEO_PATH);
        videoPlayer.reset();
        videoPlayer.play();
        if(position != 0)
            videoPlayer.mute();
        else if(position == 0)
            videoPlayer.mute_cancel();

        durationMax = videoPlayer.getDuration()-150;
        playBar.setMax(durationMax);

        VIDEO_ISPLAYING = true;
        new MyThread().start();
    }

    private void requestExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                getActivity().getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            init();
        }
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        filterMusicActivity = (FilterMusicActivity)activity;
    }

    @Override
    public void onPause() {
        super.onPause();
        VIDEO_ISPLAYING = false;
        videoPlayer.pause();
        playOrStop_btn.setBackground(mResoruce.getDrawable(R.drawable.playbutton_selector));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        videoPlayer.releaseResources();
    }
}
