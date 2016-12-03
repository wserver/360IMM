package com.github.hiteshsondhi88.IMM360;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.github.hiteshsondhi88.IMM360.drag.DragActivity;
import com.github.hiteshsondhi88.IMM360.drag.DragListItem;
import com.github.hiteshsondhi88.IMM360.filtermusic.FilterMusicActivity;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.ObjectGraph;

public class FFmpegConnector extends Activity{
    public static final int THUMBNAIL = 1001;
    public static final int MERGE = 1002;
    public static final int COMBINE = 1003;
    public static final int FILTER = 1004;

    String dcimDirectory;
    public static String resultUri = "";
    public static float time = 0;
    public static boolean isConvert = false;
    public static int  cnt=0;
    public int data_size =0;
    public static int mergeiter = 0;

    Calendar oCalendar;

    private int[] musicID = {R.raw.bgm1, R.raw.bgm2, R.raw.bgm3, R.raw.bgm4, R.raw.bgm5, R.raw.bgm6,
            R.raw.bgm7, R.raw.bgm8, R.raw.bgm9, R.raw.bgm10, R.raw.bgm11};
    private String[] filterValue = {"0.5:-0.2:0.5:4.4:3.9:8.3:7.7:0.1", "1.0:0.0:1.2:0.2:7.0:3.7:5.2:0.5",
            "1.0:0.0:0.0:0.0:0.0:0.0:0.0:0.0", "1.0:-0.1:1.2:1.2:1.7:2.6:8.8:0.4",
            "1.0:0.2:0.9:1.2:7.8:5.5:0.7:0.2", "0.5:0.1:1.2:1.9:1.2:2.4:3.6:0.3",
            "1.0:-0.1:1.5:1.2:1.7:5.6:8.8:0.4"};
    public static String filterThumbUri;
    private static final String TAG = FFmpegConnector.class.getSimpleName();
    private String cmd = "";

    @Inject
    FFmpeg ffmpeg;
    Context cont;

    public static int flag;

