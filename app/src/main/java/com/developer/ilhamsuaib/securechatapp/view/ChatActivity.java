package com.developer.ilhamsuaib.securechatapp.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.developer.ilhamsuaib.securechatapp.R;
import com.developer.ilhamsuaib.securechatapp.adapter.ChatAdapter;
import com.developer.ilhamsuaib.securechatapp.helper.AESHelper;
import com.developer.ilhamsuaib.securechatapp.databinding.ActivityChatBinding;
import com.developer.ilhamsuaib.securechatapp.databinding.EnkripanddekripDialogBinding;
import com.developer.ilhamsuaib.securechatapp.model.Chat;
import com.developer.ilhamsuaib.securechatapp.viewinterface.ChatActivityView;
import com.developer.ilhamsuaib.securechatapp.viewmodel.ChatActivityViewModel;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class ChatActivity extends AppCompatActivity implements ChatActivityView {

    private ChatActivityViewModel viewModel;

    private String myId, myUsername, userId, username, time;
    private String[] arrTextMsgOptions = {"Delete","View Ecrip & Decript"};
    private String[] arrAudioMsgOptions = {"Delete"};

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

    private ActivityChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        viewModel = new ChatActivityViewModel(this, this);

        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/securechatapp.mp3";

        myId = viewModel.getMyId();
        userId = viewModel.getUserId();
        username = viewModel.getUsername();

        setTitle(username);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myUsername = viewModel.getMyUsername();
        mAdapter = new ChatAdapter(this, Chat.class, mDatabase.child("chat").child(myId).child(userId), myUsername);
        binding.chatList.setAdapter(mAdapter);

        binding.recordLayout.setVisibility(View.GONE);

        binding.edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0){
                    binding.imgSend.setImageResource(R.drawable.send);
                    if (binding.recordLayout.getVisibility() == View.VISIBLE){
                        binding.recordLayout.setVisibility(View.GONE);
                    }
                }else{
                    binding.imgSend.setImageResource(R.drawable.mic);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.edtMessage.setOnClickListener(view -> {
            if (binding.recordLayout.getVisibility() == View.VISIBLE){
                binding.recordLayout.setVisibility(View.GONE);
            }
        });

        binding.imgSend.setOnClickListener(view -> {
            if (binding.edtMessage.getText().toString().length() <= 0 && binding.recordLayout.getVisibility() == View.GONE){
                binding.recordLayout.setVisibility(View.VISIBLE);
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }else if(binding.edtMessage.getText().toString().length() > 0){
                viewModel.sendTextMessage(binding.edtMessage.getText().toString());
            }else if(binding.recordLayout.getVisibility() == View.VISIBLE){
                binding.recordLayout.setVisibility(View.GONE);
            }
        });

        binding.imgRecord.setOnTouchListener((View view, MotionEvent motionEvent) -> {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN :
                    startRecording();
                    return true;
                case MotionEvent.ACTION_UP :
                    stopRecording();
                    return true;
            }
            return false;
        });

        binding.chatList.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) ->{
            Chat c = mAdapter.getItem(i);
            if (c.getMessageType().equals(Chat.MessageType.Audio)){
                String mp3 = null;
                try {
                    mp3 = AESHelper.decrypt(String.valueOf(R.string.myKey), c.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                viewModel.createMp3(mp3);
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
        });

        binding.chatList.setOnItemLongClickListener((AdapterView<?> adapterView, View view, final int i, long l) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            Chat c = mAdapter.getItem(i);
            if (c.getMessageType().equals(Chat.MessageType.Audio)){
                builder.setItems(arrAudioMsgOptions, (DialogInterface dialogInterface, int which) -> {
                    if (which == 0)
                        viewModel.deleteMessage(mAdapter,i);
                });
            }else{
                builder.setItems(arrTextMsgOptions, (DialogInterface dialogInterface, int which) -> {
                    if (which == 0) viewModel.deleteMessage(mAdapter,i);
                    else if (which == 1) viewEnkripAndDekripDialog(i);
                });
            }
            Dialog dialog = builder.create();
            dialog.show();
            return true;
        });
    }

    private void viewEnkripAndDekripDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        EnkripanddekripDialogBinding dialogBinding = EnkripanddekripDialogBinding.inflate(LayoutInflater.from(this), null, false);
        Chat c = mAdapter.getItem(position);
        String chiper = c.getMessage();
        String dekripsi = null;
        try {
            dekripsi = AESHelper.decrypt(String.valueOf(R.string.myKey), chiper);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialogBinding.edtChiper.setText(chiper);
        dialogBinding.edtDekrip.setText(dekripsi);
        builder.setView(dialogBinding.getRoot());
        builder.setPositiveButton("Close", (DialogInterface dialogInterface, int i) -> {
            dialogInterface.dismiss();
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void stopRecording(){
        binding.txtKetRecord.setText("Hold to record");

        customHandler.removeCallbacks(updateTimerThread);
        startTime = 0L;
        binding.txtTimer.setText("00:00");

        if (mMediaRecorder != null){
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();

            new SendAudioMessageAsync().execute();
        }
    }

    private void startRecording(){
        binding.txtKetRecord.setText("Release to send");
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
            binding.txtTimer.setText(""+mins+":"+String.format("%02d", secs));
            customHandler.postDelayed(this, 0);
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void sendTextMessageCallback() {
        binding.edtMessage.setText("");
    }

    @Override
    public void deleteMessageCallback() {
        showToast("Message deleted!");
    }

    class SendAudioMessageAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result = viewModel.getByteCode(outputFile);
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
                viewModel.sendMessage(c);
            }else{
                showToast("Message not send!");
            }
        }
    }
}