package com.developer.ilhamsuaib.securechatapp.viewmodel;

import com.developer.ilhamsuaib.securechatapp.model.User;
import com.developer.ilhamsuaib.securechatapp.view.RegisterActivity;
import com.developer.ilhamsuaib.securechatapp.viewinterface.RegisterActivityView;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by ilham suaib on 18/05/2017.
 */

public class RegisterActivityViewModel {

    private DatabaseReference mDatabase;
    private RegisterActivityView view;
    private FirebaseAuth mAuth;
    private RegisterActivity activity;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public RegisterActivityViewModel(RegisterActivity activity, RegisterActivityView view) {
        this.view = view;
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuthListener = (FirebaseAuth firebaseAuth) -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null){
                view.loginIsSucces(true);
            }
        };
    }

    public void register(String email, String password){
        if (email.equals("")|| password.equals("")) {
            view.showToastMessage("Fill the fields first!");
            return;
        }
        if (password.length() < 6) {
            view.showToastMessage("Password min 6 character");
            return;
        }
        view.showProgressDialog(true);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, (Task<AuthResult> task) -> {
            view.showProgressDialog(false);
            if (!task.isSuccessful()) {
                login(email, password);
            } else {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                String username = email.substring(0, email.indexOf("@"));
                String userId = firebaseUser.getUid();

                //input data user
                User user = new User(email, username);
                mDatabase.child("users").child(userId).setValue(user);
            }
        });
    }

    private void login(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, (Task<AuthResult> task) -> {
                    view.loginIsSucces(task.isSuccessful());
                });
    }

    public void startAuthState() {
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void stopAuthState() {
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
