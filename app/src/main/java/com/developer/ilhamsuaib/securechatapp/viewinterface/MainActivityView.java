package com.developer.ilhamsuaib.securechatapp.viewinterface;

import com.developer.ilhamsuaib.securechatapp.model.User;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilham suaib on 18/05/2017.
 */

public interface MainActivityView {
    void loadUsers(ArrayList<String> users, ArrayList<String> uId, FirebaseUser firebaseUser);

    void showProgressDialog(boolean show);

    void showToast(String message);
}
