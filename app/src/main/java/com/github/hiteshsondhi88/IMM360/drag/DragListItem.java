package com.github.hiteshsondhi88.IMM360.drag;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.File;


public class DragListItem {
    private Drawable trashDraw;
    private Drawable dragDraw;
    private String UrlOnPhone;
    public String thumbnailUri;
    private Uri previewDraw;
    private int indexStr;
    private int timeStr;

    public String getThumbnailUri() {
        return thumbnailUri;
    }
    public void setThumbnailUri(String thumbnailUri) {
        this.thumbnailUri = thumbnailUri;
        File file = new File(thumbnailUri);
        Uri uri = Uri.fromFile(file);
        this.previewDraw = uri;
    }

    public String getUrlOnPhone()
    {
        return UrlOnPhone;
    }
    public void setUrlOnPhone(String UrlOnPhone)
    {
        this.UrlOnPhone = UrlOnPhone;
    }

    public Drawable getDragDraw() {
        return dragDraw;
    }
    public void setDragDraw(Drawable dragDraw) {
        this.dragDraw = dragDraw;
    }

    public int getIndexStr() {
        return indexStr;
    }
    public void setIndexStr(int index)
    {
        indexStr = index;
    }

    public Uri getPreviewDraw() {
        return previewDraw;
    }

    public int getTimeStr() {
        return timeStr;
    }
    public void setTimeStr(int timeStr) {
        this.timeStr = timeStr;
    }

    public Drawable getTrashDraw() {
        return trashDraw;
    }
    public void setTrashDraw(Drawable trashDraw) {
        this.trashDraw = trashDraw;
    }
}