package com.github.hiteshsondhi88.IMM360.filtermusic;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.hiteshsondhi88.IMM360.R;

public class Frag1_selectFilter extends Fragment {

    private RecyclerView filterSelectView;
    private FilterMusicActivity filterMusicActivity;
    private FilterMusicAdapter adapter;
    private View v;

    public Frag1_selectFilter() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.tab_fragment1, container, false);

        initialize();
        setupView();

        return v;
    }

    private void initialize(){
        filterSelectView = (RecyclerView) v.findViewById(R.id.recyclerView1);
        adapter = new FilterMusicAdapter(getActivity(), FilterMusicData.filterImage, FilterMusicData.filterName);
        filterSelectView.setAdapter(adapter);
    }

    private void setupView(){
        filterSelectView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity().getApplicationContext(),
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                // TODO Handle item click
                                adapter.selected_filterItem = position;
                                filterSelectView.getAdapter().notifyDataSetChanged();
                                filterMusicActivity.filterID = position;
                                filterMusicActivity.sendToFragment3(position);

                            }
                        })
        );
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        filterMusicActivity = (FilterMusicActivity)activity;
    }
}
