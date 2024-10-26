package com.cjlab.taggedgame;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private MainThread thread;

    public static ArrayList<Player> players = new ArrayList<Player>();
    public static Bitmap[] bitmaps = new Bitmap[4];
    private Compass compass;
    private float sentAngle;
    FirebaseDatabase database;
    DatabaseReference ref;
    public static Player user;
    public static int viewNum;

    private static float screenX;
    private static float screenY;
    public static float speedY;
    private float lastY;
    private boolean sent;
    private int touchCount;
    private View mapView;

    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    // Bitmaps
    Bitmap playerNormal = BitmapFactory.decodeResource(getResources(),R.drawable.playersprite);
    Bitmap playerTagged = BitmapFactory.decodeResource(getResources(),R.drawable.playerspritetagged);
    Bitmap tagSprite  = BitmapFactory.decodeResource(getResources(),R.drawable.tag);
    Bitmap directionSprite  = BitmapFactory.decodeResource(getResources(),R.drawable.directioncircle);
    Bitmap tagScreen = BitmapFactory.decodeResource(getResources(),R.drawable.taggedscreen);
    Bitmap safeScreen = BitmapFactory.decodeResource(getResources(),R.drawable.safescreen);

    public GameView(Context context) {
        super(context);
        //onSwipeTouchListener = new OnSwipeTouchListener(context);
        getHolder().addCallback(this);

        //onSwipeTouchListener = new OnSwipeTouchListener(context);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Players");

        sentAngle = 0;
        viewNum = 1;
        sent = false;
        touchCount = 0;

        screenX = 0;
        screenY = 0;
        lastY = 0;
        speedY = 0;

        thread = new MainThread(getHolder(), this);

        setFocusable(true);

        mapView = findViewById(R.layout.activity_maps);


        tagScreen = Bitmap.createScaledBitmap(tagScreen, screenWidth, screenHeight, false);
        safeScreen = Bitmap.createScaledBitmap(safeScreen, screenWidth, screenHeight, false);

        bitmaps[0] = playerNormal;
        bitmaps[1] = playerTagged;
        bitmaps[2] = tagSprite;
        bitmaps[3] = tagScreen;
        //bitmaps[3] = directionSprite;
        int randName = (int) (Math.random() * 10000);
        String username = Integer.toString(randName);

        user = new Player(username, MainActivity.startTagged);
        //if((int) (Math.random() * 2) == 1) { user.tag(); }
        ref.child(user.getUsername()).setValue(user);
        checkForNewPlayer();

        //if((int) (Math.random() * 2) == 0) { user.tag(); }

        //this.setOnTouchListener(this);
        //players.add(new Player("player", bitmaps, true, players, 200, 200));
        //players.add(new Player("other", bitmaps, false, players, 200, 700));
        //players.add(new Player("other", bitmaps, false, players, 600, 900));

        //compass = new Compass(directionSprite, user, context);
    }


    public void checkForNewPlayer() {
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Player p = dataSnapshot.getValue(Player.class);
                players.add(p);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                System.out.println("changed");
                if(dataSnapshot.getValue(Player.class).getUsername().equals(user.getUsername())) {
                    user = dataSnapshot.getValue(Player.class);
                }
                for(int i = 0; i < players.size(); i++) {
                    if(players.get(i).getUsername().equals(dataSnapshot.getValue(Player.class).getUsername())) {
                        players.remove(i);
                        Player p = dataSnapshot.getValue(Player.class);
                        players.add(p);
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for(int i = 0; i < players.size(); i++) {
                    if(players.get(i).getUsername().equals(dataSnapshot.getValue(Player.class).getUsername())) {
                        players.remove(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(!user.getTagSent()) {
            sentAngle = compass.getAngle();
        }

        if(event.getX() < 250 && event.getY() < 250) {
            System.out.println("LLLLLLLLLL");
            //MainActivity.showMapView();
            GameManager gm = new GameManager(getContext());
            gm.toMapView();
            gm = null;
        }

        if(!sent) {
            if(event.getAction() == MotionEvent.ACTION_MOVE && lastY > event.getY()) {
                screenY -= lastY - event.getY();
            }
            if(event.getAction() == MotionEvent.ACTION_UP && !sent) {
                screenY = 0;
            }

            if(lastY - event.getY() > 200) {
                sent = true;
                sendAnimation(lastY - event.getY());
            }
            lastY = event.getY();
        }

        return true;
    }

    public void sendAnimation(float spd) {
        System.out.println("Sending");
        while(screenY > -screenHeight) {
            // How to run every frame?
            screenY -= spd;
        }
        user.sendTag(spd);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();

            } catch(InterruptedException e){
                e.printStackTrace();
            }
            retry = false;
        }
    }

    @Override
    public void draw(Canvas canvas)
    {
        super.draw(canvas);
//        if(canvas!=null) {
//            canvas.drawColor(Color.WHITE);
//
//            if(user.isTagged()) {
//                canvas.drawBitmap(tagScreen, 0, (int) screenY, null);
//            } else {
//                canvas.drawBitmap(safeScreen, 0, (int) screenY, null);
//            }
//            //compass.draw(canvas);
//            //canvas.drawBitmap(loadBitmapFromView(mapView), 0, 0, null);
//
//            for(int i = 0; i < players.size(); i++) {
//                players.get(i).draw(canvas, bitmaps, sentAngle);
//            }
//            user.draw(canvas, bitmaps, sentAngle);
//
//        }
    }

    public void drawMap() {
        //user.animateTag();
    }

    public static Bitmap loadBitmapFromView(View v) {
        if (v.getMeasuredHeight() <= 0) {
            v.measure(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
            Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            //Canvas c = new Canvas(b);
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
            //v.draw(c);
            return b;
        }
        Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        //Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        //v.draw(c);
        return b;
    }

//    public static Player getUser() {
//        return user;
//    }

    public static void changeScreenY(int c) {
        screenY += c;
    }

    public static int getViewNum() {
        return viewNum;
    }

    public static void setViewNum(int n) {
        viewNum = n;
    }

    public static ArrayList<Player> getPlayers() {
        return players;
    }

    public static Bitmap[] getBitmaps() {
        return bitmaps;
    }


}

