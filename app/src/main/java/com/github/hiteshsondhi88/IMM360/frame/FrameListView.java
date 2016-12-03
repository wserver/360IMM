package com.github.hiteshsondhi88.IMM360.frame;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.hiteshsondhi88.IMM360.R;

/**
 * 아이템으로 보여줄 뷰 정의
 *
 * @author Mike
 *
 */
public class FrameListView extends LinearLayout {

	private ImageView mIcon;

	private TextView mText01;

	private TextView mText02;

	public FrameListView(Context context, FrameListItem aItem) {
		super(context);

		// Layout Inflation
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.item_framelist, this, true);

		// Set Icon
		mIcon = (ImageView) findViewById(R.id.erase_btn);
		mIcon.setImageDrawable(aItem.getIcon());

		// Set Text 01
		mText01 = (TextView) findViewById(R.id.index_frame);
		mText01.setText(""+aItem.getData(0));

		// Set Text 02
		mText02 = (TextView) findViewById(R.id.time_frame);
		mText02.setText(""+aItem.getData(1)+"초");

	}

	public void setText(int index, String data) {
		if (index == 0) {
			mText01.setText(data);
		} else if (index == 1) {
			mText02.setText(data);
		} else {
			throw new IllegalArgumentException();
		}
	}
	public void setIcon(Drawable icon) {
		mIcon.setImageDrawable(icon);
	}

}