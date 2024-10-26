package com.cjlab.taggedgame;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener{

    private static GoogleMap mMap;
    private static ArrayList<Marker> markers = new ArrayList<>();

    public FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static DatabaseReference ref;

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;

    public static double playerLatitude;
    public static double playerLongitude;

    private static Marker tagMark;
    private static Marker compassMark;

    private static Player user;

    private static float[] compassValues;
    private static int compassCount = 0;
    private static float currentAngle;

    private boolean moveCamera = true;

    private static TextView status;
    private static TextView playerCount;

    private static int maxPlayers;
    private static int currentPlayers;

    private MapsThread thread;
    private static PlayerManager manager;

    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    private static ImageView leftbar;
    private static ImageView rightbar;
    private static ImageView topbar;
    private static ImageView bottombar;
    //private static ImageView statusbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Variables
        compassValues = new float[10];
        status = findViewById(R.id.status);
        playerCount = findViewById(R.id.players);

        // Initializing Database
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Players");

        // Creating User
        String username;
        System.out.println();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null || extras.getSerializable("Username").equals("Username")) {
                int randName = (int) (Math.random() * 10000);
                username = Integer.toString(randName);
            } else {
                username = extras.getString("Username");
            }
        } else {
            username = (String) savedInstanceState.getSerializable("Username");
        }
        user = new Player(username, MainActivity.startTagged);
        manager = new PlayerManager(user, this);
        System.out.println(user);
        System.out.println(user.getUsername());
        ref.child(user.getUsername()).setValue(user);

        if(MainActivity.getPopulate()) {
            for(int i = 0; i < 50; i++) {
                int randName = (int) (Math.random() * 10000);
                username = Integer.toString(randName);
                ref.child(username).setValue(new Player(username, randName > 5000, true));
            }
        }

        // Thread
        thread = new MapsThread(this);
        thread.setRunning(true);
        thread.start();

        // Location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Checking Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);
        } else {
            System.out.println("Permissions not granted");
        }

        playerLatitude = 0;
        playerLongitude = 0;

        //ref.child(user.getUsername()).child("x").setValue(0.001);

        currentPlayers = manager.getPlayers().size();
        maxPlayers = manager.getPlayers().size();

        // Screen colored borders
        leftbar = findViewById(R.id.leftbar);
        rightbar = findViewById(R.id.rightbar);
        topbar = findViewById(R.id.topbar);
        bottombar = findViewById(R.id.bottombar);
        //statusbar = findViewById(R.id.statusbar);

        updateStatus();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        System.out.println(manager.getPlayers());
