package com.example.instagram.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.instagram.Adapter.MyFotoAdapter;
import com.example.instagram.EditProfileActivity;
import com.example.instagram.FollowersActivity;
import com.example.instagram.LoginActivity;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.User;
import com.example.instagram.OptionsActivity;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {


    ImageView image_profile, optios;
    TextView posts, followers, following, fullname, bio, username, texto_seguidores, texto_seguidos;
    Button edit_profile;

    private List<String> mySaves;

    RecyclerView recyclerView_saves;
    MyFotoAdapter myFotoAdapter_saves;
    List<Post> postList_saves;

    RecyclerView recyclerView;
    MyFotoAdapter myFotoAdapter;
    List<Post> postList;


    FirebaseUser firebaseUser;
    String profileid;

    ImageButton my_fotos, saved_fotos;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences preferences = getContext().getSharedPreferences("PREPS", Context.MODE_PRIVATE);
        profileid = preferences.getString("profileid", "none");


        inicializeElements(view);

        recyclerView.setVisibility(View.VISIBLE);
        recyclerView_saves.setVisibility(View.GONE);

        userInfo();
        getFollowers();
        getNrPosts();
        myFotos();
        mySaves();

        if(profileid.equals(firebaseUser.getUid())){
            edit_profile.setText("Editar perfil");
        }else{
            checkFollow();
            saved_fotos.setVisibility(View.GONE);
        }

        prepareToListener();



        return  view;
    }

    private void addNotifications(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileid  );

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "Ha comenzado a seguirte");
        hashMap.put("postid","");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }

    private void prepareToListener() {
        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btn = edit_profile.getText().toString();

                if(btn.equals("Editar perfil")){
                    sendToEditProfileActivity();
                }else  if(btn.equals("Seguir")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    addNotifications();

                }else if(btn.equals("Siguiendo")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        optios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), OptionsActivity.class);
                startActivity(intent);
            }
        });

        my_fotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView_saves.setVisibility(View.GONE);
            }
        });

        saved_fotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                recyclerView_saves.setVisibility(View.VISIBLE);
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "Seguidores");
                startActivity(intent);
            }
        });

        texto_seguidores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "Seguidores");
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "Siguiendo");
                startActivity(intent);
            }
        });
        texto_seguidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "Siguiendo");
                startActivity(intent);
            }
        });
    }

    private void inicializeElements(View view) {
        image_profile = view.findViewById(R.id.image_profile_profileFragment);
        optios = view.findViewById(R.id.options_profileFragment);
        posts = view.findViewById(R.id.posts_profileFragment);
        followers = view.findViewById(R.id.followers_profileFragment);
        following = view.findViewById(R.id.following_profileFragment);
        fullname = view.findViewById(R.id.fullname_profileFragment);
        bio = view.findViewById(R.id.bio_profileFragment);
        username = view.findViewById(R.id.username_profileFragment);
        my_fotos = view.findViewById(R.id.my_fotos);
        edit_profile = view.findViewById(R.id.bt_edit_profile);
        saved_fotos = view.findViewById(R.id.saved_fotos);
        texto_seguidos = view.findViewById(R.id.texto_seguidos);
        texto_seguidores = view.findViewById(R.id.texto_seguidores);

        recyclerView = view.findViewById(R.id.recicler_view_profileFragment);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(),3);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        myFotoAdapter = new MyFotoAdapter(getContext(), postList);
        recyclerView.setAdapter(myFotoAdapter);

        recyclerView_saves = view.findViewById(R.id.recicler_view_saved_profileFragment);
        recyclerView_saves.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager_saves = new GridLayoutManager(getContext(),3);
        recyclerView_saves.setLayoutManager(linearLayoutManager_saves);
        postList_saves = new ArrayList<>();
        myFotoAdapter_saves = new MyFotoAdapter(getContext(), postList_saves);
        recyclerView_saves.setAdapter(myFotoAdapter_saves);
    }

    private void sendToEditProfileActivity() {
        Intent intentEditProfile = new Intent(this.getContext(), EditProfileActivity.class);
        startActivity(intentEditProfile);
    }



    private void userInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(getContext() == null){
                    return;
                }

                User user = dataSnapshot.getValue(User.class);

                Glide.with(getContext()).load(user.getImageurl()).into(image_profile);
                username.setText(user.getUsername());
                fullname.setText(user.getFullname());
                bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkFollow(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(profileid).exists()){
                    edit_profile.setText("Siguiendo");
                }else {
                    edit_profile.setText("Seguir");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowers(){
        DatabaseReference referenceFollowers = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileid).child("followers");

        referenceFollowers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followers.setText("      "+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference referenceFollowing = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileid).child("following");

        referenceFollowing.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                following.setText("      "+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getNrPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if(post.getPublisher().equals(profileid)){
                        i++;
                    }
                }
                posts.setText("      "+i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void myFotos(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if(post.getPublisher().equals(profileid)){
                        postList.add(post);
                    }
                }
                Collections.reverse(postList);
                myFotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void mySaves(){
        mySaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    mySaves.add(snapshot.getKey());
                }

                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readSaves() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList_saves.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);

                    for(String id : mySaves){
                        if(post.getPostid().equals(id)){
                            postList_saves.add(post);
                        }
                    }
                }
                myFotoAdapter_saves.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
