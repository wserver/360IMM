package com.github.hiteshsondhi88.IMM360;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.osclibrary.HttpAsyncTask;
import com.example.osclibrary.OSCCommandsExecute;
import com.example.osclibrary.OSCCommandsStatus;
import com.example.osclibrary.OSCParameterNameMapper;
import com.github.hiteshsondhi88.IMM360.drag.DragActivity;
import com.github.hiteshsondhi88.IMM360.drag.DragListItem;
import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by osujin on 2016-07-29.
 */
public class CameraActivity extends AppCompatActivity {
    ArrayList<DragListItem> data2;
    ArrayList<Data_Video> data = new ArrayList<>();
    Context mContext;
    boolean keepSendRequest = true;
    ViewGroup btn_goMain;
    TextView frameNumCount;
    private ImageView imagePreview;
    private ArrayList<HashMap<String, String>> mItemInfo;
    private String currentDownloadFile;
    private boolean startDownloading;
    private int posint =0;
    private int filmCount =0;
    private boolean isKeepSendRequest = false;
    private HashMap<Integer,String> map = new HashMap<>();
    private HashMap<Integer,String> map2 = new HashMap<>();


    public ProgressDialog mProgressDialog;


    private static final String START = "camera.startCapture";
    private static final String STOP = "camera.stopCapture";
    private HoloCircularProgressBar mHoloCircularProgressBar;
    private ObjectAnimator mProgressBarAnimator;

    enum recordState {STOP_RECORDING, IS_RECORDING}
    private recordState currentRecordState;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent1 = getIntent();
        data = (ArrayList<Data_Video>) intent1.getSerializableExtra("data"); //get index, time, url

        imagePreview = (ImageView) findViewById(R.id.imageView);
        frameNumCount = (TextView)findViewById(R.id.frameNumCount);
        frameNumCount.setText("0/"+data.size());
        initialize();

