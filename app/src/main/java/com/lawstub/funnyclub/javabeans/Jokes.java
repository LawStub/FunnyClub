package com.lawstub.funnyclub.javabeans;

import cn.bmob.v3.BmobObject;

/**
 *
 */

public class Jokes extends BmobObject {
    private String title;
    private String content;

    public void setTitle(String title){
        this.title = title;
    }

    public void setContent(String content){
        this.content = content;
    }

    public String getTitle(){
        return title;
    }

    public String getContent(){
        return content;
    }
}
