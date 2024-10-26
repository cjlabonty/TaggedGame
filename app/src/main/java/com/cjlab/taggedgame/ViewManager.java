package com.cjlab.taggedgame;

import android.app.Activity;
import android.view.View;

public class ViewManager extends MainActivity{

    private int num;
    private View view;
    //Activity a = (Activity) context;
    Activity a;

    public ViewManager(Activity activity) {
        a = activity;
    }

    public ViewManager(int vNum) {
        num = vNum;
    }

    public ViewManager(View v) {
        view = v;
    }

    public void setView() {
//        if(num != 0) {
//            System.out.println(num);
//            //a.setContentView(num);
//            super.changeView(num);
//        } else if(view != null) {
//            super.changeView(view);
//        } else {
//            System.out.println("Invalid View");
//        }
        a.setContentView(R.layout.activity_maps);
    }
}