    private ProgressDialog progressDialog;
    public FFmpegConnector(){

    }
    public FFmpegConnector(Context context, ArrayList<DragListItem> data, int mpos, int fpos, int flag){

        this.flag = flag;

        cont = context;
        ButterKnife.inject(this);
        ObjectGraph.create(new DaggerDependencyModule(cont)).inject(this);
        loadFFMpegBinary();

        File dir = new File(Environment.getExternalStorageDirectory().getPath(), "workspace");
        if(!dir.exists()){
            dir.mkdir();
        }

        File dir2 = new File(Environment.getExternalStorageDirectory().getPath(), "360IMM_");
        if(!dir2.exists()){
            dir2.mkdir();
        }

        progressDialog = new ProgressDialog(cont);
        progressDialog.setTitle(null);

        if(flag == THUMBNAIL){
            oCalendar = Calendar.getInstance();
            String year = String.valueOf(oCalendar.get(Calendar.YEAR));
            String month = String.valueOf((oCalendar.get(Calendar.MONTH) + 1));
            String day = String.valueOf(oCalendar.get(Calendar.DAY_OF_MONTH));
            String hour = String.valueOf(oCalendar.get(Calendar.HOUR_OF_DAY));
            String min = String.valueOf(oCalendar.get(Calendar.MINUTE));
            String sec = String.valueOf(oCalendar.get(Calendar.SECOND));
            resultUri = year + month + day +"_"+ hour + min + sec + ".mp4";
        }

        switch(flag){

            case THUMBNAIL:
                Log.d("aa","data size : " + data.size());
                for(int i=0;i<data.size();i++){
                    data_size = data.size();
                    cmd = "-i " + data.get(i).getUrlOnPhone();
                    cmd += " -y -vframes 1 -ss 00:00:01 -an -vcodec png -f rawvideo -s 512*512 -threads 5 -preset ultrafast ";

                    String[] tmp = cmd.split("/");
                    String tmp2 = tmp[tmp.length-1].split("\\.")[0];
                    String thumbUri = Environment.getExternalStorageDirectory().getPath() + "/workspace/" + tmp2 + ".png";
                    cmd += thumbUri;
                    if(i == 0){
                        filterThumbUri = thumbUri;
                    }

                    data.get(i).setThumbnailUri(thumbUri);

                    String[] command = cmd.split(" ");
                    if (command.length > 1){
                        execFFmpegBinary(command);
                    }
                    cmd = "";
                }
                break;
            case MERGE:
                if(mpos != 0){
                    if(fpos != 0){
                        mergeiter = 2;
                        bgm("/workspace/combine.mp4", "/workspace/"+resultUri, time, mpos);
                        filter("/workspace/"+resultUri, "/360IMM_/"+resultUri, fpos);
                    }else{
                        mergeiter = 1;
                        bgm("/workspace/combine.mp4", "/360IMM_/"+resultUri, time, mpos);
                    }
                }else{
                    if(fpos != 0){
                        mergeiter = 1;
                        filter("/workspace/combine.mp4", "/360IMM_/"+resultUri, fpos);
                    }else{
                        mergeiter = 1;
                        cmd = "-i " +Environment.getExternalStorageDirectory().getPath() +
                                "/workspace/combine.mp4 -filter_complex concat=n=1:v=1:a=1 -f MP4 -vn -threads 5 -preset ultrafast -y "+
                                Environment.getExternalStorageDirectory().getPath()+ "/360IMM_/"+resultUri;

                        String[] command = cmd.split(" ");
                        if (command.length > 1){
                            execFFmpegBinary(command);
                        }
                    }
                }
                break;
//                if(mpos != 0){
//                    if(fpos != 0){
//                        mergeiter = 2;
//                        bgm("/workspace/combine.mp4", "/workspace/"+resultUri, time, mpos);
//                        filter("/workspace/"+resultUri, "/360IMM_/"+resultUri, fpos);
//                    }else{
//                        mergeiter = 1;
//                        bgm("/workspace/combine.mp4", "/360IMM_/"+resultUri, time, mpos);
//                    }
//                }else{
//                    if(fpos != 0){
//                        mergeiter = 1;
//                        filter("/workspace/combine.mp4", "/360IMM_/"+resultUri, fpos);
//                    }else{
//                        mergeiter = 1;
//                        cmd = "-i +" +Environment.getExternalStorageDirectory().getPath() +
//                                "/workspace/combine.mp4 -filter_complex concat=n=1:v=1:a=1 -f MP4 -vn -threads 5 -preset ultrafast -y "+
//                                Environment.getExternalStorageDirectory().getPath()+ "/360IMM_/"+resultUri;
//
//                        String[] command = cmd.split(" ");
//                        if (command.length > 1){
//                            execFFmpegBinary(command);
//                        }
//                    }
//                }
//                break;
            case COMBINE:
                // 영상합치기 : -i 1.mp4 -i 2.mp4 -filter_complex concat=n=3:v=1:a=1 -f MP4 -vn -threads 5 -preset ultrafast -y result.mp4
                cmd = "";
                for(int i =0;i<data.size();i++){
                    cmd += "-i " + data.get(i).getUrlOnPhone() + " ";
                }

                String size = String.valueOf(data.size());
                Log.d("aa","영상갯수 : " + size);
                cmd += "-filter_complex concat=n="+size+":v=1:a=1 -f MP4 -vn -threads 5 -preset ultrafast -y " +
                        Environment.getExternalStorageDirectory().getPath() + "/workspace/combine.mp4";

                String[] command = cmd.split(" ");
                if (command.length > 1){
                    execFFmpegBinary(command);
                }
                break;
            case FILTER:
                Log.d("aa","filterUri : " + filterThumbUri);
                File file = new File(filterThumbUri);
                cnt =0;

                if(file.exists()){
                    String copyFile = Environment.getExternalStorageDirectory().getPath()+ "/workspace/filterResult0.png";
                    copyFile(file,copyFile);
                } else
                    Log.d("error","filterThumbUri not exist");

                for (int j = 0; j < 7; j++) {
                    cmd = "-i " + filterThumbUri +
                            " -vf eq=" + filterValue[j] + " -y -preset ultrafast -threads 5 " +
                            Environment.getExternalStorageDirectory().getPath() + "/workspace/filterResult" + (j+1) + ".png";

                    String[] command1 = cmd.split(" ");
                    if (command1.length > 1) {
                        execFFmpegBinary(command1);
                    }
                }
                break;
            default:
                Toast.makeText(cont, "flagError", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void filter(String src, String dst, int fpos){
        cmd = "-i " + Environment.getExternalStorageDirectory().getPath() + src +
                " -vf eq=" + filterValue[fpos-1] + " -y -preset ultrafast -threads 5 " +
                Environment.getExternalStorageDirectory().getPath() + dst;

        String[] command = cmd.split(" ");
        if (command.length > 1) {
            execFFmpegBinary(command);
        } else {
            //Toast.makeText(cont, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show();
        }

    }
    private void bgm(String src, String dst, float time, int mpos){
        getStringFromResource(cont, musicID[mpos-1]);
        //음악길이 자르기 -t 시간 -i 음악 -acodec copy  -f MP4 -threads 5 -preset ultrafast -y 결과파일
        cmd = "-i " + Environment.getExternalStorageDirectory().getPath() + "/workspace/bgm.mp3 -t " + time + " -acodec copy -f MP3 -threads 5 -preset ultrafast -y " +
                Environment.getExternalStorageDirectory().getPath() + "/workspace/bgmcrop.mp3";
        String[] command = cmd.split(" ");
        if (command.length > 1) {
            execFFmpegBinary(command);
        } else {
            //Toast.makeText(cont, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show();
        }

        cmd = "-i " + Environment.getExternalStorageDirectory().getPath() + src + " -i " +
                Environment.getExternalStorageDirectory().getPath() + "/workspace/bgmcrop.mp3" +
                " -c:v copy -c:a aac -strict experimental -map 0:v:0 -map 1:a:0 -f MP4 -threads 5 -preset ultrafast -y " +
                Environment.getExternalStorageDirectory().getPath() + dst;

        command = cmd.split(" ");
        if (command.length > 1) {
            execFFmpegBinary(command);
        } else {
            //Toast.makeText(cont, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show();
        }

    }
    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
//                    showUnsupportedExceptionDialog();
                }
            });
        } catch (FFmpegNotSupportedException e) {
//            showUnsupportedExceptionDialog();
        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Toast.makeText(cont, "FAILED with output : "+s, Toast.LENGTH_LONG).show();
                    Log.d("aa", "error" + s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.d(TAG, "Success command : ffmpeg "+command);
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "progress command : ffmpeg "+command);
                    isConvert = true;
//                    progressDialog.setMessage("Processing\n"+s);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    isConvert = true;
//                    progressDialog.setMessage("Processing...");
//                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg "+command);
//                    isConvert = false;
                    Log.d("aa","CNT : " + cnt);
                    if(flag == THUMBNAIL)
                    {
                        Log.d("aa","CNT1 : " + cnt);
                        if(cnt == data_size-1) {
                            ((CameraActivity) cont).mhandler.sendEmptyMessage(0);
                        }
                        else if(cnt < data_size){
                            cnt ++;
                        }
                        else{

                        }
                    }
                    else if(flag == COMBINE)
                    {
                        ((DragActivity)cont).mhandler.sendEmptyMessage(0);
                    }
                    else if(flag == FILTER){
                        cnt ++;
                        if(cnt ==7)
                            ((FilterMusicActivity)cont).mhandler.sendEmptyMessage(0);
                    }
                    else if(flag == MERGE){
                        mergeiter --;
                        if(mergeiter == 0){
                            ((ProgressActivity)cont).mhandler.sendEmptyMessage(0);
                        }
                    }

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

//    public void DeleteDir(String path)
//    {
//        File file = new File(path);
//        File[] childFileList = file.listFiles();
//        for(File childFile : childFileList)
//        {
//            if(childFile.isDirectory())
//                DeleteDir(childFile.getAbsolutePath());
//            else
//                childFile.delete();
//        }
//        file.delete();
//    }

    private boolean copyFile(File file , String save_file){
        boolean result;
        if(file!=null&&file.exists()){
            try {
                FileInputStream fis = new FileInputStream(file);
                FileOutputStream newfos = new FileOutputStream(save_file);
                int readcount=0;
                byte[] buffer = new byte[1024];
                while((readcount = fis.read(buffer,0,1024))!= -1){
                    newfos.write(buffer,0,readcount);
                }
                newfos.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            result = true;
        }else{
            result = false;
        }
        return result;
    }

    public static void getStringFromResource(Context context, int id) {
        BufferedReader reader = null;

        try {
            StringBuilder stringBuilder = new StringBuilder();
            InputStream inputStream = context.getResources().openRawResource(id);

            File file = new File(Environment.getExternalStorageDirectory().getPath()+"/workspace/", "bgm.mp3");
            FileOutputStream fOut = new FileOutputStream(file);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] buf = new byte[2014];
            int nread;

            while((nread = inputStream.read(buf))!=-1){
                buffer.write(buf, 0, nread);
            }
            byte[] bytes = buffer.toByteArray();

            fOut.write(bytes);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e(TAG, "Error opening raw resource: " + id);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing raw resource: " + id, e);
                }
            }
        }
    }
}