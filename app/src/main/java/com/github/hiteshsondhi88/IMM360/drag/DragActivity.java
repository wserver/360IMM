package com.github.hiteshsondhi88.IMM360.drag;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.osclibrary.OSCCommandsExecute;
import com.example.osclibrary.OSCParameterNameMapper;
import com.github.hiteshsondhi88.IMM360.Data_Video;
import com.github.hiteshsondhi88.IMM360.FFmpegConnector;
import com.github.hiteshsondhi88.IMM360.MainActivity;
import com.github.hiteshsondhi88.IMM360.R;
import com.github.hiteshsondhi88.IMM360.filtermusic.FilterMusicActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DragActivity extends AppCompatActivity {

    private DragListView dragList;
    private ViewGroup next_btn;
    private ArrayList<Data_Video> data = new ArrayList<>();
    private FFmpegConnector ff = new FFmpegConnector();
    private Context mContext;

    public ProgressDialog mProgressDialog;
    public Handler mhandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch(msg.what)
            {
                case 0:
                    Intent intent = new Intent(getApplicationContext(),FilterMusicActivity.class);
                    intent.putExtra("data",data);
                    mProgressDialog.cancel();
                    startActivity(intent);
                    break;
                default :
                    Log.d("Handler error","DragActivity handler error");
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draglist);

        initialize();
        setupView();
        deleteVideoOnCamera();
    }

    private void initialize(){
        mContext = this;

        Intent intent = getIntent();
        data = (ArrayList<Data_Video>) intent.getSerializableExtra("data");

        dragList = (DragListView) findViewById(R.id.draglist);
        dragList.requestFocusFromTouch();

        next_btn = (ViewGroup)findViewById(R.id.next3_layout);
    }

    private void setupView(){
        ArrayList<DragListItem> itemList=new ArrayList<>();

        for (int i=0; i<data.size(); i++){
            DragListItem item = new DragListItem();

            item.setIndexStr((data.get(i).index)+1);
            item.setTimeStr(data.get(i).time);
            item.setUrlOnPhone(data.get((data.size()-1)-i).uri_phone);
            item.setThumbnailUri(data.get((data.size()-1)-i).thumbnailUri);
            item.setTrashDraw(ContextCompat.getDrawable(this, R.drawable.delete));
            itemList.add(item);
        }

        final MyAdapter adapter=new MyAdapter(this);
        adapter.setDatas(itemList);
        dragList.setAdapter(adapter);
        dragList.setSelection(0);

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setMessage("Waiting...");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
                mProgressDialog.show();

                ff = new FFmpegConnector(mContext,adapter.dragListItems, -1, -1,FFmpegConnector.COMBINE);
            }
        });
    }

    class MyAdapter extends DragListAdapter {
        LayoutInflater inflater;

        public MyAdapter(Context context) {
            super(context);
            inflater=LayoutInflater.from(context);
        }

        @Override
        public View initItemView(int position, View convertView, ViewGroup parent) {
            View itemView = inflater.inflate(R.layout.item_draglist, null);

            TextView videoIndex= (TextView) itemView.findViewById(R.id.dragitem_index);
            TextView videoTime= (TextView) itemView.findViewById(R.id.dragitem_time);
            ImageView videoThumnail= (ImageView) itemView.findViewById(R.id.dragitem_preview);
            ImageView erase_btn= (ImageView) itemView.findViewById(R.id.dragitem_erasebtn);

            videoIndex.setText("" + dragListItems.get(position).getIndexStr());
            videoTime.setText("" + dragListItems.get(position).getTimeStr() + "초");
            videoThumnail.setImageURI(dragListItems.get(position).getPreviewDraw());
            erase_btn.setImageDrawable(dragListItems.get(position).getTrashDraw());

            return itemView;
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    private void deleteVideoOnCamera()
    {
        JSONObject parameters = new JSONObject();
        try {
            JSONArray urlsParameter = new JSONArray();
            for(int i=0; i<data.size(); i++) {
                urlsParameter.put(data.get(i).uri); //현재 파일의 url을 받아서 넣어줌
                parameters.put(OSCParameterNameMapper.FILEURLS, urlsParameter);
                final OSCCommandsExecute cmdExecute = new OSCCommandsExecute("camera.delete", parameters);
                cmdExecute.execute();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}