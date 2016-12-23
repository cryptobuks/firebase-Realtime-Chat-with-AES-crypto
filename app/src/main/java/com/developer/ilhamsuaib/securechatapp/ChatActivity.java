package com.developer.ilhamsuaib.securechatapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.ilhamsuaib.securechatapp.aeshelper.AESHelper;
import com.developer.ilhamsuaib.securechatapp.entity.Chat;
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

public class ChatActivity extends AppCompatActivity {

    private final String TAG = "TAG";
    private String myId, myUsername, userId, username, time;
    private String[] arrTextMsgOptions = {"Delete","View Ecrip & Decript"};
    private String[] arrAudioMsgOptions = {"Delete"};

    private Toolbar toolbar;

    private ListView chatList;
    private EditText edtMessage;
    private ImageView imgSend, imgRecord;
    private RelativeLayout recordLayout;
    private TextView txtTimer, txtKetRecord;

    private FirebaseListAdapter<Chat> mAdapter;
    private DatabaseReference mDatabase;
    private FirebaseUser mFirebaseUser;

    private long startTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    private MediaRecorder mMediaRecorder;
    private String outputFile = null;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/securechatapp.mp3";
        //get data from main activity

        myId = getIntent().getStringExtra("myId");
        userId = getIntent().getStringExtra("userId");
        username = getIntent().getStringExtra("username");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle(username);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //setup view
        chatList = (ListView) findViewById(R.id.chat_list);
        edtMessage = (EditText) findViewById(R.id.edt_message);
        imgSend = (ImageView) findViewById(R.id.img_send);
        imgRecord = (ImageView) findViewById(R.id.img_record);
        recordLayout = (RelativeLayout) findViewById(R.id.record_layout);
        txtTimer = (TextView) findViewById(R.id.txt_timer);
        txtKetRecord = (TextView) findViewById(R.id.txt_ket_record);

        //set chat adapter
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myUsername = mFirebaseUser.getEmail().substring(0, mFirebaseUser.getEmail().indexOf("@"));
        mAdapter = new ChatAdapter(this, Chat.class, R.layout.chat_list_item, mDatabase.child("chat").child(myId).child(userId), myUsername);
        chatList.setAdapter(mAdapter);

