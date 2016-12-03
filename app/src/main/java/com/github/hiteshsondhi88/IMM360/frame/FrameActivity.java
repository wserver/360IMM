package com.github.hiteshsondhi88.IMM360.frame;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.hiteshsondhi88.IMM360.CameraActivity;
import com.github.hiteshsondhi88.IMM360.Data_Video;
import com.github.hiteshsondhi88.IMM360.R;

import java.util.ArrayList;

public class FrameActivity extends AppCompatActivity {
    static ListView view_framelist;
    static FrameListAdapter adapter;
    static int frameTimeIndex = 0;
    final int frameTimeInt[] = {3,5,7};
    final String frameTimeStr[] = {"3초","5초","7초"};
    ViewGroup btn_next1;
    ViewGroup btn_plus1;
    ViewGroup btn_prev1;

    ArrayList<Data_Video> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);

        // 리스트뷰 객체 참조
        view_framelist = (ListView) findViewById(R.id.frameList);
        view_framelist.requestFocusFromTouch();
        // 어댑터 객체 생성
        adapter = new FrameListAdapter(this);
        view_framelist.setAdapter(adapter);


        btn_plus1 = (ViewGroup)findViewById(R.id.plus_layout);

        btn_plus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = selectTime();
                dialog.setCanceledOnTouchOutside(true);
                dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_bg));
                dialog.show();
            }
        }) ;


        //ListItem 클릭 이벤트
        view_framelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog dialog = changeTime(position);
                dialog.setCanceledOnTouchOutside(true);
                dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_bg));
                dialog.show();
            }
        });

        btn_next1 = (ViewGroup)findViewById(R.id.next_layout);

        btn_next1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i =0; i < adapter.getCount();i++){
                    Data_Video tmp;
                    FrameListItem item;
                    item = (FrameListItem)adapter.getItem(i);
                    tmp = new Data_Video(item.getData(0),item.getData(1),item.toString(),null);
                    data.add(tmp);


                }

                if(adapter.getCount() == 0)
                {
                    Toast.makeText(getApplicationContext(),"영상을 추가하세요",Toast.LENGTH_LONG).show();

                }
                else
                {
                    Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
                    intent.putExtra("data",data);
                    startActivity(intent);
                }
            }
        });


        btn_prev1 = (ViewGroup) findViewById(R.id.pre_layout);

        btn_prev1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private AlertDialog selectTime() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setSingleChoiceItems(frameTimeStr, frameTimeIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                frameTimeIndex = which;
            }
        });


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int frameIndex = adapter.getCount() + 1;
                Resources res = getResources();
                adapter.addItem(new FrameListItem(res.getDrawable(R.drawable.delete),frameIndex,frameTimeInt[frameTimeIndex]));
                view_framelist.setSelection(adapter.getCount()-1);
                frameTimeIndex = 0;
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                frameTimeIndex = 0;
            }
        });

        AlertDialog dialog = builder.create();
        return dialog;

    }



    public AlertDialog changeTime(int position) {
        final int this_position = position;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        builder.setSingleChoiceItems(frameTimeStr, frameTimeIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                frameTimeIndex = which;
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                FrameListItem curItem = (FrameListItem)adapter.getItem(this_position);
//                String[] curData = curItem.getData();
                Resources res = getResources();
                adapter.modifyItem(this_position, new FrameListItem(res.getDrawable(R.drawable.delete),adapter.getItemIdx(this_position) + 1,frameTimeInt[frameTimeIndex]));
                adapter.notifyDataSetChanged();
                frameTimeIndex = 0;
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                frameTimeIndex = 0;
            }
        });
        AlertDialog dialog = builder.create();
        return dialog;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            return true;
        } else if(id == R.id.action_delete){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}