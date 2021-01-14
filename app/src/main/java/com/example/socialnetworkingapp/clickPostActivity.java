package com.example.socialnetworkingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class clickPostActivity extends AppCompatActivity {

    private ImageView postImageView;
    private TextView postTextView;
    private Button deletePostButton;
    private Button editPostButton;
    private String postKey;
    private String currentUserId;
    private String dataBaseUsrId;
    private DatabaseReference clickPostsRef;
    String description;
    String image;
    String downloadUrlAgain;
    Uri theimageUri;
    CircleImageView theOther;
    StorageReference postImagesReferenceAgain;
    boolean allower;

    // so now we will go about getting the current user so that they can edit their own posts and nobody else's
    private FirebaseAuth mAuth;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        postImagesReferenceAgain = FirebaseStorage.getInstance().getReference();

        progressDialog = new ProgressDialog(this);

        // so now we go about obtaining our uid to know which user is the current one!
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        // so now we get the key of what we are editing/deleting
        postKey = getIntent().getStringExtra("Post Key");
        clickPostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        Log.i("Za post key is",postKey);

        postImageView = (ImageView) findViewById(R.id.postImage);
        postTextView = (TextView) findViewById(R.id.postDescription);
        deletePostButton = (Button) findViewById(R.id.deletePostButton);
        editPostButton = (Button) findViewById(R.id.editPostButton);

        // so we will initially set the buttons to invisible and only make them visible if they are your own
        editPostButton.setVisibility(View.INVISIBLE);
        deletePostButton.setVisibility(View.INVISIBLE);

        clickPostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // so we will now make sure that the datasnapshot exists before doin g all the jazz i.e. that nothing was deleted
                if (snapshot.exists()) {
                    // and now very importantly we will also go about getting each pressed uid to in the future see if it will match that of the current user
                    dataBaseUsrId = snapshot.child("uid").getValue().toString();

                    // so now we get the the stuff from firebase
                    try {
                        description = snapshot.child("contents").getValue().toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        image = snapshot.child("postImage").getValue().toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    if (description != null) {
                        postTextView.setText(description);
                    }

                    if (image != null) {
                        Log.i("Your imager is",image);
                        Picasso.get().load(image).placeholder(R.drawable.empty).into(postImageView);
                    } else {
                        Picasso.get().load(R.drawable.empty).placeholder(R.drawable.empty).into(postImageView);
                    }

                    // so now we will check if the userid's initialised in fact match
                    if (currentUserId.equals(dataBaseUsrId)) {
                        // and if they do we will set the buttons to visible
                        editPostButton.setVisibility(View.VISIBLE);
                        deletePostButton.setVisibility(View.VISIBLE);
                    }

                    // so now we will create an edit onclicker to edit
                    editPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // so here we will of course edit the current post
                            editCurrentPost(description,image);
                        }
                    });

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // now we will create an onclick listener to deal with deletepost
        deletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // now the first thing that we will want to do is delete the post and probably even within firebase
                deleteCurrentPost();
            }
        });


    }

    public void deleteCurrentPost() {
        // so we are just going to remove the post info
        clickPostsRef.removeValue();
        // then we will send the user to the main activity
        sendUserToMainActivity();
        Toast.makeText(this, "Your post has been deleted!!", Toast.LENGTH_SHORT).show();
    }

    public void sendUserToMainActivity() {
        Intent intentFive = new Intent(clickPostActivity.this,MainActivity.class);
        intentFive.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentFive);
        finish();
    }

    public void openGallery() {
        // were here we are obtaining from the gallery as we did with setUpActiviity
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data!=null) {
            theimageUri = data.getData();
            Picasso.get().load(theimageUri).placeholder(R.drawable.empty).into(theOther);
            progressDialog.dismiss();

        }
    }

    public void editCurrentPost(String description,String x) {
        // so this alertdialog will create a pop-up which will be a linearlayout with both an image and a description
        AlertDialog.Builder builder = new AlertDialog.Builder(clickPostActivity.this);
        builder.setTitle("Edit Post:");

        LinearLayout linearLayout = new LinearLayout(clickPostActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        theOther = new CircleImageView(clickPostActivity.this);
        Picasso.get().load(x).placeholder(R.drawable.empty).into(theOther);
        theOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // so this will open all of our images
                openGallery();
            }
        });
        linearLayout.addView(theOther);

        final EditText inputField = new EditText(clickPostActivity.this);
        inputField.setText(description);
        linearLayout.addView(inputField);
        builder.setView(linearLayout);

        // so we change in the case of a positive
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Log.i("Mesut","Ozil");

                    progressDialog.setTitle("Updating");
                    progressDialog.setMessage("Please wait while we update your information!");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    allower = true;
                    clickPostsRef.child("contents").setValue(inputField.getText().toString());
                    Toast.makeText(clickPostActivity.this, "Your post has been updated!", Toast.LENGTH_SHORT).show();
                    //sendUserToMainActivity();
                }  catch (Exception e) {
                    e.printStackTrace();
                }


                try {
                    // VERY IMPORTANT TO KEEP IN MIND WHEN SAVING IMAGES TO DATABASE look below
                    // SO to make sure that the url is saved correctly thus allowing correct displays we will first save the image to storage and then save to the database from there!
                    final StorageReference filePath = postImagesReferenceAgain.child("Post Images").child(theimageUri.getLastPathSegment() +"edited"+ ".jpg");
                    // so once the filepath has been created we signify whether or not they have been saved to storage

                    // so now this is all to save the uri (key that ti is the uri) so that it can be displayed on the app
                    filePath.putFile(theimageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                filePath.putFile(theimageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                // so we preven the sendUserMainActivity from executing in the case of the image being clicked
                                                allower = false;
                                                downloadUrlAgain = uri.toString();
                                                clickPostsRef.child("postImage").setValue(downloadUrlAgain);
                                                Toast.makeText(clickPostActivity.this, "Image uploaded successfully to storage!", Toast.LENGTH_SHORT).show();
                                                //sendUserToMainActivity();
                                                progressDialog.dismiss();
                                            }
                                        });
                                    }
                                });
                            } else {
                                Toast.makeText(clickPostActivity.this, "Error whilst uploading image!\nPlase try again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    // clickPostsRef.child("postImage").setValue(downloadUrlAgain);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (theimageUri==null) {
                    progressDialog.dismiss();
                }








            }





        });

        // and we delete everything in the case of a negative
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }
}