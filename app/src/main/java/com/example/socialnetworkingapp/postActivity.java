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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class postActivity extends AppCompatActivity {

    Toolbar mToolBar;
    ImageButton selectPostImage;
    Button updatePostButton;
    EditText postDescription;

    ProgressDialog loadingBar;

    String contentsOfThePost;

    // now we will also go about creating a reference for storage
    StorageReference postImagesReference;

    // we will also go about creating a reference to the database
    DatabaseReference databaseReference, postRef;

    FirebaseAuth mAuth;

    String signifier;


    String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, currentUserId;

    Uri imageUri;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // so now we initialised towards the actual value
        postImagesReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth = FirebaseAuth.getInstance();
        // so this is now to get the current user
        currentUserId = mAuth.getCurrentUser().getUid();


        selectPostImage = (ImageButton) findViewById(R.id.selectPostImage);
        updatePostButton = (Button) findViewById(R.id.updatePostButton);
        postDescription = (EditText) findViewById(R.id.postDescription);

        loadingBar = new ProgressDialog(this);

        // make sure that you are importing the right toolbar here because sometimes it is wrong!
        mToolBar = (Toolbar) findViewById(R.id.updatePostPageToolbar);
        setSupportActionBar(mToolBar);
        // so these serve to create that top bar with both a title and a going back option!
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Post!");


        // so now we will also go about creating an onclicklistener
        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // so here we will go about creating the gallery of where you will be posting an image
                openGallery();
            }
        });

        // now we will also want to create a onclick for the button
        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePostInfo();
            }
        });


    }

    // this now serves to pick up on when somebody pressed the back button!
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // so first we get the id
        int id = item.getItemId();

        // now this if statement will determine if the user clicked on the back button
        if (id == android.R.id.home) {
            // then we will send the user to the main activity because the back button was pressed!
            sendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendUserToMainActivity() {
        Intent toMainActivity = new Intent(postActivity.this,MainActivity.class);
        if (imageUri != null) {
            toMainActivity.putExtra("the image uri",imageUri.toString());
        }
        startActivity(toMainActivity);
    }

    public void openGallery() {
        // were here we are obtaining from the gallery as we did with setUpActiviity
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,1);
    }

    public void validatePostInfo() {
        // so essentially what we will be doing here is saving to firebase

        // and first we will get the contents of the post
        contentsOfThePost = postDescription.getText().toString();

        // now we have to make sure that we have at least an image or text
        if (TextUtils.isEmpty(contentsOfThePost) && imageUri == null) {
            Toast.makeText(this, "You need to place either an image or post!", Toast.LENGTH_SHORT).show();
        } else {
            // and here is were we will begin doing our stuff if an image or a post was placed in or both
            if (imageUri != null && !TextUtils.isEmpty(contentsOfThePost)) {
                signifier = "both";
                loadingBar.setTitle("Profile Image and Post");
                loadingBar.setMessage("Please wait while we update!");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(false);
                storingToFirebaseStorage();
            } else if (imageUri!=null) {
                signifier = "image";
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait while we update!");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(false);
                storingToFirebaseStorage();
            } else if (!TextUtils.isEmpty(contentsOfThePost)) {
                signifier = "post";
                loadingBar.setTitle("Profile Post");
                loadingBar.setMessage("Please wait while we update!");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(false);
                storingToFirebaseStorage();
            }
        }
    }

    public void storingToFirebaseStorage() {
        // now to amke each entry unique we will get the time and date that the post was made

        // so forst we will work on getting the date
        Calendar callForDate = Calendar.getInstance();
        // make sure that the pattern is actually formatted in this way because otherwise you may get wonky results
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        // now we will go about getting the time
        Calendar callForTime = Calendar.getInstance();
        // make sure that the pattern is actually formatted in this way because otherwise you may get wonky results
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        // now we will create a single string variable to get both the date & time which will be our child of child in storage
        postRandomName = saveCurrentDate + saveCurrentTime;


        if (signifier.equals("image") || signifier.equals("both")) {
            // so we wil go about creating a folder for the posts
            final StorageReference filePath = postImagesReference.child("Post Images").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");
            // so once the filepath has been created we signify whether or not they have been saved to storage


            // so now this is all to save the uri (key that ti is the uri) so that it can be displayed on the app
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloadUrl = uri.toString();
                                        savePostInformationToFirebaseDatabase();
                                        Toast.makeText(postActivity.this, "Image uploaded successfully to storage!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    } else {
                        Toast.makeText(postActivity.this, "Error whilst uploading image!\nPlase try again!", Toast.LENGTH_SHORT).show();
                    }
                }
            });



        } else {
            savePostInformationToFirebaseDatabase();
        }
    }

    public void savePostInformationToFirebaseDatabase() {
        // so now we create an eventlistener for the current user
        databaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userFullName = dataSnapshot.child("Full name").getValue().toString();
                    String userProfileImage = dataSnapshot.child("Profile Image").getValue().toString();


                    // so here we will create a hashmap
                    HashMap postsMap = new HashMap();
                    postsMap.put("uid",currentUserId);
                    postsMap.put("fullname",userFullName);
                    postsMap.put("profileImage",userProfileImage);
                    postsMap.put("date",saveCurrentDate);
                    postsMap.put("time",saveCurrentTime);

                    // now depending on whether the user inputted an image, post or both we do the stuff
                    if (signifier.equals("both")) {
                        postsMap.put("postImage",downloadUrl);
                        postsMap.put("contents",contentsOfThePost);
                    } else if (signifier.equals("image")) {
                        postsMap.put("postImage",downloadUrl);
                    } else if (signifier.equals("post")) {
                        postsMap.put("contents",contentsOfThePost);
                    }

                    // so this will now serve to save all the stuff into the database
                    postRef.child(postRandomName+currentUserId).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                // and in the case were it was successful we will of course want to send the user to the main activity
                                sendUserToMainActivity();
                                Toast.makeText(postActivity.this, "It was a success!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            } else {
                                Toast.makeText(postActivity.this, "There was some sort of an issue there!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // so now we will go about creating an onActivity  to handle an activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data!=null) {
            imageUri = data.getData();
            selectPostImage.setImageURI(imageUri);
        }

    }
}