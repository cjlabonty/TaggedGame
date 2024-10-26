package com.cjlab.taggedgame;

import android.content.Context;
import com.google.firebase.database.*;
import java.util.ArrayList;

public class PlayerManager {

    public static ArrayList<Player> players = new ArrayList<>();
    private Compass compass;
    private float sentAngle;
    FirebaseDatabase database;
    DatabaseReference ref;
    public static Player user;

    public PlayerManager(Player player, Context context) {
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Players");

        sentAngle = 0;

        user = player;
        checkForNewPlayer();
        System.out.println(user);

        compass = new Compass(context);
    }


    public void checkForNewPlayer() {
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Player p = dataSnapshot.getValue(Player.class);
                players.add(p);
                MapsActivity.updatePlayers(players.size());
                // Maybe add user separately?
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                System.out.println(dataSnapshot.getValue(Player.class));
                if(dataSnapshot.getValue(Player.class).getUsername() != null) {
                    if(dataSnapshot.getValue(Player.class).isPopulated() && dataSnapshot.getValue(Player.class).isEliminated()) {
                        System.out.println("Removing pop player");
                        ref.child(dataSnapshot.getValue(Player.class).getUsername()).removeValue();
                        removePlayer(dataSnapshot.getValue(Player.class).getUsername());
                    }
                    if(dataSnapshot.getValue(Player.class).getUsername().equals(user.getUsername())) {
                        System.out.println("User Update");
                        //user = dataSnapshot.getValue(Player.class);
                        if(dataSnapshot.getValue(Player.class).isEliminated()) {
                            System.out.println("Removing self from database");
                            ref.child(dataSnapshot.getValue(Player.class).getUsername()).removeValue();
                            removePlayer(dataSnapshot.getValue(Player.class).getUsername());
                            user.eliminate();
                            MapsActivity.updatePlayers(players.size());
                        } else {
                            if(dataSnapshot.getValue(Player.class).isTagged()) {
                                MapsActivity.getUser().setTagged(true);
                            }
                        }
//                    MapsActivity.getUser().setX(dataSnapshot.getValue(Player.class).getX());
//                    MapsActivity.setUser(dataSnapshot.getValue(Player.class));
//                    MapsActivity.getUser().setTagX(tagx);
//                    MapsActivity.getUser().setTagY(tagy);
                    }

                }

                System.out.println("Players update");
                if(dataSnapshot.getValue(Player.class).getUsername() != null) {
                    for(int i = 0; i < players.size(); i++) {
                        if(players.get(i).getUsername().equals(dataSnapshot.getValue(Player.class).getUsername())) {
//                        if(players.get(i).getUsername().equals(MapsActivity.getUser().getUsername())) {
//                            players.remove(i);
//                            players.add(MapsActivity.getUser());
//                        } else {
                            players.remove(i);
                            Player p = dataSnapshot.getValue(Player.class);
                            players.add(p);
                            //}
                            break;
                        }
                    }
                }
                MapsActivity.drawPlayers();
                MapsActivity.updateStatus();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for(int i = 0; i < players.size(); i++) {
                    if(players.get(i).getUsername().equals(dataSnapshot.getValue(Player.class).getUsername())) {
                        players.remove(i);
                        break;
                    }
                }
                MapsActivity.updatePlayers(players.size());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static ArrayList<Player> getPlayers() {
        return players;
    }

    public void removePlayer(String username) {
        for(int i = 0; i < players.size(); i++) {
            if(players.get(i).getUsername().equals(username)) {
                players.remove(i);
            }
        }
    }

}