        mHoloCircularProgressBar = (HoloCircularProgressBar)findViewById(R.id.holoCircularProgressBar);
        mHoloCircularProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keepSendRequest = false;
                startVideo();
            }
        });

        btn_goMain = (ViewGroup)findViewById(R.id.gomain_btn);
        btn_goMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Go Main")
                        .setMessage("메인화면으로 돌아가시겠습니까?")
                        .setCancelable(true)
                        .setPositiveButton("Home", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                data.clear();
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
        });
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentRecordState != recordState.STOP_RECORDING) {
            changeRecordingStatus(STOP);
        }
        keepSendRequest = false;
    }

    private void initialize(){
        mContext = this;
        setCaptureMode();
        currentRecordState = recordState.STOP_RECORDING;
        getLivePreview();
    }

    //set Capture mode to VIDEO
    private void setCaptureMode() {
        JSONObject setParam = new JSONObject();
        JSONObject optionParam = new JSONObject();

        try {
            setParam.put("captureMode", "video");
            optionParam.put("options", setParam);

            OSCCommandsExecute commandsExecute =
                    new OSCCommandsExecute("camera.setOptions", optionParam);
            commandsExecute.setListener(new HttpAsyncTask.OnHttpListener() {
                @Override
                public void onResponse(OSCReturnType type, Object response) {
                    if(type == OSCReturnType.SUCCESS );
                    else Toast.makeText(mContext, "Camera Error",Toast.LENGTH_SHORT).show();
                }
            });
            commandsExecute.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startVideo() {
        changeRecordingStatus(START);

        if (mHoloCircularProgressBar.getMarkerProgress() != 1)
        {
            animate(mHoloCircularProgressBar,null,1f,(data.get(handlerIndex).time)*1000+2000);
            mHoloCircularProgressBar.setMarkerProgress(1f);
            handler.sendEmptyMessageDelayed(0, (data.get(handlerIndex).time) * 1000+2000);
        }

        else
        {
            animate(mHoloCircularProgressBar,null,0f,(data.get(handlerIndex).time)*1000+2000);
            mHoloCircularProgressBar.setMarkerProgress(0f);
            handler.sendEmptyMessageDelayed(0, (data.get(handlerIndex).time) * 1000+2000);
        }
        mHoloCircularProgressBar.setEnabled(false);

    }
    private int handlerIndex = 0;
    Handler handler = new Handler() {
        public void handleMessage(Message msg){
            handlerIndex++;
            stopVideo();
        }
    };

    Handler handler2 = new Handler() {
        public void handleMessage(Message msg){
            getListFiles(null);
        }
    };

    private void stopVideo() {  // stop video
        keepSendRequest = false;
        changeRecordingStatus(STOP);

        frameNumCount.setText(++filmCount+"/"+data.size());
        mHoloCircularProgressBar.setEnabled(true);
        if(handlerIndex == data.size()){
            isKeepSendRequest = true;
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("Waiting...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
            mProgressDialog.show();
            handler.removeMessages(0);
            Log.d("keepsendRequest",""+ keepSendRequest);
            handler2.sendEmptyMessageDelayed(0,3000);

            //
        }

    }

    private void getLivePreview() {
        OSCCommandsExecute commandsExecute = new OSCCommandsExecute("camera.getLivePreview", null,
                OSCCommandsExecute.CommandType.PREVIEW);
        commandsExecute.setListener(new HttpAsyncTask.OnHttpListener() {
            @Override
            public void onResponse(OSCReturnType type, Object response) {
                if (type == OSCReturnType.SUCCESS) {
                    imagePreview.setImageBitmap((Bitmap) response);
                    if(keepSendRequest && isKeepSendRequest == false ) {
                        getLivePreview();
                    }
                }
                else{
                    // Utils.showTextDialog(mContext,getString(R.string.response), Utils.parseString(response));
                    keepSendRequest = false;
                }
            }
        });
        commandsExecute.execute();
    }

    //changeRecordingStatus according to command
    private void changeRecordingStatus(final String command) {
        OSCCommandsExecute commandsExecute = new OSCCommandsExecute(command, null);
        commandsExecute.setListener(new HttpAsyncTask.OnHttpListener() {
            @Override
            public void onResponse(OSCReturnType type, Object response) {
                if (type == OSCReturnType.SUCCESS) {
                    String state = Utils.getCommandState(response);
                    if (state != null) {
                        if (state.equals(OSCParameterNameMapper.STATE_INPROGRESS)) {
                            String commandId = Utils.getCommandId(response);
                            checkCommandsStatus(commandId);
                            return;
                        }
                        else {
                            setRecordingState(command);
                            if(keepSendRequest == false && isKeepSendRequest == false ){
                                keepSendRequest =true;
                                getLivePreview();
                            }
                        }
                    }
                }
            }
        });
        commandsExecute.execute();

    }

    private void checkCommandsStatus(final String commandId) {
        final OSCCommandsStatus commandsStatus = new OSCCommandsStatus(commandId);
        commandsStatus.setListener(new HttpAsyncTask.OnHttpListener() {
            @Override
            public void onResponse(OSCReturnType type, final Object response) {
                if (type == OSCReturnType.SUCCESS) {
                    String state = Utils.getCommandState((String) response);
                    if (state != null && state.equals(OSCParameterNameMapper.STATE_INPROGRESS)) {
                        checkCommandsStatus(commandId);
                    } else {
                        updateUIBasedOnResponse(response);
                        if(keepSendRequest == false && isKeepSendRequest ==false){
                            keepSendRequest =true;
                            getLivePreview();}
                    }
                } else {
                    Utils.showDialog(mContext, "RESPONSE",
                            Utils.parseString(response));
                }
            }
        });
        commandsStatus.execute();
    }


    private final int entryCount = 1;

    private static final String VIDEO = "video";



    private void getListFiles(String token) {
        JSONObject parameters = new JSONObject();
        try {
            //Set parameter values
            parameters.put(OSCParameterNameMapper.ENTRYCOUNT, entryCount);

            //Set fileType parameter (image or video)
            parameters.put(OSCParameterNameMapper.FILETYPE, VIDEO);

            Log.v("1", "get list token = " + token);
            if (token != null) {
                //Set continuation token if it exists
                parameters.put(OSCParameterNameMapper.CONTINUATION_TOKEN, token);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (parameters != null) {
            OSCCommandsExecute commandsExecute = null;
            commandsExecute = new OSCCommandsExecute("camera.listFiles", parameters);

            if (commandsExecute == null) {
                Log.v("", "ERROR: media type error");
                return;
            }

            commandsExecute.setListener(new HttpAsyncTask.OnHttpListener() {
                @Override
                public void onResponse(HttpAsyncTask.OnHttpListener.OSCReturnType type, Object response) {
                    if (type == OSCReturnType.SUCCESS) {
                        Log.d("updateLisc","updateList");
                        updateList((String) response);
                    } else {
                        Utils.showDialog(mContext, "RESPONSE", Utils.parseString(response));
                    }
                }
            });

            commandsExecute.execute();
        }

    }

    public Handler mhandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            Log.d("error",""+msg.what);
            switch(msg.what)
            {
                case 0:
                    for(int i =0; i < data.size(); i++){
                        data.get(i).thumbnailUri = data2.get(i).getThumbnailUri();
                    }
                    Intent intent = new Intent(getApplicationContext(),DragActivity.class);
                    intent.putExtra("data",data);
                    handler2.removeMessages(0);
                    mProgressDialog.cancel();
                    startActivity(intent);
                    break;
                default :
                    Log.d("Handler error","1");
            }
        }
    };
    private void updateList(String res) {
        String tmp_url;
        String tmp_url_phone;
        Data_Video tmp;
        try {
            JSONObject jObject = new JSONObject(res);

            JSONObject resultData = jObject.getJSONObject(OSCParameterNameMapper.RESULTS);
            JSONArray entries = resultData.getJSONArray(OSCParameterNameMapper.ENTRIES);
            for (int i = 0; i < entries.length(); i++) {
                //Parse file info and save info in hash map
                JSONObject fileInfo = entries.getJSONObject(i);
                HashMap<String, String> info= makeFileInfoMap(fileInfo);
                map.put(posint,info.get("fileUrl"));
                map2.put(posint,info.get("name"));
                Log.d("hashmap",""+""+posint+"name"+map2.get(posint)+"fileUrl"+map.get(posint));
                if(posint < handlerIndex){
                    getFile(posint);
                    tmp_url = map.get(posint);
                    tmp_url_phone = OSCCommandsExecute.getFileLocation()+"/"+map2.get(posint);
                    tmp = new Data_Video(posint,data.get(posint).time,tmp_url,tmp_url_phone);
                    data.set(posint,tmp);
                }
            }

            posint++;

            if (resultData.has(OSCParameterNameMapper.CONTINUATION_TOKEN)&& posint < handlerIndex) {
                //if continuation token exists call the get list files
                //to get remaining list
                String token = resultData.getString(OSCParameterNameMapper.CONTINUATION_TOKEN);
                getListFiles(token);
            } else {
                if(posint == data.size()){
                     data2 = new ArrayList<DragListItem>();

                    for(int i=0; i<data.size();i++){
                        DragListItem tmp2 = new DragListItem();
                        tmp2.setUrlOnPhone(data.get(i).uri_phone);
                        data2.add(tmp2);
                    }

                    FFmpegConnector ff = new FFmpegConnector(this, data2, -1, -1, FFmpegConnector.THUMBNAIL);}


//                while(true){+\
//                    if(ff.isConvert == false) {
//                        if (ff.cnt == data.size()) {
//                            break;
//                        }
//                    }
//
//                    Log.d("thumbnail", "thumb" + String.valueOf(ff.isConvert) + " " + String.valueOf(ff.cnt) + " "+ data.size());-
//                    SystemClock.sleep(1000);
//                }

//                for (int i=0;i<data.size();i++) {
//                    File file = new File(data.get(i).thumbnailUri);
//                    while (!file.exists()) {
//                        Log.d("aa", "loof" + data.get(i).thumbnailUri);

//                SystemClock.sleep(3000);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void getFile(final int position){
        currentDownloadFile = map2.get(position);
        JSONObject parameters = new JSONObject();
        try {
            String url = map.get(position);

            parameters.put(OSCParameterNameMapper.FILEURL, url);

            OSCCommandsExecute commandsExecute;

            //Set the data type for request (image or video)
            //It will set different http request header property

            commandsExecute = new OSCCommandsExecute("camera.getFile", parameters, OSCCommandsExecute.CommandType.VIDEO);

            commandsExecute.setListener(new HttpAsyncTask.OnHttpListener() {
                @Override
                public void onResponse(HttpAsyncTask.OnHttpListener.OSCReturnType type, Object response) {
                    if (type == OSCReturnType.SUCCESS) {
                        //Get binary data from camera and save successfully
                        //Response of getFile is the fileUri of the saved file
                        String name = map2.get(position);
                        handleResponse(name, (String) response);
                    } else {
                        Utils.showDialog(mContext, "RESPONSE", Utils.parseString(response));
                    }
                }
            });
            commandsExecute.execute();
            //Set downloading flag as true
            startDownloading = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


//    private void getThumbnailImage(int position){
//
//        JSONObject parameters = new JSONObject();
//        try {
//            String uri = map.get(position);
//            parameters.put(OSCParameterNameMapper.FILEURL, uri);
//            parameters.put(OSCParameterNameMapper.MAXSIZE, 144);
//            OSCCommandsExecute commandsExecute= new OSCCommandsExecute("camera.listFiles", parameters,
//                    OSCCommandsExecute.CommandType.VIDEO);
//            commandsExecute.setListener(new HttpAsyncTask.OnHttpListener() {
//                @Override
//                public void onResponse(HttpAsyncTask.OnHttpListener.OSCReturnType type, Object response) {
//                    if (type == OSCReturnType.SUCCESS) {
//                        getThumbnailString((String)response);
//                        Log.d("thumbnail", "thumb");
//                    } else {
//                        if(response == null){
//                            Log.d("Respose","null");
//                        }
//                        getThumbnailString((String)response);
//                        Log.d("thumbnail2","thumb");
//                    }
//                }
//            });
//
//            commandsExecute.execute();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

//    private void getThumbnailString(String res) {
//
//        Log.d("thumbnail3", "thumb");
//        try {
//            JSONObject jObject = new JSONObject(res);
//
//            JSONObject resultData = jObject.getJSONObject(OSCParameterNameMapper.RESULTS);
//            JSONArray entries = resultData.getJSONArray(OSCParameterNameMapper.ENTRIES);
//            for (int i = 0; i < entries.length(); i++) {
//                //Parse file info and save info in hash map
//                JSONObject fileInfo = entries.getJSONObject(i);
//                HashMap<String, String> info = makeFileInfoMap(fileInfo);
//                String tmp = info.get("thumbnail");
//                Log.d("thumbnail", tmp);
//            }
//
//        }catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    private void handleResponse(String fileName, String localUri) {
        //Set downloading flag as false
        //Update android gallery
        startDownloading = false;
        currentDownloadFile = "";
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + (String) localUri)));
        Toast.makeText(mContext, fileName + " is saved", Toast.LENGTH_LONG).show();
//        mProgressDialog.cancel();
    }

    private HashMap<String, String> makeFileInfoMap(JSONObject fileInfo) {
        HashMap<String, String> infom = new HashMap<>();

        Iterator it = fileInfo.keys();
        while (it.hasNext()) {
            try {
                String key = (String) it.next();
                Object tempValue = fileInfo.get(key);
                String value = tempValue.toString();
                infom.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return infom;
    }


    private void updateUIBasedOnResponse(Object response) {
        String commandName = Utils.getCommandName(response);
        setRecordingState(commandName);
    }

    private void setRecordingState(String command) {
        //change recording status and UI button
        if (command.equals(START)) {
            currentRecordState = recordState.IS_RECORDING;
        }
        else { //camera._stopRecording
            currentRecordState = recordState.STOP_RECORDING;
        }
    }

    private void animate(final HoloCircularProgressBar progressBar, final Animator.AnimatorListener listener,
                         final float progress, final int duration) {

        mProgressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progress);
        mProgressBarAnimator.setDuration(duration);

        mProgressBarAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationCancel(final Animator animation) {
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                progressBar.setProgress(progress);
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
            }

            @Override
            public void onAnimationStart(final Animator animation) {
            }
        });
        if (listener != null) {
            mProgressBarAnimator.addListener(listener);
        }
        mProgressBarAnimator.reverse();
        mProgressBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                progressBar.setProgress((Float) animation.getAnimatedValue());
            }
        });
        progressBar.setMarkerProgress(progress);
        mProgressBarAnimator.start();
    }
}
//