        recordLayout.setVisibility(View.GONE);

        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0){
                    imgSend.setImageResource(R.drawable.send);
                    if (recordLayout.getVisibility() == View.VISIBLE){
                        recordLayout.setVisibility(View.GONE);
                    }
                }else{
                    imgSend.setImageResource(R.drawable.mic);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        edtMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recordLayout.getVisibility() == View.VISIBLE){
                    recordLayout.setVisibility(View.GONE);
                }
            }
        });

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtMessage.getText().toString().length() <= 0 && recordLayout.getVisibility() == View.GONE){
                    recordLayout.setVisibility(View.VISIBLE);
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }else if(edtMessage.getText().toString().length() > 0){
                    sendTextMessage();
                }else if(recordLayout.getVisibility() == View.VISIBLE){
                    recordLayout.setVisibility(View.GONE);
                }
            }
        });

        imgRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN :
                        startRecording();
                        return true;
                    case MotionEvent.ACTION_UP :
                        stopRecording();
                        return true;
                }
                return false;
            }
        });

        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Chat c = mAdapter.getItem(i);
                if (c.getMessageType().equals(Chat.MessageType.Audio)){
                    String mp3 = null;
                    try {
                        mp3 = AESHelper.decrypt(String.valueOf(R.string.myKey), c.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    createMp3(mp3);
                    mp = new MediaPlayer();
                    try {
                        mp.setDataSource(outputFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(ChatActivity.this, "File not found! \n"+e, Toast.LENGTH_SHORT).show();
                    }

                    try {
                        mp.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(ChatActivity.this, "Failed to play file \n"+e, Toast.LENGTH_SHORT).show();
                    }
                    mp.start();
                }
            }
        });

        chatList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                Chat c = mAdapter.getItem(i);
                if (c.getMessageType().equals(Chat.MessageType.Audio)){
                    builder.setItems(arrAudioMsgOptions, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            switch (which){
                                case 0:
                                    deleteMessage(i);
                                break;
                            }
                        }
                    });
                }else{
                    builder.setItems(arrTextMsgOptions, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            switch (which){
                                case 0:
                                    deleteMessage(i);
                                    break;
                                case 1:
                                    viewEnkripAndDekripDialog(i);
                                    break;
                            }
                        }
                    });
                }
                Dialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });
    }

    private void viewEnkripAndDekripDialog(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.enkripanddekrip_dialog, null);
        EditText edtChiper = (EditText) linearLayout.findViewById(R.id.edt_chiper);
        EditText edtDekripsi = (EditText) linearLayout.findViewById(R.id.edt_dekrip);
        Chat c = mAdapter.getItem(position);
        String chiper = c.getMessage();
        String dekripsi = null;
        try {
            dekripsi = AESHelper.decrypt(String.valueOf(R.string.myKey), chiper);
        } catch (Exception e) {
            e.printStackTrace();
        }
        edtChiper.setText(chiper);
        edtDekripsi.setText(dekripsi);
        builder.setView(linearLayout);
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void deleteMessage(int index){
        mDatabase = mAdapter.getRef(index);
        mDatabase.removeValue();
    }

    private void createMp3(String dataByte){
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

    private void stopRecording(){
        Toast.makeText(ChatActivity.this, "Stop recording!", Toast.LENGTH_SHORT).show();
        txtKetRecord.setText("Hold to record");

        customHandler.removeCallbacks(updateTimerThread);
        startTime = 0L;
        txtTimer.setText("00:00");

        if (mMediaRecorder != null){
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();

            new SendAudioMessageAsyny().execute();
        }
    }

    private void startRecording(){
        Toast.makeText(ChatActivity.this, "Start recording!", Toast.LENGTH_SHORT).show();
        txtKetRecord.setText("Release to send");
        mp = MediaPlayer.create(this, R.raw.sound);
        mp.start();

        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);

        try {
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setOutputFile(outputFile);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        }catch (IllegalStateException e){
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis()-startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime/1000);
            int mins = secs/60;
            secs = secs % 60;
            txtTimer.setText(""+mins+":"+String.format("%02d", secs));
            customHandler.postDelayed(this, 0);
        }
    };

    private void sendTextMessage(){
        Toast.makeText(ChatActivity.this, "Send text message!", Toast.LENGTH_SHORT).show();
        time = DateFormat.getDateTimeInstance().format(new Date());
        String message = null;
        try {
            message = AESHelper.encrypt(String.valueOf(R.string.myKey), edtMessage.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Chat c = new Chat();
        c.setTime(time);
        c.setMessage(message);
        c.setUsername(myUsername);
        c.setMessageType(Chat.MessageType.Text);
        sendMessage(c);
        edtMessage.setText("");
    }

    private void sendMessage(Chat c){
        mDatabase.child("chat").child(myId).child(userId).push().setValue(c);
        mDatabase.child("chat").child(userId).child(myId).push().setValue(c);
        mDatabase.keepSynced(true);
        scroll();
    }

    private void scroll(){
        chatList.setSelection(mAdapter.getCount()-1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private String getByteCode(String path){
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
            Log.e(TAG, e.getMessage());
            strResult.append(e.getMessage());
            e.printStackTrace();
        }
        return strResult.toString();
    }

    class SendAudioMessageAsyny extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... voids) {
            String result = getByteCode(outputFile);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.length()>0){
                String enkripAdio = null;
                try {
                    enkripAdio = AESHelper.encrypt(String.valueOf(R.string.myKey), s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                time = DateFormat.getDateTimeInstance().format(new Date());
                Chat c = new Chat();
                c.setTime(time);
                c.setMessage(enkripAdio);
                c.setUsername(myUsername);
                c.setMessageType(Chat.MessageType.Audio);
                sendMessage(c);
            }else{
                Toast.makeText(ChatActivity.this, "Message not send!",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
