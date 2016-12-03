package com.github.hiteshsondhi88.IMM360;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class ProgressActivity extends AppCompatActivity {
    int musicID;
    int filterID;
    FFmpegConnector ff;
    Context mContext;

    public Handler mhandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            switch(msg.what)
            {
                case 0:
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(),"완료",Toast.LENGTH_LONG).show();
                    mediaScanning(Environment.getExternalStorageDirectory().getPath() + "/360IMM_/" +  FFmpegConnector.resultUri);

                    break;
                default :
                    Log.d("Handler error","2");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        mContext = this;

        Intent intent1 = getIntent();
        musicID = intent1.getIntExtra("data",0);
        filterID = intent1.getIntExtra("data2",0);
        Log.d("aa","musicID : " + musicID);
        Log.d("aa","filterID : " + filterID);
        ff = new FFmpegConnector(this, null, musicID, filterID, FFmpegConnector.MERGE);
    }

    public void DeleteDir(String path)
    {
        File file = new File(path);
        File[] childFileList = file.listFiles();
        for(File childFile : childFileList)
        {
            if(childFile.isDirectory())
                DeleteDir(childFile.getAbsolutePath());
            else
                childFile.delete();
        }
        file.delete();
    }

    private String mPath;

    private MediaScannerConnection mMediaScanner;
    private MediaScannerConnection.MediaScannerConnectionClient mMediaScannerClient;

    public void mediaScanning(final String path) {
        mPath = path;
        Log.d("aa","mpath : " + mPath);

        if (mMediaScanner == null) {
            mMediaScannerClient = new MediaScannerConnection.MediaScannerConnectionClient() {

                @Override
                public void onMediaScannerConnected() {
                    mMediaScanner.scanFile(mPath, null ); // 디렉토리
                    // 가져옴
                }

                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Log.d("aa","ScanComplete");
//                    DeleteDir(Environment.getExternalStorageDirectory().getPath()+"/workspace");
                    mMediaScanner.disconnect();
                }
            };
            mMediaScanner = new MediaScannerConnection(mContext, mMediaScannerClient);
        }
        mMediaScanner.connect();
    }

    @Override
    public void onBackPressed() {
    }
}
