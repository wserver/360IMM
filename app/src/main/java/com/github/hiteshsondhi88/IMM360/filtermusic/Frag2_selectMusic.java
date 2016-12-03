package com.github.hiteshsondhi88.IMM360.filtermusic;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.hiteshsondhi88.IMM360.R;

import java.io.IOException;

public class Frag2_selectMusic extends Fragment{
    private RecyclerView musicSelectView;
    private FilterMusicAdapter adapter;
    private FilterMusicActivity filterMusicActivity;
    private MediaPlayer musicPlayer;
    private View fragmentView;
    private Context mContext;


    public Frag2_selectMusic() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.tab_fragment2, container, false);

        initialize();
        setupView();

        return fragmentView;
    }

    private void initialize(){
        mContext = getActivity().getApplicationContext();

        musicSelectView = (RecyclerView) fragmentView.findViewById(R.id.recyclerView2);
        adapter = new FilterMusicAdapter(mContext, FilterMusicData.musicImage, FilterMusicData.musicName);
        adapter.isMusic = true;
        musicSelectView.setAdapter(adapter);

        int tmp;
        if(adapter.selected_musicItem == 0)
            tmp = 1;
        else
            tmp = adapter.selected_musicItem;

        Uri uri = Uri.parse("android.resource://com.github.hiteshsondhi88.IMM360/raw/bgm" + tmp);
        musicPlayer = MediaPlayer.create(mContext, uri);
    }

    private void setupView(){
        musicSelectView.addOnItemTouchListener(
                new RecyclerItemClickListener(mContext, new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                // TODO Handle item click
                                adapter.selected_musicItem = position;
                                musicSelectView.getAdapter().notifyDataSetChanged();

                                Uri uri = Uri.parse("android.resource://com.github.hiteshsondhi88.IMM360/raw/bgm" + position);
                                filterMusicActivity.sendToFragment4(position);
                                filterMusicActivity.musicID = position;

                                    if (musicPlayer == null) {
                                        musicPlayer = MediaPlayer.create(mContext, uri);
                                    } else {
                                        musicPlayer.reset();
                                        try {
                                            musicPlayer.setDataSource(mContext, uri);
                                            musicPlayer.prepare();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    musicPlay(0);
                            }
                        })
                    );
    }

    public void musicPause(){
        musicPlayer.pause();
    }

    public void musicPlay(int playPosition){
        musicPlayer.seekTo(playPosition);
        if(adapter.selected_musicItem != 0)
            musicPlayer.start();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        filterMusicActivity = (FilterMusicActivity)activity;
    }

    @Override
    public void onPause() {
        super.onStop();
        musicPlayer.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        musicPlayer.release();
    }
}
