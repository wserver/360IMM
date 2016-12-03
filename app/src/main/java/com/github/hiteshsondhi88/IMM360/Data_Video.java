package com.github.hiteshsondhi88.IMM360;

import java.io.Serializable;

/**
 * Created by osujin on 2016-08-09.
 */
public class Data_Video implements Serializable {

    public int index;
    public int time;
    public String uri;
    public String uri_phone;
    public String thumbnailUri = "";


        public Data_Video(int index, int time, String uri, String uri_phone) {
            this.index = index;
            this.time = time;
            this.uri = uri;
            this.uri_phone = uri_phone;
        }

    public String getThumbnailUri() {
        return thumbnailUri;
    }

    public void setThumbnailUri(String thumbnailUri) {
        this.thumbnailUri =  thumbnailUri;
//        Environment.getExternalStorageDirectory().getPath() +
    }
}
