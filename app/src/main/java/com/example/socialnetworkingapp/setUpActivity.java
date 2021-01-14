package com.example.socialnetworkingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.security.spec.ECField;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class setUpActivity extends AppCompatActivity {

    EditText userName , fullName , countryName;
    Button saveInformationButton;
    CircleImageView profileImage;

    FirebaseAuth mAuth;
    DatabaseReference usersRef;

    String currentUserId;
    ProgressDialog theProgressDialog;

    // so this will be to save the image within the firebase storage
    StorageReference userProfileImageRef;

    // this will be to determine get the possible intent from mainactivity
    String theExtra;
    // this will be to determine whether or not an image is in fact present
    boolean theAllower;

    boolean anotherTing = false;

    boolean yetAnotherThing;


    // these will now be to get the extra from the intent from the profileimageclick
    String theExtraFromProfileImageUserName;
    String theExtraFromProfileImageFullName;
    String theExtraFromProfileImageCountryName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);


        yetAnotherThing = getIntent().getBooleanExtra("theBoolean",false);


        theExtra = getIntent().getStringExtra("allower");
        if (theExtra == null) {
            theAllower = false;
        } else {
            theAllower = true;
        }


        theProgressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        // so this is where we begin saving to the storge in firebase
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        userName = (EditText) findViewById(R.id.theUserName);
        fullName = (EditText) findViewById(R.id.theFullName);
        countryName = (EditText) findViewById(R.id.theCountry);
        saveInformationButton = (Button) findViewById(R.id.saveButton);
        profileImage = (CircleImageView) findViewById(R.id.theEmpty);

        theExtraFromProfileImageUserName = getIntent().getStringExtra("username");
        theExtraFromProfileImageFullName = getIntent().getStringExtra("fullname");
        theExtraFromProfileImageCountryName = getIntent().getStringExtra("countryname");

        // so now if the extra's are available i.e. if before pressing the profile image the user had written some info it is re-inputted!
        if (theExtraFromProfileImageCountryName != null && theExtraFromProfileImageFullName != null && theExtraFromProfileImageUserName != null) {
            userName.setText(theExtraFromProfileImageUserName);
            fullName.setText(theExtraFromProfileImageFullName);
            countryName.setText(theExtraFromProfileImageCountryName);
        } else  if (theExtraFromProfileImageCountryName != null && theExtraFromProfileImageFullName != null) {
            countryName.setText(theExtraFromProfileImageCountryName);
            fullName.setText(theExtraFromProfileImageFullName);
        } else if (theExtraFromProfileImageCountryName != null && theExtraFromProfileImageUserName != null) {
            countryName.setText(theExtraFromProfileImageCountryName);
            userName.setText(theExtraFromProfileImageUserName);
        } else  if (theExtraFromProfileImageUserName != null && theExtraFromProfileImageFullName!= null){
            fullName.setText(theExtraFromProfileImageFullName);
            userName.setText(theExtraFromProfileImageUserName);
        }


        // now in this case for the button we will commit a setOnClickListener rether than a function with a view
        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAccountSetUpInformation();
            }
        });


        // so now we will here create the basis for altering the profile image
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // so what we will want to do is when the profile image is clicked send the user to there own gallery to pick out a photo
                // in that regard we would have to do so by way of an intent
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                // so this is where in the phon we specify that the image can be gotten from (in this case we will get it from images to make sure that the person is real)
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,1);
            }


        });



        // so now we will create the foundation for actually replacing the empty image with the chosen picture

        // so to do this we will obviously get the database reference
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // first we will of course check if dataSnapshot exists i.e. if an image was saved to firebase
                if(dataSnapshot.exists()) {
                    try {

                        // so here we will also go about creating a validation system
                        if (dataSnapshot.hasChild("Profile Image")) {
                            // so if it exists we will extract the image from our database
                            String image = dataSnapshot.child("Profile Image").getValue(String.class);

                            userProfileImageRef.getDownloadUrl();


                            // now to properly cache the image we would need to utilise an external library
                            Picasso.get().load(image).placeholder(R.drawable.empty).into(profileImage);
                            Log.i("Things",image);
                        } else {
                            Toast.makeText(setUpActivity.this, "Profile Image does not exist!", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Log.i("Do","Nothing");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    // but we are also going to want to crop pur image if it is to large this is done by searching for the requestcode by way of a onActivityResult method
    // the activityresult therefore picks up on an activity such as a crop!
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data!=null) {
            // so first we will set the boolean to true
            anotherTing = true;
            // so in this case we get the uri which is essentially just the link to the gallery
            Uri imageUri = data.getData();
            Log.i("It is",imageUri.toString());

            // and here is were we add the cropping functionality!
            // this is gotten to by way of an external library
            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);
            
        }

        // so this will now be to get access to the chosen image so that we can later on save to firebase
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            anotherTing =  true;
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                // and here we will do a progress dialog
                theProgressDialog.setTitle("Profile Image");
                theProgressDialog.setMessage("Please wait awhile we crop your profile image!");
                theProgressDialog.show();
                theProgressDialog.setCanceledOnTouchOutside(true);
                Log.i("Something","1");
                // so now we get the uri of the specific image that we cropped!
                Uri resultUri = result.getUri();

                // so we create another storagereference to put within our main one!
                final StorageReference filePath = userProfileImageRef.child(currentUserId + ".jpg");


                // then we check if the image checker was in fact a success
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // if a success we get the url and then save it in firebase
                                String downloadUrl = uri.toString();
                                usersRef.child("Profile Image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // then in the case were the theExtra is null i.e. when we never got to main activity we will set the allower to be allowed
                                            if (theExtra == null) {
                                                theAllower = true;
                                            }

                                            // then if the info is not empty we will go about getting an extra
                                            String theUserName = userName.getText().toString();
                                            String theFullName = fullName.getText().toString();
                                            String theCountryName = countryName.getText().toString();

                                            // so now depending on which are currently available we put the extra's
                                            if (!TextUtils.isEmpty(theUserName) || !TextUtils.isEmpty(theFullName) || !TextUtils.isEmpty(theCountryName) || !yetAnotherThing) {
                                                Intent selfIntent = new Intent(setUpActivity.this,setUpActivity.class);
                                                if (!TextUtils.isEmpty(theUserName) && !TextUtils.isEmpty(theFullName) && !TextUtils.isEmpty(theCountryName)) {
                                                    selfIntent.putExtra("username",theUserName);
                                                    selfIntent.putExtra("fullname",theFullName);
                                                    selfIntent.putExtra("countryname",theCountryName);
                                                    selfIntent.putExtra("theBoolean",true);
                                                } else if (!TextUtils.isEmpty(theUserName) && !TextUtils.isEmpty(theFullName)) {
                                                    selfIntent.putExtra("username",theUserName);
                                                    selfIntent.putExtra("fullname",theFullName);
                                                    selfIntent.putExtra("theBoolean",true);
                                                } else  if (!TextUtils.isEmpty(theUserName) && !TextUtils.isEmpty(theCountryName)) {
                                                    selfIntent.putExtra("username",theUserName);
                                                    selfIntent.putExtra("countryname",theCountryName);
                                                    selfIntent.putExtra("theBoolean",true);
                                                } else  if (!TextUtils.isEmpty(theFullName) && !TextUtils.isEmpty(theCountryName)) {
                                                    selfIntent.putExtra("fullname",theFullName);
                                                    selfIntent.putExtra("countryname",theCountryName);
                                                    selfIntent.putExtra("theBoolean",true);
                                                } else {
                                                    selfIntent.putExtra("theBoolean",true);
                                                }
                                                startActivity(selfIntent);
                                            }


                                            Toast.makeText(setUpActivity.this, "Profile image stored to the database successfully", Toast.LENGTH_SHORT).show();
                                            // and if the task is successful we will remove the ladingbar
                                            theProgressDialog.dismiss();
                                        } else {
                                            Toast.makeText(setUpActivity.this, "There was some sort of an error here!", Toast.LENGTH_SHORT).show();
                                            theProgressDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });

            } else {
                Toast.makeText(this, "Error occurred!", Toast.LENGTH_SHORT).show();
            }




        }

    }

    private void saveAccountSetUpInformation() {
        String theUserName = userName.getText().toString();
        String theFullName = fullName.getText().toString();
        String theCountryName = countryName.getText().toString();

        // in addition we will also want to send the user to the mainactivity i the userprofile has already been created!
        if (TextUtils.isEmpty(theUserName) || TextUtils.isEmpty(theFullName) || TextUtils.isEmpty(theCountryName) || !yetAnotherThing) {
            Log.i("Username",Boolean.toString(TextUtils.isEmpty(theUserName)));
            Toast.makeText(this, "You need to fill out all fields and provide a profile image!!", Toast.LENGTH_SHORT).show();
        } else {
            // and we will also create a progressbar for it
            theProgressDialog.setTitle("Setting up your Information");
            theProgressDialog.setMessage("Please wait a moment before you can begin!");
            theProgressDialog.show();
            theProgressDialog.setCanceledOnTouchOutside(true);

            // so here we will create a hashmap to store all the values and their "keys"
            HashMap userMap = new HashMap();
            userMap.put("Username",theUserName);
            userMap.put("Full name",theFullName);
            userMap.put("Country",theCountryName);
            // these will be added later to settings
            userMap.put("Status","Good");
            userMap.put("Gender","None");
            userMap.put("DOB","mm/dd/yyyy");
            userMap.put("Relationship Status","None");



            // now we will store all the info in the firebase database
            // we will also place an oncomplete listener to determine whether the task was a success or not!
            // (also note to give permission to access the intenet within the manifest!)
            usersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        sendUserToMainActivity();

                        Toast.makeText(setUpActivity.this, "Your account is created sucessfully!", Toast.LENGTH_LONG).show();

                        // then only after the task is concluded (both ways) do we dismiss the progress bar
                        theProgressDialog.dismiss();
                    } else {
                        String errorMessage = task.getException().getMessage();
                        Log.i("It is",errorMessage);
                        Toast.makeText(setUpActivity.this, "There was some sort of an error here!", Toast.LENGTH_SHORT).show();
                        theProgressDialog.dismiss();
                    }
                }
            });
        }

    }



    private void sendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(setUpActivity.this,MainActivity.class);
        // so what this does is clear out everything done in setupactivity
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        finish();
    }
}