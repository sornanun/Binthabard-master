package com.example.sornanun.binthabard;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by SORNANUN on 4/6/2560.
 */

public class FirebaseController{

    FirebaseDatabase database;
    DatabaseReference ref;
    FirebaseUser user;

    String currentUser;
    String currentUserID;

    FirebaseController() {
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("monkLocation");
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();

            currentUser = email.substring(0, email.length() - 15);
            currentUserID = user.getUid();
        }
    }

    public void saveOrUpdateLocationToFirebase(String latitude, String longitude, String address, String updateTime) {
        ref.child(currentUserID).child("monkUser").setValue(currentUser);
        ref.child(currentUserID).child("monkLat").setValue(latitude);
        ref.child(currentUserID).child("monkLong").setValue(longitude);
        ref.child(currentUserID).child("monkAddress").setValue(address);
        ref.child(currentUserID).child("monkUpdateTime").setValue(updateTime);
    }

    public void removeLacationFromFirebase() {
        ref.child(currentUserID).removeValue();
    }
}
