package com.example.zct11.course.app;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.example.zct11.course.base.BaseApplication;

/**
 * Created by zct11 on 2017/10/19.
 */

public class CourseApplication extends BaseApplication {

    private static CourseApplication courseApplication;

    public  static  CourseApplication getInstance(){
        return courseApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        courseApplication=this;
        initTextSize();
    }

    /**
     * 使其系统更改字体大小无效
     */
    private void initTextSize() {
        Resources res = getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}
