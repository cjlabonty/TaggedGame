package com.cjlab.taggedgame;

import android.content.res.Resources;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Player {
    private String username;
    private boolean tagged;
    private static boolean tagSent;
    public static int numPlayers = 0;
    private double tagDist = 0.0002;

    FirebaseDatabase database;
    DatabaseReference ref;

    private double x, y;
    private double tagX, tagY;
    private static double tagSpeed;
    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    private static float angle;
    private int count;
    private boolean eliminated;

    private boolean populated;

    private ArrayList<Player> players;

    public Player() {
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Players");
        System.out.println("Empty Constructor activated");
//        tagSent = getTagSent();
//        x = 99999;
//        tagSent = false;
    }

    public Player(String user, boolean isTagged) {
        username = user;
        tagged = isTagged;
        numPlayers++;
        populated = false;

        x = 0;
        y = 0;

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Players");

        tagX = x;
        tagY = y;
        tagSent = false;
        eliminated = false;

        count = 0;
        tagSpeed = 200;
        angle = 0;

        players = PlayerManager.getPlayers();
    }

    public Player(String user, boolean isTagged, boolean p) {
        username = user;
        tagged = isTagged;
        numPlayers++;

        x = Math.random() * 360 - 180;
        y = Math.random() * 180 - 90;

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Players");

        tagX = x;
        tagY = y;
        tagSent = false;
        eliminated = false;
        populated = true;

        count = 0;
        tagSpeed = 200;
        angle = 0;

        players = PlayerManager.getPlayers();
    }

    public void update() {
        if(tagSent && tagged && !eliminated) {
//            System.out.println(tagX);
//            System.out.println(tagY);
            //tagSpeed = 2;
            tagDist = tagSpeed / 200;
            tagX += tagSpeed * Math.cos(angle) / 1000;
            tagY += tagSpeed * Math.sin(angle) / 1000;
            //System.out.println(tagSpeed);

            if(tagX < -180) { tagX = 180; }
            if(tagX > 180) { tagX = -180; }
            if(tagY < -90) { tagY = 90; } // Fix later
            if(tagY > 90) { tagY = -90; } // Fix later

            // If player receives own tag
//            if(count > tagDist / tagSpeed * 2) {
//                if(tagY > y - tagDist && tagY < y + tagDist && tagX > x - tagDist && tagX < x + tagDist) {
//                    System.out.println("Received own tag");
//                    tagSent = false;
//                    ref.child(getUsername()).child("tagSent").setValue(false);
//                }
//            }

            for(int i = 0; i < PlayerManager.getPlayers().size(); i++)
            {
                if(!PlayerManager.getPlayers().get(i).getUsername().equals(username)) {
                    if(tagX > PlayerManager.getPlayers().get(i).getX() - tagDist && tagX < PlayerManager.getPlayers().get(i).getX() + tagDist
                            && tagY > PlayerManager.getPlayers().get(i).getY() - tagDist && tagY < PlayerManager.getPlayers().get(i).getY() + tagDist
                            && !PlayerManager.getPlayers().get(i).isTagged()){
                        unTag();
                        PlayerManager.getPlayers().get(i).tag();
                        ref.child(PlayerManager.getPlayers().get(i).getUsername()).child("tagged").setValue(true);
//                        ref.child(getUsername()).child("tagged").setValue(false);
//                        tagged = false;
//                        tagSent = false;
                        MapsActivity.updateStatus();
                        break;
                    }
                }
            }
            count++;
        } else {
            tagX = x;
            tagY = y;
            count = 0;
        }
        if(MainActivity.getPopulate() && eliminated) {
            System.out.println("Removing populated player from database");
            ref.child(getUsername()).removeValue();
            tagged = false;
            tagSent = false;
        }
    }

    public void tag() {
        if(!tagged) {
            tagged = true;
            ref.child(getUsername()).child("tagged").setValue(true);
            MapsActivity.updateStatus();
        }
    }

    public void unTag() {
        System.out.println("unTag");
        if(tagged) {
            tagged = false;
            tagSent = false;
            ref.child(getUsername()).child("tagged").setValue(false);
        }
    }

    public void sendTag(double speed) {
        if(tagged && !tagSent && !eliminated) {
            System.out.println("Tag sent (from player class)");
            //ref.child(getUsername()).child("tagSent").setValue(true);
            players = PlayerManager.getPlayers();
            tagX = x;
            tagY = y;
            System.out.println(tagX);
            System.out.println(tagY);
            angle = MapsActivity.getCurrentAngle();
            System.out.println(speed);
            tagSpeed = speed;
            System.out.println(tagSpeed);
            tagSent = true;
        }
    }

    public void sendTag() {
        if(tagged && !tagSent && !eliminated) {
            //ref.child(getUsername()).child("tagSent").setValue(true);
            players = PlayerManager.getPlayers();
            tagX = x;
            tagY = y;
            System.out.println(tagX);
            System.out.println(tagY);
            angle = MapsActivity.getCurrentAngle();
            tagSpeed = 500;
            tagSent = true;
        }
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void eliminate() {
        eliminated = true;
    }

    public static boolean getTagSent() {
        return tagSent;
    }

    public void setTagSent(boolean tagSent) {
        this.tagSent = tagSent;
    }

    public String getUsername() {
        return username;
    }

    public boolean isTagged() {
        return tagged;
    }

    public void setTagged(boolean b) {
        tagged = b;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
        //setRefX();
    }

    public void setRefX(double dx) {
        ref.child(getUsername()).child("x").setValue(dx);
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
        //setRefY();
    }

    public void setRefY(double dy) {
        ref.child(getUsername()).child("y").setValue(dy);
    }

    @Exclude
    public double getTagX() {
        return tagX;
    }

    @Exclude
    public double getTagY() {
        return tagY;
    }

    public void setTagX(double x) {
        tagX =  x;
    }

    public void setTagY(double y) {
        tagX =  y;
    }

    public boolean isPopulated() {
        return populated;
    }

}
