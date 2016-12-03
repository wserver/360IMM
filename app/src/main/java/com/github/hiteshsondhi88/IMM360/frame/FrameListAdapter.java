package com.github.hiteshsondhi88.IMM360.frame;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.github.hiteshsondhi88.IMM360.R;

import java.util.ArrayList;
import java.util.List;

public class FrameListAdapter extends BaseAdapter {

	private Context mContext;

	private List<FrameListItem> mItems = new ArrayList<FrameListItem>();

	public FrameListAdapter(Context context) {
		mContext = context;
	}

	public void addItem(FrameListItem it) {
		mItems.add(it);
	}
	public void modifyItem(int location, FrameListItem it) {
		mItems.set(location, it);
	}

	public void removeItem(int position)
	{
		mItems.remove(position);
	}
	public void setListItems(List<FrameListItem> lit) {
		mItems = lit;
	}

	public int getCount() {
		return mItems.size();
	}

	public Object getItem(int position) {
		return mItems.get(position);
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	public boolean isSelectable(int position) {
		try {
			return mItems.get(position).isSelectable();
		} catch (IndexOutOfBoundsException ex) {
			return false;
		}
	}

	public long getItemId(int position) {
		return position;
	}
	public int getItemIdx(int position)
	{
		return position;
	}

	public View getView(int position, final View convertView, ViewGroup parent) {
		final FrameListView itemView;
		ImageView erase_button;
		final int this_position = position;

		if (convertView == null) {
			itemView = new FrameListView(mContext, mItems.get(position));
		} else {
			itemView = (FrameListView) convertView;
			itemView.setIcon(mItems.get(position).getIcon());
			itemView.setText(0, ""+mItems.get(position).getData(0));
			itemView.setText(1, ""+mItems.get(position).getData(1)+"초");
		}

		erase_button = (ImageView)itemView.findViewById(R.id.erase_btn);
		erase_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("Erase Check")
						.setMessage("동영상을 삭제하시겠습니까?")
						.setCancelable(true)
						.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								removeItem(this_position);
								for(int i=0; i<mItems.size(); i++) {
									mItems.get(i).setIndex(i+1);
								}
								notifyDataSetInvalidated();
							}
						})
						.setNegativeButton("취소", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});

				AlertDialog dialog = builder.create();
				dialog.getWindow().setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.dialog_bg));
				dialog.show();
			}
		});
		return itemView;
	}



}