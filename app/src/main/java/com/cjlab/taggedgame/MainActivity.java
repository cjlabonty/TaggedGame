package com.cjlab.taggedgame;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.database.*;

import java.util.Map;


public class MainActivity extends AppCompatActivity {

    public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference;

    private EditText regName;
    String provider;
    private int FINE_PERMISSION_CODE = 1;
    private int COARSE_PERMISSION_CODE = 1;
    private int lastView = 1;

    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    Context context;

    public static boolean startTagged;
    public static boolean populatePlayers;

    public static GameManager manager;

    public static GameView view;
    //public MapsActivity mapView;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Firebase
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Players");

        // Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestFineLocationPermission();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestCoarseLocationPermission();
        }

        regName = (EditText) findViewById(R.id.textUser);

        startTagged = false;
        populatePlayers = false;

        context = this;

        manager = new GameManager(this);

        //view = new GameView(this);
        //mapView = new MapsActivity();
        //mapView = LayoutInflater.from(this).inflate(R.layout.activity_maps, this);

        // mMapsActivity ma = new MapsActivity();
        intent = new Intent(getApplicationContext(), MapsActivity.class);
        //MapsActivity ma = new MapsActivity();
        //intent.putExtra("")

        //checkForViewChange();
        setContentView(R.layout.activity_main);
        //checkForViewChange();
        float scalingFactor = 0.5f; // scale down to half the size
        // Do math to figure out location
        //view.setScaleX(scalingFactor);
        //view.setScaleY(scalingFactor);
        //view.setClickable(true);
        //view.setX(100);
        //view.setY(100);
//        CustomLayout group = new CustomLayout(this);
//        group.addView(view);
//        group.addView(mapView);
//        setContentView(mapView);
        //changeView(3);

    }

    private void requestFineLocationPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Fine location is needed for this app to work.  You cannot play unless this permission is allowed")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
        }

    }

    private void requestCoarseLocationPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Coarse location is needed for this app to work.  You cannot play unless this permission is allowed")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},COARSE_PERMISSION_CODE);
        }

    }

    public void joinGame(View v) {
        regName = (EditText) findViewById(R.id.textUser);
        String text = regName.getText().toString();
        intent.putExtra("Username", text);
        startActivity(intent);
    }

    public void setMapView() {
        setContentView(R.layout.activity_maps);
    }

    public void setGameView(Context c) {
        System.out.println(c);
        Activity a = (Activity) c;
        a.setContentView(view);
    }

    public void populate(View v) {
        populatePlayers = true;
    }

    public static boolean getPopulate() {
        return populatePlayers;
    }

    public void startTag(View v) {
        startTagged = true;
    }

    public void clearData(View v) {
        FirebaseDatabase.getInstance().getReference().child("Players")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Player p = snapshot.getValue(Player.class);
                            reference.child(p.getUsername()).removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    public static FirebaseDatabase getDatabase() {
        return database;
    }

}