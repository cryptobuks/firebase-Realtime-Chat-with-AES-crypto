package com.developer.ilhamsuaib.securechatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.ilhamsuaib.securechatapp.entity.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView txtCu;

    private FirebaseUser firebaseUser;
    private DatabaseReference mDatabase;

    private ListView listView;
    private ArrayList<String> users = new ArrayList<>();
    private ArrayList<String> uId = new ArrayList<>();

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle("Secure Chat App");
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String cuName = firebaseUser.getEmail().substring(0, firebaseUser.getEmail().indexOf("@"));
        txtCu = (TextView) findViewById(R.id.txt_cu);
        txtCu.setText("WELCOME, "+cuName);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading user list...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        listView = (ListView) findViewById(R.id.lv_user);
        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot user : dataSnapshot.getChildren()){
                    //load data from internet
                    if (!user.getKey().equals(firebaseUser.getUid())){
                        users.add((String) user.child("username").getValue());
                        uId.add(user.getKey());
                    }
                }
                progressDialog.dismiss();
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, users);
                listView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String userId = uId.get(i);
                String username = users.get(i);
                String myId = firebaseUser.getUid();

                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("myId", myId);
                intent.putExtra("userId", userId);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
