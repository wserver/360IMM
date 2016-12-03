package com.github.hiteshsondhi88.IMM360.filtermusic;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.hiteshsondhi88.IMM360.R;

import java.io.File;

public class Frag3_filterApply extends Fragment {

    ImageView appliedImage;

    public Frag3_filterApply() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_fragment3, container, false);

        appliedImage = (ImageView) v.findViewById(R.id.filter_view);

        File file = new File(Environment.getExternalStorageDirectory().getPath() +
                "/workspace/filterResult" + (FilterMusicActivity.filterID) + ".png");
        Uri uri = Uri.fromFile(file);
        changeImage(uri);

        return v;
    }

    public void changeImage(Uri uri) {
        appliedImage.setImageURI(uri);
    }
}