//        for(Player p : manager.getPlayers()) {
//            System.out.println(p.getUsername());
//            LatLng temp = new LatLng(p.getX(), p.getY());
//            if(p.isTagged()) {
//                Marker marker = mMap.addMarker(new MarkerOptions().position(temp).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
//            } else {
//                Marker marker = mMap.addMarker(new MarkerOptions().position(temp).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
//            }
//        }
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("Location Change");
        if(!user.isEliminated()) {
            playerLatitude = location.getLatitude();
            playerLongitude = location.getLongitude();
            user.setX(playerLongitude);
            user.setY(playerLatitude);
            user.setRefX(playerLongitude);
            user.setRefY(playerLatitude);
            System.out.println("Location Change");
        }

        if(moveCamera) {
            LatLng cam = new LatLng(playerLatitude, playerLongitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(cam));
        }
        moveCamera = false;

        drawPlayers();

        updateStatus();
    }

    public static void drawPlayers() {
        // Clearing Markers
        for(Marker m: markers) {
            m.remove();
        }
        // Drawing players
        System.out.println("Drawing Players");
        for(Player p : manager.getPlayers()) {
            LatLng temp = new LatLng(p.getY(), p.getX());
            Marker mark;
            if(p.isTagged()) {
                mark = mMap.addMarker(new MarkerOptions().position(temp).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title(p.getUsername()));
            } else {
                mark = mMap.addMarker(new MarkerOptions().position(temp).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(p.getUsername()));
            }
            markers.add(mark);
        }
    }

    public void endRound(View v) {
        for(Player p : manager.getPlayers()) {
            if(p.isTagged()) {
                p.eliminate();
                p.setTagged(false);
                if(p.getUsername().equals(user.getUsername())) {
                    user.eliminate();
                }
                LatLng pos = new LatLng(p.getY(), p.getX());
                //mMap.addMarker(new MarkerOptions().position(pos).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)).title(p.getUsername()));
                ref.child(p.getUsername()).child("eliminated").setValue(true);
//                manager.removePlayer(p.getUsername());
            }
        }
    }

    public void drawTag() {
        if(mMap != null && !user.isEliminated()) {
//            if(tagMark != null) {
//                tagMark.remove();
//            }
            LatLng tPos = new LatLng(user.getTagY(), user.getTagX());
            tagMark = mMap.addMarker(new MarkerOptions().position(tPos).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(tPos));
        }
    }

    public void removeTagMark() {
        if(tagMark != null) {
            tagMark.remove();
        }
    }

    public void drawCompass() {
        if(compassMark != null) {
            compassMark.remove();
        }
        if(mMap != null & user.getX() != 0.0 && compassCount > 10 && !user.isEliminated()) {
            float zoom = mMap.getCameraPosition().zoom;
            //System.out.println(zoom);
            for(float f: compassValues) {
                currentAngle += f;
            }
            currentAngle /= compassValues.length;
            //LatLng pos = new LatLng(user.getY() + Math.sin(Compass.getAngle()) * (152.9449 * Math.exp(-0.6871*zoom)), user.getX() + Math.cos(Compass.getAngle()) *  (152.9449 * Math.exp(-0.6871*zoom)));
            LatLng pos = new LatLng(user.getY() + Math.sin(currentAngle) * (152.9449 * Math.exp(-0.6871*zoom)), user.getX() + Math.cos(currentAngle) *  (152.9449 * Math.exp(-0.6871*zoom)));
            compassMark = mMap.addMarker(new MarkerOptions().position(pos).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
        compassValues[compassCount % 10] = Compass.getAngle();
        compassCount ++;
    }

    public void sendTag(View v) {
        if(!user.getTagSent()) {
            //manager.user.sendTag();
            //if(mEdit.getText())
            float zoom = mMap.getCameraPosition().zoom;
            user.sendTag(152.9449 * Math.exp(-0.6871*zoom) * 40);
        }
    }

    public static void updateStatus() {
        status.bringToFront();
        if(!user.isTagged()) {
            status.setText("SAFE");
            leftbar.setColorFilter(Color.argb(255, 50, 255, 100));
            rightbar.setColorFilter(Color.argb(255, 50, 255, 100));
            topbar.setColorFilter(Color.argb(255, 50, 255, 100));
            bottombar.setColorFilter(Color.argb(255, 50, 255, 100));
            //statusbar.setColorFilter(Color.argb(255, 50, 255, 100));
        } else {
            status.setText("TAGGED");
            leftbar.setColorFilter(Color.argb(255, 255, 20, 20));
            rightbar.setColorFilter(Color.argb(255, 255, 20, 20));
            topbar.setColorFilter(Color.argb(255, 255, 20, 20));
            bottombar.setColorFilter(Color.argb(255, 255, 20, 20));
            //statusbar.setColorFilter(Color.argb(255, 255, 20, 20));
        }
        if(user.isEliminated()){
            status.setText("ELIMINATED");
            leftbar.setColorFilter(Color.argb(255, 100, 100, 100));
            rightbar.setColorFilter(Color.argb(255, 100, 100, 100));
            topbar.setColorFilter(Color.argb(255, 100, 100, 100));
            bottombar.setColorFilter(Color.argb(255, 100, 100, 100));
            //statusbar.setColorFilter(Color.argb(255, 100, 100, 100));
        }
    }

    public static void updatePlayers(int p) {
        currentPlayers = p;
        if(currentPlayers > maxPlayers) {
            maxPlayers = p;
        }
        playerCount.setText(currentPlayers + " / " + maxPlayers);
    }

    public void tagNext(View v) {
        for(int i = 0; i < manager.getPlayers().size(); i+=2) {
            manager.getPlayers().get(i).tag();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    public static double getPlayerLatitude() {
        return playerLatitude;
    }

    public static double getPlayerLongitude() {
        return playerLongitude;
    }

    public PlayerManager getManager() {
        return manager;
    }

    public static Player getUser() {
        return user;
    }

    public static void setUser(Player p) {
        user = p;
    }

    public static float getCurrentAngle() {
        if(compassCount > 10) {
            return currentAngle;
        } else {
            return 0;
        }
    }

}