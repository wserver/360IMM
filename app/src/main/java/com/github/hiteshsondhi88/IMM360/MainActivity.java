package com.github.hiteshsondhi88.IMM360;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.osclibrary.HTTP_SERVER_INFO;
import com.github.hiteshsondhi88.IMM360.frame.FrameActivity;
import com.lge.octopus.ConnectionManager;
import com.lge.octopus.OctopusManager;
import com.lge.octopus.tentacles.wifi.client.WifiClient;


public class MainActivity extends AppCompatActivity {

    Button buttonFrame;
    Button buttonConnect;
    Button buttonAlbum;

    Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkFileWritePermission();

        FFmpegConnector.cnt = 0;
        mediaScanning(Environment.getExternalStorageDirectory().getPath() + "/360IMM_/" +  FFmpegConnector.resultUri);

//        mContext = this;
        String IP = new String("192.168.43.1:6624");
        setIPPort(IP);

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mReceiver, getFilter());

        buttonFrame = (Button)findViewById(R.id.main_btn);
        buttonConnect = (Button)findViewById(R.id.connection_btn);
        buttonAlbum = (Button)findViewById(R.id.album_btn);

        buttonFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),FrameActivity.class);
                startActivity(intent);
            }
        });

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext,ConnectionActivity.class);
                startActivity(i);
            }
        });

        buttonAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext,AlbumActivity.class);
                startActivity(i);
            }
        });

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("EXIT")
                .setMessage("어플리케이션을 종료하시겠습니까?")
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);
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

    private IntentFilter getFilter(){
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(WifiClient.ACTION_WIFI_STATE);
        return mFilter;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            int result = intent.getIntExtra(WifiClient.EXTRA_RESULT, WifiClient.RESULT.DISCONNECTED);
            if(WifiClient.ACTION_WIFI_STATE.equals(action)){
                if(result == WifiClient.RESULT.CONNECTED){
                     buttonFrame.setEnabled(true);
                    buttonConnect.setEnabled(false);
                } else {
                     buttonFrame.setEnabled(false);
                    buttonConnect.setEnabled(true);
                }
            }
        }
    };

    private void setIPPort(String ip) {
        String[] temp = ip.split(":");
        HTTP_SERVER_INFO.IP = temp[0];
        if (temp.length == 2) {
            HTTP_SERVER_INFO.PORT = temp[1];
        } else {
            HTTP_SERVER_INFO.PORT = "6624";
        }
    }

    private void disconnectWifi(){
        ConnectionManager mConnectionManager = OctopusManager.getInstance(mContext).getConnectionManager();
        mConnectionManager.disconnect();
    }

    protected void onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    protected void onResume() {
        super.onResume();
        //ConnectionManager mConnectionManager = OctopusManager.getInstance(mContext).getConnectionManager();
        if(checkIsConnectedToDevice()){
              buttonFrame.setEnabled(true);
            buttonConnect.setEnabled(false);
        }else {
              buttonFrame.setEnabled(false);
            buttonConnect.setEnabled(true);
        }
    }
    public boolean checkIsConnectedToDevice() {
        WifiManager wifimanager;
        wifimanager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wifimanager.getConnectionInfo();

        String ssid = info.getSSID();

        if(ssid.contains(".OSC")){
            return true;
        }
        else{
            return false;
        }
    }

    private final int MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private void checkFileWritePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("jino", "Permission for write external storage is granted");
                } else {
                    Log.d("jino", "Permission for write external storage is denied");
                }
        }
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
                    mMediaScanner.disconnect();
                }
            };
            mMediaScanner = new MediaScannerConnection(mContext, mMediaScannerClient);
        }
        mMediaScanner.connect();
    }
}