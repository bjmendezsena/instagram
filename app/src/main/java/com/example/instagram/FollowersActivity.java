package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toolbar;

import com.example.instagram.Adapter.UserAdapter;
import com.example.instagram.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class FollowersActivity extends AppCompatActivity {

    String id;
    String title;

    List<String> idList;

    RecyclerView recyclerView;
    UserAdapter userAdapter;
    List<User> userList;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");



        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_followers);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        inicializeElements();

        switch (title){
            case "Me gusta":
                getLikes();
                break;
            case "Siguiendo":
                getFollowing();
                break;
            case "Seguidores":
                getFollowers();
                break;
            case "Visitas":
                getViews();
                break;
        }
    }

    private void getViews(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(id).child(getIntent().getStringExtra("storyid")).child("views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLikes() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes")
                .child(id);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(id).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(id).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void inicializeElements() {
        recyclerView = findViewById(R.id.recicler_view_followers);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList, false);
        recyclerView.setAdapter(userAdapter);
        idList = new ArrayList<>();
    }

    private void showUsers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (String id : idList){
                        if(user.getId().equals(id)){
                            userList.add(user);
                        }
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
