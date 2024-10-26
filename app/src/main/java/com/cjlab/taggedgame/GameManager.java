package com.cjlab.taggedgame;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class GameManager extends MainActivity{

    private Activity activity;
    public Context context;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        context = super.getApplicationContext();
//        activity = (Activity) context;
//        System.out.println(context);
//        System.out.println(activity);
//    }

    public GameManager(Context c) {
        context = c;
        activity = (Activity) context;
    }

    public void setView(View v) {
//        System.out.println(v);
//        activity.setContentView(v);
//        v.bringToFront();

    }

    public void toGameView() {
        //setGameView();
        super.setGameView(context);
        //System.out.println(activity);
        //activity.setContentView(R.layout.activity_main);
    }

    public void toMapView() {
        setMapView();
    }

    public void setView(int i) {
        activity.setContentView(i);
    }
}
