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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class logInActivity extends AppCompatActivity {

    Button logInButton;
    EditText userEmail, userPassword;
    TextView needNewAccountLink;
    // now let us also put a progress dialog
    ProgressDialog theProgressDialog;

    FirebaseAuth mAuth;


    ImageView forGoogle;
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        theProgressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        needNewAccountLink = (TextView) findViewById(R.id.registerAccountLink);
        userEmail = (EditText) findViewById(R.id.logInEmail);
        userPassword = (EditText) findViewById(R.id.logInPassword);
        logInButton = (Button) findViewById(R.id.logInButton);
        forGoogle = (ImageView) findViewById(R.id.googleclone);

        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();
            }
        });

        // so this first creates the request to google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        forGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });



    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            // so if we have obtained the email we will set a progress bar
            theProgressDialog.setTitle("Google sign-in!");
            theProgressDialog.setMessage("Please wait while we sign you in!");
            theProgressDialog.setCanceledOnTouchOutside(true);
            theProgressDialog.show();
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                e.printStackTrace();
                Toast.makeText(this, "There was an error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                theProgressDialog.dismiss();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            sendUserToMainActivity();
                            theProgressDialog.dismiss();
                            // so this will now be to prevent the sign in form being automatic i.e. you actually have to choose an account to sign/log in to!
                            mGoogleSignInClient.revokeAccess();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(logInActivity.this, "Not authenticated\nPlease try again!", Toast.LENGTH_SHORT).show();
                            theProgressDialog.dismiss();
                        }

                        // ...
                    }
                });
    }


    private void sendUserToRegisterActivity() {
        Intent intentTwo = new Intent(logInActivity.this,registerActivity.class);
        intentTwo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentTwo);
        finish();
    }

    public void sendUserToSignInActivity() {
        Intent intentFour = new Intent(logInActivity.this,logInActivity.class);
        intentFour.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentFour);
        finish();
    }

    public void sendUserTosetUpActivity() {
        Intent intentThree = new Intent(logInActivity.this,setUpActivity.class);
        intentThree.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentThree);
        finish();
    }


    public void sendUserToMainActivity() {
        Intent intentFive = new Intent(logInActivity.this,MainActivity.class);
        intentFive.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentFive);
        finish();
    }

    public void logIn(View view) {
        if(TextUtils.isEmpty(userEmail.getText().toString()) || TextUtils.isEmpty(userPassword.getText().toString())) {
            Toast.makeText(this, "Neither the email nor the password can be left empty!", Toast.LENGTH_SHORT).show();
        } else {
            theProgressDialog.setTitle("Logging In");
            theProgressDialog.setMessage("Please wait while we log you in!");
            theProgressDialog.show();
            theProgressDialog.setCanceledOnTouchOutside(true);
            mAuth.signInWithEmailAndPassword(userEmail.getText().toString(),userPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(logInActivity.this, "A successful login!", Toast.LENGTH_SHORT).show();
                        // it is very important to put the intent inside of the oncomplete listener so that we only move to the new intent if we were succesfully logged in when the task is coimpleted!
                        Intent intenter = new Intent(logInActivity.this,MainActivity.class);
                        startActivity(intenter);
                        finish();
                        theProgressDialog.dismiss();
                    } else {
                        Toast.makeText(logInActivity.this, "Incorrect Login Information\nPlease re-enter your email or password!", Toast.LENGTH_SHORT).show();
                        theProgressDialog.dismiss();
                    }
                }
            });
        }

    }

    @Override
    protected void onStart() {

        // so this will now be for the sake of sending the user straight to the mainactivity should they have already been logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // so if there is no user he will have to gign in thus we send them to the signin
        if (currentUser != null) {
            Intent intenter = new Intent(logInActivity.this,MainActivity.class);
            startActivity(intenter);
            finish();
        }

        super.onStart();
    }
}