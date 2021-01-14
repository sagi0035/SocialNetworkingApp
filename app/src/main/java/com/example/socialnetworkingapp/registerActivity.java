package com.example.socialnetworkingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class registerActivity extends AppCompatActivity {

    EditText userEmail , userPassword , userConfirmPassword;
    Button createAccount;

    // this will be to create a progress dialog
    ProgressDialog loadingBar;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // now we get the instance of firebase
        mAuth = FirebaseAuth.getInstance();

        userEmail = (EditText) findViewById(R.id.registerEmail);
        userPassword = (EditText) findViewById(R.id.registerPassword);
        userConfirmPassword = (EditText) findViewById(R.id.registerConfirmPassword);
        createAccount = (Button) findViewById(R.id.registerButton);

        loadingBar = new ProgressDialog(this);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        // so if there is no user he will have to gign in thus we send them to the signin
        if (currentUser != null) {
            Intent intenter = new Intent(registerActivity.this,MainActivity.class);
            startActivity(intenter);
            finish();
        }

        super.onStart();
    }

    private void CreateNewAccount() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String confirmPassword = userConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email cannot be empty!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password) || password.length() < 5) {
            Toast.makeText(this, "Password cannot be empty and must be at least 5 characters long!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please confirm your password!", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Your passwords do not match! Please try again!", Toast.LENGTH_SHORT).show();
        } else {
            // so here we will now place the loadingbar to signify that we are loading the registration of the user
            loadingBar.setTitle("Creating new account...");
            loadingBar.setMessage("Please wait while we create your account!");
            loadingBar.show();
            // now this will set the basis the loadingbar to only be removed after everything has finished loading!
            loadingBar.setCanceledOnTouchOutside(true);
            // now in this case we will simply go about registering the user!
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // so here we will first check if the task of creating a user was succesful
                    if (task.isSuccessful()) {
                        // so now what we also want to do is send the user to setup once the task is successful!
                        sendUserToSetUpActivity();

                        Toast.makeText(registerActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    } else {
                        // this will on the other hand be in the case of a failure!
                        String message = task.getException().getMessage();
                        Toast.makeText(registerActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                 }
            });
        }



    }

    private void sendUserToSetUpActivity() {
        Intent setUpIntent = new Intent(registerActivity.this,setUpActivity.class);
        // so this I'm not sure what is achieved by it but I will look at it later!
        setUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setUpIntent);
        finish();
    }
}