package com.example.instagram.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.instagram.Adapter.PostAdapter;
import com.example.instagram.Adapter.StoryAdapter;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.Story;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class HomeFragment extends Fragment {

    //Elementos para las publicaciones
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    //Elementos para las historias
    private RecyclerView recyclerView_story;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;

    //Lista de usuarios seguidos
    private List<String> followingList;

    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //Instanciar elementos
        inicializeElements(view);


        //Ver si són seguidores o usuarios seguidos
        checkFollowing();

        return view;
    }

    private void inicializeElements(View view) {
        recyclerView = view.findViewById(R.id.recicler_view_home);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);

        recyclerView_story = view.findViewById(R.id.recicler_view_story);
        recyclerView_story.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManagerStory = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL,false);
        recyclerView_story.setLayoutManager(linearLayoutManagerStory);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(this.getContext(), storyList);
        recyclerView_story.setAdapter(storyAdapter);

        progressBar = view.findViewById(R.id.progress_circular_home);
    }

    private void checkFollowing(){
        followingList = new ArrayList<>();

        DatabaseReference  reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }
                readPosts();
                readStory();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList.clear();
                String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);


                    for(String id : followingList){
                        if(post.getPublisher().equals(id)){
                            postList.add(post);
                        }
                    }
                    if(post.getPublisher().equals(myId)){
                        postList.add(post);
                    }
                }

                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readStory(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long timeCurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story("", 0, 0, "", FirebaseAuth.getInstance().getCurrentUser().getUid()));
                for(String id : followingList){
                    int countStory = 0;
                    Story story = null;
                    for(DataSnapshot snapshot : dataSnapshot.child(id).getChildren()){
                        story = snapshot.getValue(Story.class);
                        if(timeCurrent > story.getTimestart() && timeCurrent < story.getTimeend()){
                            countStory++;
                        }
                    }
                    if(countStory > 0){
                        storyList.add(story);
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
