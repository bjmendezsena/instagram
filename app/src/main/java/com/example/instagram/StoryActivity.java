package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagram.Model.Story;
import com.example.instagram.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {


    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;
    StoriesProgressView storiesProgressView;
    ImageView image, story_photo, story_delete;
    TextView story_username, seen_number;
    View skip, reverse;

    LinearLayout r_seen;

    List<String> images;
    List<String> storyids;
    String userid;
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;

                case MotionEvent.ACTION_UP:
                        long now = System.currentTimeMillis();
                        storiesProgressView.resume();
                        return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);


        inicializeElements();
        r_seen.setVisibility(View.GONE);
        story_delete.setVisibility(View.GONE);
        userid = getIntent().getStringExtra("userid");


        if(userid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            r_seen.setVisibility(View.VISIBLE);
            story_delete.setVisibility(View.VISIBLE);
        }


        getStories(userid);
        userInfo(userid);
        prepareListener();

    }

    private void prepareListener() {
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);


        skip = findViewById(R.id.skip_storyAct);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);

        r_seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendToFollowersActivity();
            }
        });

        story_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                        .child(userid).child(storyids.get(counter));
                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            sendMessage("Elimindo");
                            finish();
                        }
                    }
                });
            }
        });
    }

    private void sendMessage(String msg) {
        Toast.makeText(StoryActivity.this, msg, Toast.LENGTH_SHORT);
    }

    private void SendToFollowersActivity() {
        Intent intentFollowers = new Intent(StoryActivity.this, FollowersActivity.class);
        intentFollowers.putExtra("id", userid);
        intentFollowers.putExtra("storyid", storyids.get(counter));
        intentFollowers.putExtra("title", "Visitas");
        startActivity(intentFollowers);
    }


    private void inicializeElements() {
        r_seen = findViewById(R.id.r_seen_storyAct);
        seen_number = findViewById(R.id.seen_number);
        story_delete = findViewById(R.id.story_delete);
        storiesProgressView = findViewById(R.id.stories);
        image = findViewById(R.id.image_StoryAct);
        story_photo = findViewById(R.id.story_photo_storyAct);
        story_username = findViewById(R.id.story_username_storyAct);
        reverse = findViewById(R.id.reverse_storyAct);
    }

    @Override
    public void onNext() {
        Glide.with(getApplicationContext()).load(images.get(++counter)).into(image);

        addViews(storyids.get(counter));
        seenNumber(storyids.get(counter));
    }

    @Override
    public void onPrev() {

        if((counter - 1) < 0) return;;
        Glide.with(getApplicationContext()).load(images.get(--counter)).into(image);
        seenNumber(storyids.get(counter));
    }

    @Override
    public void onComplete() {

        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    private void getStories(String userid){
        images = new ArrayList<>();
        storyids = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                images.clear();
                storyids.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);
                    long  timecurrent = System.currentTimeMillis();
                    if(timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){
                        images.add(story.getImageurl());
                        storyids.add(story.getStoryid());
                    }
                }
                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);

                Glide.with(getApplicationContext()).load(images.get(counter))
                        .into(image);

                addViews(storyids.get(counter));
                seenNumber(storyids.get(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void userInfo(final String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(story_photo);
                story_username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addViews(String storyid){
        FirebaseDatabase.getInstance().getReference("Story").child(userid)
                .child(storyid).child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
    }

    private void seenNumber(String storyid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid).child(storyid).child("views");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String seenNumber = Long.toString(dataSnapshot.getChildrenCount());
                seen_number.setText("Visto por "+seenNumber);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
