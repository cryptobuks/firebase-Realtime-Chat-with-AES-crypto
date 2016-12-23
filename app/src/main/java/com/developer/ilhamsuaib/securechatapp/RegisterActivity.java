package com.developer.ilhamsuaib.securechatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.developer.ilhamsuaib.securechatapp.entity.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity{

    final String TAG = "TAG";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    private static EditText edtEmail, edtPassword;
    private Button btnRegister;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading authentication...");
        progressDialog.setCancelable(false);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    goToMainActivity();
                }else{
                    Toast.makeText(RegisterActivity.this, "No user logged in",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        edtEmail = (EditText) findViewById(R.id.edt_usermail);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                final String email = edtEmail.getText().toString();
                final String password = edtPassword.getText().toString();
                Log.d(TAG, "Email : "+email+" & Password : "+password);
                register(email, password);
            }
        });
    }

    private void goToMainActivity(){
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        progressDialog.dismiss();
        finish();
    }

    private void register(final String email, final String password){
        boolean isCorrect = true;
        if (edtEmail.getText().toString().isEmpty() || edtPassword.getText().toString().isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(RegisterActivity.this, "Fill the field first!", Toast.LENGTH_SHORT).show();
            isCorrect = false;
        }
        if (edtPassword.getText().toString().length() < 6) {
            progressDialog.dismiss();
            Toast.makeText(RegisterActivity.this, "Password min 6 character", Toast.LENGTH_SHORT).show();
            isCorrect = false;
        }
        if (isCorrect) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        progressDialog.dismiss();
                        login(email, password);
                    } else {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                        String username = email.substring(0, email.indexOf("@"));
                        String userId = firebaseUser.getUid();

                        //input data user
                        User user = new User(email, username);
                        mDatabase.child("users").child(userId).setValue(user);
                    }
                }
            });
        }
    }

    private void login(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "Login Failed!",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(RegisterActivity.this, "Success!",
                                    Toast.LENGTH_SHORT).show();
                            goToMainActivity();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
