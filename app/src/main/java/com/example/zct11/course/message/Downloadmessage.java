package com.example.zct11.course.message;

/**
 * Created by zct11 on 2017/11/4.
 */

public class Downloadmessage {
    private String url;
    private String name;
    private String size;


    public Downloadmessage(String url, String name, String size) {
        this.url = url;
        this.name = name;
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
