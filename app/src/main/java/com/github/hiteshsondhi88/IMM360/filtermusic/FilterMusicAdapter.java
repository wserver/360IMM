package com.github.hiteshsondhi88.IMM360.filtermusic;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.hiteshsondhi88.IMM360.R;

public class FilterMusicAdapter extends RecyclerView.Adapter<FilterMusicAdapter.Holder> {
    public static int selected_filterItem = 0;
    public static int selected_musicItem = 0;

    private int[] images;
    private String[] names;
    private Context context;
    public boolean isMusic = false;

    public FilterMusicAdapter(Context context, int[] images, String[] names) {
        this.context = context;
        this.images = images;
        this.names = names;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.tab_view1, parent, false);
        Holder holder = new Holder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.preview.setImageDrawable(ContextCompat.getDrawable(context, images[position]));
        holder.filter_name.setText(names[position]);

        if(position == selected_filterItem && isMusic == false)
            holder.checking.setVisibility(View.VISIBLE);
        else if(position != selected_filterItem && isMusic == false)
            holder.checking.setVisibility(View.GONE);

        if(position == selected_musicItem && isMusic == true){
            holder.checking.setVisibility(View.VISIBLE);
            setGrayScale(holder.preview);}
        else if(position != selected_musicItem && isMusic == true){
            holder.checking.setVisibility(View.GONE);
            holder.preview.clearColorFilter();}
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView preview;
        ImageView checking;
        TextView filter_name;

        public Holder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            preview = (ImageView) itemView.findViewById(R.id.tab_view_preview);
            checking = (ImageView) itemView.findViewById(R.id.checked);
            filter_name = (TextView) itemView.findViewById(R.id.tab_view_text);
        }
    }

    public void setGrayScale(ImageView v){
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);                        //0이면 grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        v.setColorFilter(cf);
    }
}
