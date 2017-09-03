package com.lawstub.funnyclub.javabeans;

import cn.bmob.v3.BmobObject;

/**
 * Created by 廖婵001 on 2017/8/28 0028.
 */

public class PictureInfo extends BmobObject {
    private String uris;

    public void setUris(String uris){
        this.uris = uris;
    }

    public String getUris(){
        return uris;
    }
}
