package com.devandroid.fbatista.uberclone.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfigFirebase {

    private static DatabaseReference mReference;
    private static FirebaseAuth mAuth;

    public static FirebaseAuth getFirebaseAuth(){
        if(mAuth != null){
            return mAuth;
        } else{
            mAuth = FirebaseAuth.getInstance();
            return mAuth;
        }
    }

    public static DatabaseReference getDatabaseReference(){
        return FirebaseDatabase.getInstance().getReference();
    }


}
