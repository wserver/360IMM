package com.github.hiteshsondhi88.IMM360.drag;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.hiteshsondhi88.IMM360.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public abstract class DragListAdapter extends BaseAdapter {
    protected ArrayList<DragListItem> dragListItems;
    protected LayoutInflater inflater;
    private Context mContext;

    protected boolean   isChanged         = true;
    protected boolean   mShowItem         = false;
    protected boolean isMoving            = false;
    private boolean   isSameDragDirection   = true;
    private int      mLastFlag         = -1;
    private int      mHeight;
    private int      mDragPosition      = -1;
    protected int      mInvisilePosition   = -1;

    public abstract View initItemView(int position, View convertView, ViewGroup parent);

    public DragListAdapter(Context context) {
        super();
        mContext=context;
        inflater = LayoutInflater.from(context);
        dragListItems = new ArrayList<DragListItem>();
    }

    public void setDatas(ArrayList<DragListItem> t) {
        if (t != null && t.size() >= 0) {
            this.dragListItems.clear();
            this.dragListItems.addAll(t);
            notifyDataSetChanged();
        }
    }

    public void addDatas(ArrayList<DragListItem> t) {
        if (t != null && t.size() >= 0) {
            this.dragListItems.addAll(t);
            notifyDataSetChanged();
        }
    }

    public void showDropItem(boolean showItem) {
        this.mShowItem = showItem;
    }

    public void setInvisiblePosition(int position) {
        mInvisilePosition = position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView erase_button;
        final int this_position1 = position;

        convertView = initItemView(position,convertView,parent);

        if (isChanged) {
            if (position == mInvisilePosition) {
                if (!mShowItem) {
                    convertView.setBackgroundColor(0x0000000000);
                    int vis = View.INVISIBLE;
                    setVisibilityUI(convertView, vis);
                }else{
                    convertView.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
                    int vis = View.VISIBLE;
                    setVisibilityUI(convertView, vis);
                }
            }

            if (mLastFlag != -1) {
                if (mLastFlag == 1) {
                    if (position > mInvisilePosition) {
                        Animation animation;
                        animation = getFromSelfAnimation(0, -mHeight);
                        convertView.startAnimation(animation);
                    }
                }
                else if (mLastFlag == 0) {
                    if (position < mInvisilePosition) {
                        Animation animation;
                        animation = getFromSelfAnimation(0, mHeight);
                        convertView.startAnimation(animation);
                    }
                }
            }
            isChanged = true;
        }

        erase_button = (ImageView)convertView.findViewById(R.id.dragitem_erasebtn);
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
                                if(dragListItems.size() == 1)
                                {
                                    Toast.makeText(mContext,"마지막 영상입니다.",Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    deleteVideoOnPhone(dragListItems.get(this_position1).getUrlOnPhone());
                                    removeItem(this_position1);
                                    for(int i = 0; i< dragListItems.size(); i++) {
                                        dragListItems.get(i).setIndexStr(i+1);
                                    }
                                    notifyDataSetInvalidated();
                                }
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        return convertView;
    }

    private void deleteVideoOnPhone(String url)
    {
        File file = new File(url);
        file.delete();
    }

    private void setVisibilityUI(View convertView, int vis) {
        if (convertView instanceof ViewGroup){
            ViewGroup viewGroup= (ViewGroup) convertView;
            for (int i=0;i<viewGroup.getChildCount();i++){
                View view=viewGroup.getChildAt(i);
                view.setVisibility(vis);
            }
        }
    }

    public void exchange(int startPosition, int endPosition) {
        Object startObject = getItem(startPosition);

        if (startPosition < endPosition) {
            dragListItems.add(endPosition + 1, (DragListItem) startObject);
            dragListItems.remove(startPosition);
        }
        else {
            dragListItems.add(endPosition, (DragListItem) startObject);
            dragListItems.remove(startPosition + 1);
        }
        isChanged = true;
    }

    public void exchangeCopy(int startPosition, int endPosition) {
        Object startObject = getCopyItem(startPosition);

        if (startPosition < endPosition) {
            mCopyList.add(endPosition + 1, (DragListItem) startObject);
            mCopyList.remove(startPosition);
        }
        else {
            mCopyList.add(endPosition, (DragListItem) startObject);
            mCopyList.remove(startPosition + 1);
        }
        isChanged = true;
    }

    private void removeItem(int pos) {
        if (dragListItems != null && dragListItems.size() > pos) {
            dragListItems.remove(pos);
            this.notifyDataSetChanged();
        }
    }

    public Object getCopyItem(int position) {
        return mCopyList.get(position);
    }

    public void addDragItem(int start, Object obj) {
        dragListItems.remove(start);
        dragListItems.add(start, (DragListItem) obj);
    }

    private ArrayList<DragListItem> mCopyList = new ArrayList<DragListItem>();

    public void copyList() {
        mCopyList.clear();
        for (DragListItem str : dragListItems) {
            mCopyList.add(str);
        }
    }

    public void postList() {
        dragListItems.clear();
        for (DragListItem str : mCopyList) {
            dragListItems.add(str);
        }
        if (listener != null) {
            listener.onDragStop(dragListItems);
        }

        for(int i = 0; i< dragListItems.size(); i++) {
            dragListItems.get(i).setIndexStr(i+1);
        }
        notifyDataSetChanged();
    }

    public int getmLastFlag() {
        return mLastFlag;
    }

    public void setIsSameDragDirection(boolean value) {
        isSameDragDirection = value;
    }

    public void setLastFlag(int flag) {
        mLastFlag = flag;
    }

    public void setHeight(int value) {
        mHeight = value;
    }

    public void setCurrentDragPosition(int position) {
        mDragPosition = position;
    }

    private Animation getFromSelfAnimation(int x, int y) {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, x, Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, y);
        translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(100);
        translateAnimation.setInterpolator(new AccelerateInterpolator());
        return translateAnimation;
    }

    onDragStopListener listener;

    public void setOnDragStopListener(onDragStopListener listener) {
        this.listener = listener;
    }

    public interface onDragStopListener<DragListItem> {
        void onDragStop(List<com.github.hiteshsondhi88.IMM360.drag.DragListItem> afterList);
    }

    public List<DragListItem> getDragListItems() {
        return dragListItems;
    }

    @Override
    public int getCount() {
        return dragListItems.size();
    }

    @Override
    public Object getItem(int position) {
        return dragListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public DragListItem getItemObject(int position) {
        return dragListItems.get(position);
    }
}