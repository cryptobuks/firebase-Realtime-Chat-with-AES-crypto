package com.developer.ilhamsuaib.securechatapp.viewmodel;

import com.developer.ilhamsuaib.securechatapp.viewinterface.MainActivityView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by ilham suaib on 18/05/2017.
 */

public class MainActivityViewModel {

    private MainActivityView view;
    private FirebaseUser firebaseUser;
    private DatabaseReference mDatabase;

    private ArrayList<String> users = new ArrayList<>();
    private ArrayList<String> uId = new ArrayList<>();

    public MainActivityViewModel(MainActivityView view) {
        this.view = view;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        loadUsers();
    }

    private void loadUsers(){
        view.showProgressDialog(true);
        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                view.showProgressDialog(false);
                for (DataSnapshot user : dataSnapshot.getChildren()){
                    //load data from database
                    if (!user.getKey().equals(firebaseUser.getUid())){
                        users.add((String) user.child("username").getValue());
                        uId.add(user.getKey());
                    }
                }
                view.loadUsers(users, uId, firebaseUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                view.showProgressDialog(false);
                view.showToast("Database error!");
            }
        });
    }

    public String getCuName() {
        String currentUserName = firebaseUser.getEmail().substring(0, firebaseUser.getEmail().indexOf("@"));
        return currentUserName;
    }
}
