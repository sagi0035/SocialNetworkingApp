package com.example.socialnetworkingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class settingsActivity extends AppCompatActivity {


    private Toolbar mToolBar;
    EditText userName , userProfileName, userStatus, userCountry, userGender, userRelationshipStatus, userDOB;
    Button updateAccountSettingsButton;
    CircleImageView userProfileImage;
    ProgressDialog progressDialog;

    // so here we will create a storagereference
    StorageReference userProfileImageRef;

    // now we will be obtaining a database reference because we wil be getting our info from the database as per what we previously specified
    DatabaseReference settingsUserReference;
    DatabaseReference postsRef;
    FirebaseAuth mAuth;
    // now we will also obtain the userid so as to be able to get the settings of the specific at the time user
    String currentUserId;
    String profUserImage;

    // so this is to obtain the current profile image
    String currentProfileImage;



    // so here we will just get the info of the edittexts
    String profName;
    String profDOB;
    String profGender;
    String profUserName;
    String profRelationshipStatus;
    String profCountry;
    String profStatus;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



        // and here we give value to the storagereference var
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        // and now we have the id of the current user and thus know which settings to use
        currentUserId = mAuth.getCurrentUser().getUid();
        settingsUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        // so we will obtain the value of the current image and save it in a var to be used if the back button rather than the update button is pressed
        settingsUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentProfileImage = snapshot.child("Profile Image").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        // so this gives values to the toolbar through the code
        mToolBar = (Toolbar) findViewById(R.id.settingsToolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = (EditText) findViewById(R.id.settingsProfileName);
        userProfileName = (EditText) findViewById(R.id.settingsUsername);
        userStatus = (EditText) findViewById(R.id.settingsprofileStatus);
        userCountry = (EditText) findViewById(R.id.settingsCountry);
        userGender = (EditText) findViewById(R.id.settingsGender);
        userRelationshipStatus = (EditText) findViewById(R.id.settingsRelationshipStatus);
        userDOB = (EditText) findViewById(R.id.settingsDOB);

        updateAccountSettingsButton = (Button) findViewById(R.id.accountSettingsButton);

        userProfileImage = (CircleImageView) findViewById(R.id.settingsProfileImage);





        // so now we will create a valueeventlistener to determine if we were successful in obtaining the user for the settings
        settingsUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // so now we will get all the specific values for the info specified
                    String myProfileImage = snapshot.child("Profile Image").getValue().toString();
                    String myStatus = snapshot.child("Status").getValue().toString();
                    String myCountry = snapshot.child("Country").getValue().toString();
                    String myUserName = snapshot.child("Username").getValue().toString();
                    String myFullName = snapshot.child("Full name").getValue().toString();
                    String myGender = snapshot.child("Gender").getValue().toString();
                    String myRelationshipStatus = snapshot.child("Relationship Status").getValue().toString();
                    String myDOB = snapshot.child("DOB").getValue().toString();

                    userProfileName.setText(myUserName);
                    userCountry.setText(myCountry);
                    userRelationshipStatus.setText(myRelationshipStatus);
                    userStatus.setText(myStatus);
                    userDOB.setText(myDOB);
                    userGender.setText(myGender);
                    userName.setText(myFullName);

                    Log.i("thh",myProfileImage);

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.empty).into(userProfileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        // now we are going to provide the basis for a user changing some things which is done through the button
        updateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // here we will validate and then save the new info
                validateAccountInfo();
            }
        });

        // now we will go about changing the image potentially
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });


    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data!=null) {
            // so in this case we get the uri which is essentially just the link to the gallery
            Uri imageUri = data.getData();
            // and here is were we add the cropping functionality!
            // this is gotten to by way of an external library
            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);

        }

        // so this will now be to get access to the chosen image so that we can later on save to firebase
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                // so now we get the uri of the specific image that we cropped!
                Uri resultUri = result.getUri();
                // we will save it inside of a var to be used later with the save changes clicker and save the chosen image to give the user a preview o the settings
                profUserImage = resultUri.toString();
                Picasso.get().load(profUserImage).placeholder(R.drawable.empty).into(userProfileImage);


            }




        }

    }



    public void validateAccountInfo() {
        // so here we will just get the info of the edittexts
        String profName = userProfileName.getText().toString();
        String profDOB = userDOB.getText().toString();
        String profGender = userGender.getText().toString();
        String profUserName = userName.getText().toString();
        String profRelationshipStatus = userRelationshipStatus.getText().toString();
        String profCountry = userCountry.getText().toString();
        String profStatus = userStatus.getText().toString();

        // and here we will be validating that something is written in each of these fields
        if (TextUtils.isEmpty(profName)) {
            Toast.makeText(this, "Do not leave empty fields!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(profDOB)) {
            Toast.makeText(this, "Do not leave empty fields!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(profGender)) {
            Toast.makeText(this, "Do not leave empty fields!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(profUserName)) {
            Toast.makeText(this, "Do not leave empty fields!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(profRelationshipStatus)) {
            Toast.makeText(this, "Do not leave empty fields!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(profCountry)) {
            Toast.makeText(this, "Do not leave empty fields!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(profStatus)) {
            Toast.makeText(this, "Do not leave empty fields!", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Updating");
            progressDialog.setMessage("Please wait while we update your information!");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            // so now if nothing is empty we willl just update everything i firebase so that the changes are also reflected in the settings page
            updateAccountInfo(profCountry,profDOB,profStatus,profGender,profRelationshipStatus,profUserName,profName);

        }

    }

    private void updateAccountInfo(String profCountry, String profDOB, String profStatus, String profGender, String profRelationshipStatus, String profUserName, String profName) {
        // so this is were we will begin updating the info in firebase

        // we will do this by way of a hashmap
        HashMap hashMap = new HashMap();
        hashMap.put("Country",profCountry);
        hashMap.put("DOB",profDOB);
        hashMap.put("Status",profStatus);
        hashMap.put("Gender",profGender);
        hashMap.put("Relationship Status",profRelationshipStatus);
        hashMap.put("Username",profName);
        if (profUserName!=null) {
            hashMap.put("Full name",profUserName);
            UpdatePostNames();
        }
        if (profUserImage!=null) {
            hashMap.put("Profile Image",profUserImage);
            UpdatePostProfileImages();
        } else {
            Toast.makeText(this, "We still have problems!!!!", Toast.LENGTH_SHORT).show();
        }



        // and now we will save everything inside our firebase database
        settingsUserReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(settingsActivity.this, "Update successful!", Toast.LENGTH_SHORT).show();
                    // and then when this was a success we can just send the user back to the main activity
                    sendUserToMainActivity();
                }
            }
        });


    }

    private void sendUserToMainActivity() {
        progressDialog.dismiss();
        Intent send = new Intent(settingsActivity.this,MainActivity.class);
        send.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(send);
        finish();
    }

    public void openGallery() {
        // were here we are obtaining from the gallery as we did with setUpActiviity
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,1);
    }


    // Update "Posts" with the new URL image
    private void UpdatePostProfileImages() {
        // query the user's uid in post to filter out only the current user's posts
        Query query = postsRef.orderByChild("uid").equalTo(currentUserId);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // iterate through each post and update the URL on each post
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if (profUserImage!=null) {
                        ds.child("profileImage").getRef().setValue(profUserImage);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        query.addListenerForSingleValueEvent(valueEventListener);
    }

    // Update "Posts" with the new URL image
    private void UpdatePostNames() {
        // query the user's uid in post to filter out only the current user's posts
        Query query = postsRef.orderByChild("uid").equalTo(currentUserId);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String profUserName = userName.getText().toString();
                // iterate through each post and update the URL on each post
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if (profUserName!=null) {
                        Log.i("Thiiiis","Happpennned");
                        ds.child("fullname").getRef().setValue(profUserName);
                    } else {
                        Log.i("thiiis","sam");
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        query.addListenerForSingleValueEvent(valueEventListener);

    }

}