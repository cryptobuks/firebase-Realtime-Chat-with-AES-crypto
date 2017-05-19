package com.developer.ilhamsuaib.securechatapp.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.developer.ilhamsuaib.securechatapp.R;
import com.developer.ilhamsuaib.securechatapp.databinding.ActivityMainBinding;
import com.developer.ilhamsuaib.securechatapp.viewinterface.MainActivityView;
import com.developer.ilhamsuaib.securechatapp.viewmodel.MainActivityViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MainActivityView {

    private ActivityMainBinding binding;
    private ProgressDialog progressDialog;
    private MainActivityViewModel viewModel;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new MainActivityViewModel(this);

        setTitle("Secure Chat App");
        setSupportActionBar(binding.toolbar);

        binding.txtCu.setText("WELCOME, "+viewModel.getCuName());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading user list...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void loadUsers(ArrayList<String> users, ArrayList<String> uId, FirebaseUser firebaseUser) {
        arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, users);
        binding.lvUser.setAdapter(arrayAdapter);

        binding.lvUser.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("myId", firebaseUser.getUid());
            intent.putExtra("userId", uId.get(position));
            intent.putExtra("username", users.get(position));
            startActivity(intent);
        });
    }

    @Override
    public void showProgressDialog(boolean show) {
        if (show) progressDialog.show();
        else progressDialog.dismiss();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
