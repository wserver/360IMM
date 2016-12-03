package com.github.hiteshsondhi88.IMM360.drag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.github.hiteshsondhi88.IMM360.R;

public class DragListView extends ListView {
    private int                     mStartPosition;
    private int                     mDragPosition;
    private int                     mLastPosition;
    private int                     mDragPoint;
    private int                     mDragOffset;
    private int                     mUpScrollBounce;
    private int                     mDownScrollBounce;
    private final static int         mStep               = 1;
    private int                     mCurrentStep;
    private int                     mItemVerticalSpacing   = 0;
    private int                     mHoldPosition;
    private boolean                  isLock;
    private boolean                  isMoving            = false;
    private boolean                  bHasGetSapcing         = false;

    public static final int            MSG_DRAG_STOP         = 0x1001;
    public static final int            MSG_DRAG_MOVE         = 0x1002;
    private static final int         ANIMATION_DURATION      = 200;

    private ImageView mDragImageView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams   mWindowParams;

    private Context mContext;

    public DragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext=context;
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        init();
    }

    private void init() {
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    private void getSpacing() {
        bHasGetSapcing = true;

        mUpScrollBounce = getHeight() / 3;
        mDownScrollBounce = getHeight() * 2 / 3;

        int[] firstTempLocation = new int[2];
        int[] secondTempLocation = new int[2];

        ViewGroup firstItemView = (ViewGroup) getChildAt(0);
        ViewGroup secondItemView = (ViewGroup) getChildAt(1);

        if (firstItemView != null) {
            firstItemView.getLocationOnScreen(firstTempLocation);
        }
        else {
            return;
        }

        if (secondItemView != null) {
            secondItemView.getLocationOnScreen(secondTempLocation);
            mItemVerticalSpacing = Math.abs(secondTempLocation[1] - firstTempLocation[1]);
        }
        else {
            return;
        }
    }

    private void initLongClick(float myDragOffset, float mx, float my) {
        // long click
        if (!isLock && !isMoving) {


            int x = (int) mx;
            int y = (int) my;

            mLastPosition = mStartPosition = mDragPosition = pointToPosition(x, y);
            if (mDragPosition <= lockItems) {
                return;
            }

            if (mDragPosition == AdapterView.INVALID_POSITION) {
                return;
            }
            if (false == bHasGetSapcing) {
                getSpacing();
            }

            ViewGroup dragger = (ViewGroup) getChildAt(mDragPosition - getFirstVisiblePosition());
            DragListAdapter adapter = getDragAdapter();
            adapter.isMoving = true;

            mDragPoint = y - dragger.getTop();
            mDragOffset = (int) myDragOffset;
            dragger.destroyDrawingCache();
            dragger.setDrawingCacheEnabled(true);
            dragger.setBackgroundDrawable(getResources().getDrawable(R.drawable.dragitem_bg));
            Bitmap bm = Bitmap.createBitmap(dragger.getDrawingCache(true));
            hideDropItem();
            adapter.setInvisiblePosition(mStartPosition);
            adapter.notifyDataSetChanged();
            startDrag(bm, y);
            isMoving = true;
            adapter.copyList();
        }
    }

    private DragListAdapter getDragAdapter() {
        DragListAdapter adapter;
        if (getAdapter() instanceof HeaderViewListAdapter) {
            HeaderViewListAdapter hAdapter = (HeaderViewListAdapter) getAdapter();
            adapter = (DragListAdapter) hAdapter.getWrappedAdapter();
        }
        else {
            adapter = (DragListAdapter) getAdapter();
        }
        return adapter;
    }

    private boolean setOnLongClickListener(final float myDragOffset, final float mx, final float my) {
        this.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                initLongClick(myDragOffset, mx, my);
                return false;
            }
        });
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                boolean re=setOnLongClickListener(ev.getRawY() - ev.getY(), ev.getX(), ev.getY());
                return re;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mDragImageView != null && mDragPosition != INVALID_POSITION && !isLock) {
            int action = ev.getAction();
            switch (action) {

                case MotionEvent.ACTION_UP:
                    int upY = (int) ev.getY();
                    stopDrag();
                    onDrop(upY);
                    break;

                case MotionEvent.ACTION_MOVE:
                    int moveY = (int) ev.getY();
                    int tempPosition = pointToPosition(0, moveY);
                    if (tempPosition > lockItems) {
                        onDrag(moveY);
                        itemMoveAnimation(moveY);
                    }
                    break;

                case MotionEvent.ACTION_DOWN:
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }


    private void hideDropItem() {
        DragListAdapter adapter = getDragAdapter();
        adapter.showDropItem(false);
    }


    private boolean   isSameDragDirection   = true;
    private int      lastFlag         = -1;
    private int      mFirstVisiblePosition, mLastVisiblePosition;
    private int      turnUpPosition, turnDownPosition;

    private void onChangeCopy(int last, int current) {

        DragListAdapter adapter = getDragAdapter();

        if (last != current) {
            adapter.exchangeCopy(last, current);
        }

    }

    private void itemMoveAnimation(int y) {

        DragListAdapter adapter = getDragAdapter();

        int tempPosition = pointToPosition(0, y);

        if (tempPosition == INVALID_POSITION || tempPosition == mLastPosition) {
            return;
        }

        mFirstVisiblePosition = getFirstVisiblePosition();
        mDragPosition = tempPosition;
        onChangeCopy(mLastPosition, mDragPosition);
        int MoveNum = tempPosition - mLastPosition;
        int count = Math.abs(MoveNum);

        for (int i = 1; i <= count; i++) {
            int xAbsOffset, yAbsOffset;

            if (MoveNum > 0) {

                if (lastFlag == -1) {
                    lastFlag = 0;
                    isSameDragDirection = true;
                }

                if (lastFlag == 1) {
                    turnUpPosition = tempPosition;
                    lastFlag = 0;
                    isSameDragDirection = !isSameDragDirection;
                }

                if (isSameDragDirection) {
                    mHoldPosition = mLastPosition + 1;
                }
                else {
                    if (mStartPosition < tempPosition) {
                        mHoldPosition = mLastPosition + 1;
                        isSameDragDirection = !isSameDragDirection;
                    }
                    else {
                        mHoldPosition = mLastPosition;
                    }
                }

                xAbsOffset = 0;
                yAbsOffset = -mItemVerticalSpacing;
                mLastPosition++;

            }
            else {

                if (lastFlag == -1) {
                    lastFlag = 1;
                    isSameDragDirection = true;
                }

                if (lastFlag == 0) {
                    turnDownPosition = tempPosition;
                    lastFlag = 1;
                    isSameDragDirection = !isSameDragDirection;
                }

                if (isSameDragDirection) {
                    mHoldPosition = mLastPosition - 1;
                }
                else {

                    if (mStartPosition > tempPosition) {
                        mHoldPosition = mLastPosition - 1;
                        isSameDragDirection = !isSameDragDirection;
                    }
                    else {
                        mHoldPosition = mLastPosition;
                    }
                }

                xAbsOffset = 0;
                yAbsOffset = mItemVerticalSpacing;
                mLastPosition--;

            }

            adapter.setHeight(mItemVerticalSpacing);
            adapter.setIsSameDragDirection(isSameDragDirection);
            adapter.setLastFlag(lastFlag);

            ViewGroup moveView = (ViewGroup) getChildAt(mHoldPosition - getFirstVisiblePosition());

            Animation animation;
            if (isSameDragDirection) {
                animation = getFromSelfAnimation(xAbsOffset, yAbsOffset);
            }
            else {
                animation = getToSelfAnimation(xAbsOffset, -yAbsOffset);
            }
            moveView.startAnimation(animation);
        }
    }

    private void onDrop(int x, int y) {
        DragListAdapter adapter = getDragAdapter();
        adapter.setInvisiblePosition(-1);
        adapter.showDropItem(true);
        adapter.notifyDataSetChanged();
    }

    private void startDrag(Bitmap bm, int y) {
        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = y - mDragPoint + mDragOffset;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

//        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;

        mWindowParams.alpha = 0.8f;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(bm);

        mWindowManager.addView(imageView, mWindowParams);
        mDragImageView = imageView;
    }

    public void onDrag(int y) {
        int drag_top = y - mDragPoint;

        if (mDragImageView != null && drag_top >= 0) {
            mWindowParams.alpha = 1.0f;
            mWindowParams.y = y - mDragPoint + mDragOffset;
            mWindowManager.updateViewLayout(mDragImageView, mWindowParams);
        }
        doScroller(y);
    }

    public void doScroller(int y) {
        if (y < mUpScrollBounce) {
            mCurrentStep = mStep + (mUpScrollBounce - y) / 10;
        }
        else if (y > mDownScrollBounce) {
            mCurrentStep = -(mStep + (y - mDownScrollBounce)) / 10;
        }
        else {
            mCurrentStep = 0;
        }

        View view = getChildAt(mDragPosition - getFirstVisiblePosition());
        setSelectionFromTop(mDragPosition, view.getTop() + mCurrentStep);

    }

    public void stopDrag() {

        isMoving = false;

        if (mDragImageView != null) {
            mWindowManager.removeView(mDragImageView);
            mDragImageView = null;
        }
        isSameDragDirection = true;
        lastFlag = -1;

        final DragListAdapter adapter=getDragAdapter();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.isMoving = false;
            }
        }, 500);
        adapter.setLastFlag(lastFlag);
        adapter.postList();
    }

    public void onDrop(int y) {
        onDrop(0, y);
    }

    private Animation getFromSelfAnimation(int x, int y) {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, x, Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, y);
        translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(ANIMATION_DURATION);
        translateAnimation.setInterpolator(new AccelerateInterpolator());
        return translateAnimation;
    }

    private Animation getToSelfAnimation(int x, int y) {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, x, Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, y, Animation.RELATIVE_TO_SELF, 0);
        translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(ANIMATION_DURATION);
        translateAnimation.setInterpolator(new AccelerateInterpolator());
        return translateAnimation;
    }

    public boolean isLock() {
        return isLock;
    }

    public void setLock(boolean lock) {
        isLock = lock;
    }

    private int lockItems = -1;

    public void setLockItems(int lockItems) {
        this.lockItems = lockItems;
    }

    public int getmLastPosition() {
        return mLastPosition;
    }

    public void setmLastPosition(int mLastPosition) {
        this.mLastPosition = mLastPosition;
    }
}