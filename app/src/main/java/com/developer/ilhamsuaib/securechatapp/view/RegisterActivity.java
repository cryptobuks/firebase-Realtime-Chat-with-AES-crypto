package com.developer.ilhamsuaib.securechatapp.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.developer.ilhamsuaib.securechatapp.R;
import com.developer.ilhamsuaib.securechatapp.databinding.ActivityRegisterBinding;
import com.developer.ilhamsuaib.securechatapp.viewinterface.RegisterActivityView;
import com.developer.ilhamsuaib.securechatapp.viewmodel.RegisterActivityViewModel;

public class RegisterActivity extends AppCompatActivity implements RegisterActivityView{

    private RegisterActivityViewModel viewModel;
    private ProgressDialog progressDialog;
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        viewModel = new RegisterActivityViewModel(this, this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading authentication...");
        progressDialog.setCancelable(false);

        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.edtUsermail.getText().toString();
            String password = binding.edtPassword.getText().toString();
            viewModel.register(email, password);
        });
    }

    @Override
    public void showProgressDialog(boolean show) {
        if (show) progressDialog.show();
        else progressDialog.dismiss();
    }

    @Override
    public void showToastMessage(String message) {
        Toast.makeText(RegisterActivity.this, message,
                    Toast.LENGTH_SHORT).show();
    }

    @Override
    public void loginIsSucces(boolean isSucces) {
        if (isSucces){
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        }else{
            showToastMessage("Login Failed, try again!");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewModel.startAuthState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.stopAuthState();
    }
}
