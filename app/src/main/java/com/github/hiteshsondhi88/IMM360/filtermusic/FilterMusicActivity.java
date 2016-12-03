package com.github.hiteshsondhi88.IMM360.filtermusic;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.hiteshsondhi88.IMM360.FFmpegConnector;
import com.github.hiteshsondhi88.IMM360.MainActivity;
import com.github.hiteshsondhi88.IMM360.ProgressActivity;
import com.github.hiteshsondhi88.IMM360.R;

import java.io.File;

public class FilterMusicActivity extends AppCompatActivity {

    private final String TAG1 = "1";
    private final String TAG2 = "2";
    public static int musicID = 0;
    public static int filterID = 0;

    private Button filter_btn;
    private Button music_btn;
    private ViewGroup next_btn;

    private boolean MUSICBTN_ISCLICKED = false;

    private Fragment fragment1;
    private Fragment fragment2;
    private Fragment fragment3;
    private Fragment fragment4;

    private Frag1_selectFilter ref_frag1;
    private Frag2_selectMusic ref_frag2;
    private Frag3_filterApply ref_frag3;
    private Frag4_musicApply ref_frag4;

    public Handler mhandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            switch(msg.what)
            {
                case 0:
                    switchFragment(2);
                    next_btn.setEnabled(true);
                    break;
                default :
                    Log.d("Handler error","FilterMusicActivity Handler error");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        initialize();
        setupView();
        applyFilter();
    }

    private void initialize(){
        filter_btn = (Button) findViewById(R.id.filter_btn);
        music_btn = (Button) findViewById(R.id.music_btn);
        next_btn = (ViewGroup) findViewById(R.id.next4_layout);

        next_btn.setEnabled(false);

        fragment1 = new Frag1_selectFilter();
        fragment2 = new Frag2_selectMusic();
        fragment3 = new Frag3_filterApply();
        fragment4 = new Frag4_musicApply();
    }

    private void setupView(){
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProgressActivity.class);
                intent.putExtra("data",musicID);
                intent.putExtra("data2",filterID);
                startActivity(intent);
            }
        });

        filter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_btn.setTextColor(Color.parseColor("#ffffffff"));
                music_btn.setTextColor(Color.parseColor("#ff777777"));
                switchFragment(1);
            }
        });

        music_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                music_btn.setTextColor(Color.parseColor("#ffffffff"));
                filter_btn.setTextColor(Color.parseColor("#ff777777"));
                switchFragment(2);
            }
        });
    }

    public void switchFragment(int index) {
        Fragment frag1;
        Fragment frag2;
        String tag1;
        String tag2;

        if (index == 1) {
            frag1 = fragment1;
            frag2 = fragment3;
            tag2 = TAG2;
            tag1 = TAG1;
            MUSICBTN_ISCLICKED = false;
        } else{
            frag1 = fragment2;
            frag2 = fragment4;
            tag2 = TAG1;
            tag1 = TAG2;
            MUSICBTN_ISCLICKED = true;
        }

        FragmentManager fm1 = getFragmentManager();
        FragmentTransaction fragmentTransaction1 = fm1.beginTransaction();
        fragmentTransaction1.replace(R.id.frame1or2, frag1 , tag1);
        fragmentTransaction1.commit();

        FragmentManager fm2 = getFragmentManager();
        FragmentTransaction fragmentTransaction2 = fm2.beginTransaction();
        fragmentTransaction2.replace(R.id.frame3or4, frag2, tag2);
        fragmentTransaction2.commit();

        if(MUSICBTN_ISCLICKED == true){
            fm2.executePendingTransactions();
            ref_frag4 = (Frag4_musicApply) fm2.findFragmentByTag(TAG1);
            fm1.executePendingTransactions();
            ref_frag2 = (Frag2_selectMusic) fm1.findFragmentByTag(TAG2);
        }else{
            fm2.executePendingTransactions();
            ref_frag3 = (Frag3_filterApply) fm2.findFragmentByTag(TAG2);
            fm1.executePendingTransactions();
            ref_frag1 = (Frag1_selectFilter) fm1.findFragmentByTag(TAG1);
        }
    }

    public void sendToFragment1(){

    }
    public void sendToFragment2(int option, int playPosition) {
        if (option == 0) {
            ref_frag2.musicPlay(playPosition);
        } else if (option == 1) {
            ref_frag2.musicPause();
        }
    }

    public void sendToFragment3(int selectedFilter){
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/workspace/filterResult"+selectedFilter+".png");
        Uri uri = Uri.fromFile(file);
        ref_frag3.changeImage(uri);
    }

    public void sendToFragment4(int position){
        ref_frag4.playVideo(position);
    }

    public void applyFilter(){
        FFmpegConnector ff = new FFmpegConnector(this, null, -1, -1, FFmpegConnector.FILTER);
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("EXIT")
                .setMessage("메인화면으로 돌아가시겠습니까?")
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){

                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_bg));
        dialog.show();
    }

}
