package com.example.socialnetworkingapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    NavigationView navigationView;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    RecyclerView recyclerView;
    Toolbar myToolBar;
    FirebaseAuth mAuth;
    DatabaseReference usersRef, postsRef;

    // so now we will create a variable for the imagebutton
    ImageButton addNewPost;

    // and these will now be to pick up the profile image and username to create the picture and the name of the user
    CircleImageView navProfileImage;
    TextView profileUserName;

    // now this will be to determine the current user
    String currentUser;

    String imageUri;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myToolBar = (Toolbar) findViewById(R.id.main_page_toolbar);

        // now we will give a value to the image button
        addNewPost = (ImageButton) findViewById(R.id.addNewPost);

        imageUri = getIntent().getStringExtra("the image uri");

        try {
            Log.i("The uri is",imageUri);
        } catch (Exception e) {

        }


        mAuth = FirebaseAuth.getInstance();
        // so notice that for the 2 different children in database we have seperate references
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        // and now here we officially obtain the current user
        currentUser = mAuth.getCurrentUser().getUid();

        Log.i("50 cent",currentUser);



        //myToolBar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(myToolBar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = (NavigationView) findViewById(R.id.navigationView);


        // now we will go about giving a value to the recyclerView which will allow us to display posts
        recyclerView = (RecyclerView) findViewById(R.id.all_users_posts_list);
        // and we will then perform a fixed size
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        // now we will make sure that the newest posts are first and the older ones are last
        linearLayoutManager.setReverseLayout(true);

        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);






        // so this will now be to show the profile as per navigation_header
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);

        // now we wil create values for our profileimage & username (remember the id here is at navigation_header.xml)
        // without navView. the app will crash because it ould not have access to the navigation_header
        navProfileImage = (CircleImageView) navView.findViewById(R.id.navProfileImage);
        profileUserName = (TextView) navView.findViewById(R.id.navUser);

        // now we will go about getting the reference
        usersRef.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // now what we are going to do here is important because it is what is known as a validation
                    // what you will want to do is first check if the child exists i.e. that it is not null and in turn set the text
                    if (dataSnapshot.hasChild("Full name")) {
                        // so now we will show the name
                        String theName = dataSnapshot.child("Full name").getValue().toString();
                        // and now we will display it on the navigationheader
                        profileUserName.setText(theName);
                    } else {
                        Toast.makeText(MainActivity.this, "Profile name does not exist!", Toast.LENGTH_SHORT).show();
                    }
                    // then we will also do the same thing for the profile image
                    if (dataSnapshot.hasChild("Profile Image")) {
                        // so now we will show the image
                        String theImage = dataSnapshot.child("Profile Image").getValue().toString();
                        // and finally we will display the picture (through picasso of course)
                        Picasso.get().load(theImage).placeholder(R.drawable.empty).into(navProfileImage); // with a default of the empty image
                    } else {
                        Toast.makeText(MainActivity.this, "Profile image does not exist!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // now we will set an onClickListener for the imageview
        addNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // so when it is clicked we will send it to the class that is concerned with creating a new post
                sendUserToPostActivity();
            }
        });


        displayAllUsersPost();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });

    }


    // so this will be vailidating each user as per the app opening (that is if they are logged/registered or not)
    @Override
    protected void onStart() {
        super.onStart();

        // so first we get the user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // so if there is no user he will have to gign in thus we send them to the signin
        if (currentUser == null) {
            sendToLogIn();
        } else {
            Log.i("Test","Two");
            // this will be done to check if the user has in fact been authenticated but is not available at the realtime database!
            checkUserExistence();
        }

        /*
        FirebaseRecyclerAdapter<Posts,PostsViewHolder> adapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>() {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {

            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                PostsViewHolder viewHolder = new PostsViewHolder(view);
                return viewHolder;
            }
        };

        */

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void checkUserExistence() {
        Log.i("Test","Four");
        // so first we will go about finding the userId
        final String userIdCurrent  = mAuth.getCurrentUser().getUid();
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                // so then we will want to check that the profileimage and the infor was placed in so if there are more than 1 values you are good
                // beacuse we would then have 1 + 3
                if ((int)dataSnapshot.child(userIdCurrent).getChildrenCount() <= 1) {
                    // so of course what we are going to want to send to setup activity
                    Log.i("Meto",Integer.toString((int)dataSnapshot.child(userIdCurrent).getChildrenCount()));
                    Log.i("Jeto",userIdCurrent);
                    sendUserToSetUpActivity();

                    // so in the case were there is just one child that will be the profile image
                    // in this case we will pass in an intent to signify within the setupactivity that a image is present
                    if ((int)dataSnapshot.child(userIdCurrent).getChildrenCount() == 1) {
                        Intent shiftingIntent = new Intent(MainActivity.this,setUpActivity.class);
                        shiftingIntent.putExtra("allower","allowed");
                        startActivity(shiftingIntent);
                    }

                } else {
                    Log.i("Meo",Integer.toString((int)dataSnapshot.child(userIdCurrent).getChildrenCount()));
                    Log.i("Test","Three");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToSetUpActivity() {
        Intent otherIntent = new Intent(MainActivity.this,setUpActivity.class);
        otherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(otherIntent);
        finish();
    }


    private void sendToLogIn() {
        Intent intent = new Intent(MainActivity.this,logInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void sendUserToPostActivity() {
        Intent postActivityIntent = new Intent(MainActivity.this,postActivity.class);
        startActivity(postActivityIntent);
    }


    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_post:
                sendUserToPostActivity();
                break;
            case R.id.nav_profile:
                Toast.makeText(this, "It is working! - The profile!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "It is working! - The home!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Toast.makeText(this, "It is working! - The friends!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_friends:
                Toast.makeText(this, "It is working! - The find friends!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "It is working! - The messages!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                Intent sendTo= new Intent(MainActivity.this,settingsActivity.class);
                startActivity(sendTo);
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                // so now we will send the user back to the login
                sendToLogIn();
                break;

        }
    }


    public void displayAllUsersPost() {
        // so here is were thank to the recycler we can retrieve all the posts from firebase!
        // were note that there was a dependency that we placed in build.gradle
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(postsRef,Posts.class).build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Posts,PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder viewHolder, int position, @NonNull Posts model) {

                // so this is now to get the key for future editing/deletion
                final String postKey = getRef(position).getKey();

                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
                viewHolder.setFullName(model.getFullname());
                try {
                    viewHolder.setContents(model.getContents());
                } catch (Exception e) {
                    // so nothing
                }
                try {
                    viewHolder.setPostImage(model.getPostImage());
                } catch (Exception e) {
                   // do nothing
                }
                viewHolder.setProfileImage(model.getProfileImage());

                // and now very importantly here we will create an onclicklistener to send us to clickPostActivity
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent clickPostIntent = new Intent(MainActivity.this,clickPostActivity.class);
                        // and importantly we will also go about sending our postkey
                        Log.i("Za post key is",postKey);
                        clickPostIntent.putExtra("Post Key",postKey);
                        startActivity(clickPostIntent);
                    }
                });

            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
                PostsViewHolder viewHolder = new PostsViewHolder(v);
                return viewHolder;
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }



    // now we have to create a static class for the FirebaseRecyclerAdapter

    // IF ERROR OCCURRS CHANGE THIS TO STATIC
    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        View mView;



        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date) {
            TextView postDate = (TextView) mView.findViewById(R.id.textTwo);
            postDate.setText(date);
        }

        public void setTime(String time) {
            TextView postTime = (TextView) mView.findViewById(R.id.textThree);
            postTime.setText(time);
        }

        public void setFullName(String fullName) {
            TextView userName = (TextView) mView.findViewById(R.id.postUserName);
            userName.setText(fullName);
        }

        public void setProfileImage(String profileImage) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.postProfileImage);
            Picasso.get().load(profileImage).into(image);
        }


        public void setContents(String contents) {
            try {
                TextView postContents = (TextView) mView.findViewById(R.id.postDescription);
                postContents.setText(contents);
            } catch (Exception e) {
            }
        }

        public void setPostImage(String postImage) {

            ImageView imageTwo = (ImageView) mView.findViewById(R.id.postImageView);




            if (imageTwo != null) {

                Picasso.get().load(postImage).into(imageTwo);
            }

        }






    }

}