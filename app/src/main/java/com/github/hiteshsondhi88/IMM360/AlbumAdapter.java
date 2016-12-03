/*
 * Copyright 2016 LG Electronics Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.hiteshsondhi88.IMM360;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.osclibrary.HttpAsyncTask;
import com.example.osclibrary.OSCParameterNameMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class AlbumAdapter extends ArrayAdapter<HashMap<String, String>> {
    private static final String TAG = AlbumAdapter.class.getSimpleName();

    private final Activity mContext;

    //Array list for file information HashMap
    private final ArrayList<HashMap<String, String>> mItemInfo;

    //Array list for thumbnail id
    private final ArrayList<Integer> mBitmapId;

    private int mCurrentPosition = 0; //The position of view which currently updates UI

    private Bitmap mDummyBitmap; //Dummy image  for thumbnail
    private final int mDummyCnt = 10; //The number for bounds of cancel the thumbnail request

    public enum selectedGalleryType {CAMERA_IMAGE, CAMERA_VIDEO, DOWNLOAD_IMAGE, DOWNLOAD_VIDEO};
    private selectedGalleryType galleryType;

    private boolean duringDeletion;

    public AlbumAdapter(Activity context, ArrayList<HashMap<String, String>> itemInfo, ArrayList<Integer> thumbnailsId) {
        super(context, R.layout.item_album, itemInfo);
        mContext = context;
        mItemInfo = itemInfo;
        mBitmapId = thumbnailsId;
        duringDeletion = false;
        try {
            //Set dummy image for thumbnail
            InputStream istr = mContext.getAssets().open("waiting.png");
            mDummyBitmap = VrUtils.scaleBitmap(BitmapFactory.decodeStream(istr), 300, 150);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDuringDeletion (boolean mode) {
        duringDeletion = mode;
    }


    public void setType(selectedGalleryType type) {
        galleryType = type;
    }

    public class ViewHolder {
        public TextView textViewName;
        public TextView textViewSize;
        public ImageView imageView;
    }


    private void setRowBackground(int position, View rowView, ViewGroup parent) {
        final ListView lv = (ListView) parent;
        int colorHighlight = ContextCompat.getColor(mContext, R.color.colorHighlight);
        int colorTransparent = ContextCompat.getColor(mContext, R.color.colorTransparent);

        if (lv.isItemChecked(position)) {
            rowView.setBackgroundColor(colorHighlight);
        } else {
            rowView.setBackgroundColor(colorTransparent);
        }
    }

        private String convertBytesToMB(String sizeString) {
        double size = Float.parseFloat(sizeString);
        double mbSize = size / (1024 * 1024);

        return String.format("%.2f", mbSize) + "MB";
    }

    public View getView(final int position, View view, ViewGroup parent) {
        mCurrentPosition = position; //Set current position
        LayoutInflater inflater = mContext.getLayoutInflater();

        View rowView = view;
        ViewHolder holder;
        if (view == null)
        {
            rowView = inflater.inflate(R.layout.item_album, null, true);
            holder = new ViewHolder();
            holder.imageView = (ImageView) rowView.findViewById(R.id.icon);
            holder.textViewName = (TextView) rowView.findViewById(R.id.itemname);
            holder.textViewSize = (TextView) rowView.findViewById(R.id.itemsize);
            rowView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) rowView.getTag();
        }

        //Set background color of current row
        setRowBackground(position, rowView, parent);

        final ImageView rowImageView = holder.imageView;
        rowImageView.setImageBitmap(mDummyBitmap);

        if (position < mItemInfo.size()) {
            //Get item information of current position
            HashMap<String, String> info = mItemInfo.get(position);
            holder.textViewName.setText(info.get(OSCParameterNameMapper.FileInfo.NAME));
            String sizeString =
                    convertBytesToMB(info.get(OSCParameterNameMapper.FileInfo.SIZE));
            holder.textViewSize.setText(sizeString);
        }

        if (position < mBitmapId.size() && !duringDeletion) {
            //Get thumbnail bitmap data of current position
            HttpAsyncTask.OnCancelCallback cancelCallback = new HttpAsyncTask.OnCancelCallback() {
                //Return http request need to be canceled or not
                //if position of this call back is out of bound, then cancel the request
                //   bound =>  (current position - 10, current position + 10)
                @Override
                public boolean cancelBackground(Object object) {
                    int tmpPosition = position; // the position of requested item
                    if (object != null)
                        tmpPosition = (int) object;
                    return !(((mCurrentPosition - mDummyCnt) < tmpPosition)
                            && (tmpPosition < (mCurrentPosition + mDummyCnt)));
                }
            };
            HttpAsyncTask.OnHttpListener thumbnailListener = new HttpAsyncTask.OnHttpListener() {
                //Set bitmap image as thumbnail of row view at the position
                @Override
                public void onResponse(OSCReturnType type, final Object response) {
                    Log.v(TAG, "type = " + type);
                    if (type == OSCReturnType.SUCCESS && response != null) {
                        if (((mCurrentPosition - mDummyCnt) < position)
                                && (position < (mCurrentPosition + mDummyCnt))) {
                            rowImageView.setImageBitmap((Bitmap) response);
                        }
                    }

                }
            };
            //Request thumbnail image
            getThumbnailImage(position, thumbnailListener, cancelCallback);
        }

        rowImageView.setOnClickListener(new View.OnClickListener() {
            final String imageFileUri = getInfo(position, OSCParameterNameMapper.FileInfo.URL);
            @Override
            public void onClick(View v) {
                ((AlbumActivity)mContext).startViewer("video",imageFileUri);
            }
        });

        return rowView;
    }


    private void getThumbnailImage(int position, HttpAsyncTask.OnHttpListener listener,
                                   HttpAsyncTask.OnCancelCallback cancelCallback) {
        //   get thumbnail image from android database
        if ((galleryType.equals(selectedGalleryType.DOWNLOAD_IMAGE)) || (galleryType.equals(selectedGalleryType.DOWNLOAD_VIDEO))) {
            if (mBitmapId.get(position) == -1) {

            } else {
                ThumbnailBuilder task = new ThumbnailBuilder(mContext, mBitmapId.get(position), position, galleryType);
                task.setOnCancelCallback(cancelCallback);
                task.setListener(listener);
                task.execute();
            }
        }
        else
        {
            Toast.makeText(mContext,"Album Type error",Toast.LENGTH_SHORT).show();

        }

    }

    public void addItem(HashMap<String, String> info, int bitmapId) {
        //Same file info saved at the same position in array lists
        mItemInfo.add(info);
        mBitmapId.add(bitmapId);
    }

    public void removeItems(int[] positions) {
        ArrayList<HashMap<String, String>> tempInfo = new ArrayList<>();

        for (int i = 0; i < positions.length; i++) {
            int position = positions[i];
            tempInfo.add(mItemInfo.get(position));
        }

        int totalItems = mBitmapId.size();
        for (int i = 0; i < tempInfo.size(); i++) {
            mBitmapId.remove(totalItems - (i + 1));
        }

        mItemInfo.removeAll(tempInfo);
    }


    public void removeAllItems() {
        if (!mBitmapId.isEmpty()) {
            mBitmapId.clear();
        }
        if (!mItemInfo.isEmpty()) {
            for (int i = 0; i < mItemInfo.size(); i++) {
                mItemInfo.get(i).clear();
            }
            mItemInfo.clear();
        }
    }



    public String getInfo(int position, String key) {
        HashMap<String, String> info = mItemInfo.get(position);

        if (info.containsKey(key)) {
            return info.get(key);
        }

        return null;
    }

    public int getSize() {
        return mItemInfo.size();
    }
}