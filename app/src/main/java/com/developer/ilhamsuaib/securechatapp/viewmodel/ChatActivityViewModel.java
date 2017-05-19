package com.developer.ilhamsuaib.securechatapp.viewmodel;

import android.os.AsyncTask;
import android.os.Environment;

import com.developer.ilhamsuaib.securechatapp.R;
import com.developer.ilhamsuaib.securechatapp.helper.AESHelper;
import com.developer.ilhamsuaib.securechatapp.model.Chat;
import com.developer.ilhamsuaib.securechatapp.view.ChatActivity;
import com.developer.ilhamsuaib.securechatapp.viewinterface.ChatActivityView;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by ilham suaib on 18/05/2017.
 */

public class ChatActivityViewModel {

    private ChatActivityView view;
    private ChatActivity activity;
    private DatabaseReference mDatabase;
    private FirebaseUser mFirebaseUser;

    private String outputFile;
    private String myUsername;
    private String myId;
    private String userId;
    private String username;
    private String time;

    public ChatActivityViewModel(ChatActivity activity, ChatActivityView view) {
        this.activity = activity;
        this.view = view;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/securechatapp.mp3";
        myId = activity.getIntent().getStringExtra("myId");
        userId = activity.getIntent().getStringExtra("userId");
        username = activity.getIntent().getStringExtra("username");
        myUsername = mFirebaseUser.getEmail().substring(0, mFirebaseUser.getEmail().indexOf("@"));
    }

    public void sendTextMessage(String message){
        view.showToast("Send text message!");
        time = DateFormat.getDateTimeInstance().format(new Date());
        try {
            message = AESHelper.encrypt(String.valueOf(R.string.myKey), message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Chat c = new Chat();
        c.setTime(time);
        c.setMessage(message);
        c.setUsername(myUsername);
        c.setMessageType(Chat.MessageType.Text);
        sendMessage(c);
        view.sendTextMessageCallback();
    }

    public String getByteCode(String path){
        File file = new File(path);
        StringBuffer strResult = new StringBuffer();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            boolean eof = false;
            while (!eof){
                int input = fileInputStream.read();
                if (input == -1){
                    eof = true;
                }else {
                    strResult.append(input);
                    strResult.append(",");
                }
            }
            fileInputStream.close();
        } catch (Exception e) {
            strResult.append(e.getMessage());
            e.printStackTrace();
        }
        return strResult.toString();
    }

    public void createMp3(String dataByte){
        String tmp = dataByte;
        try{
            File file = new File(outputFile);
            FileOutputStream outputStream = new FileOutputStream(file);
            do {
                String data = tmp.substring(0, tmp.indexOf(","));
                int bit = Integer.parseInt(data);
                outputStream.write(bit);
                tmp = tmp.substring(tmp.indexOf(",")+1);
            }while (tmp.contains(","));
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Chat c){
        mDatabase.child("chat").child(myId).child(userId).push().setValue(c);
        mDatabase.child("chat").child(userId).child(myId).push().setValue(c);
        mDatabase.keepSynced(true);
    }

    public void deleteMessage(FirebaseListAdapter<Chat> mAdapter, int index){
        mDatabase = mAdapter.getRef(index);
        mDatabase.removeValue();
        view.deleteMessageCallback();
    }

    public String getMyUsername(){
        return myUsername;
    }

    public String getMyId() {
        return myId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
