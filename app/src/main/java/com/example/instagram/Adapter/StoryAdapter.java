package com.example.instagram.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.AddStoryActivity;
import com.example.instagram.Model.Story;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.example.instagram.StoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder>{

    public Context mContext;
    public List<Story> mStories;


    public StoryAdapter(Context mContext, List<Story> mStories) {
        this.mContext = mContext;
        this.mStories = mStories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        if(i==0){
            View view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item, parent, false);
            return new ViewHolder(view);
        }else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.story_item, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Story story = mStories.get(position);

        userInfo(holder, story.getUserid(), position);

        if(holder.getAdapterPosition() != 0){
            seenStory(holder, story.getStoryid());
        }
        if(holder.getAdapterPosition() == 0){
            myStory(holder.addStory_text, holder.story_plus, false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.getAdapterPosition() == 0){
                    myStory(holder.addStory_text, holder.story_plus, true);
                }else {
                    sendToStory(story.getUserid());
                }
            }
        });

    }



    private void sendToStory(String userid) {
        Intent intentStory = new Intent(mContext, StoryActivity.class);
        intentStory.putExtra("userid", userid);
        mContext.startActivity(intentStory);
    }

    @Override
    public int getItemCount() {
        return mStories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView story_photo, story_plus, story_photo_seen;
        public TextView story_username, addStory_text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            inicializeElements(itemView);
        }

        private void inicializeElements(View itemView) {
            story_photo = itemView.findViewById(R.id.story_photo);
            story_plus = itemView.findViewById(R.id.story_plus);
            story_photo_seen = itemView.findViewById(R.id.story_photo_seen);
            story_username = itemView.findViewById(R.id.story_username);
            addStory_text = itemView.findViewById(R.id.addstory_text);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return  0;
        }
            return 1;
    }

    private void userInfo(final ViewHolder viewHolder, final String userId, final int pos){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageurl()).into(viewHolder.story_photo);
                if(pos != 0){
                    Glide.with(mContext).load(user.getImageurl()).into(viewHolder.story_photo_seen);
                    viewHolder.story_username.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void myStory(final TextView textView, final ImageView imageView, final boolean click){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timeCurrent= System.currentTimeMillis();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);
                    if(timeCurrent > story.getTimestart() && timeCurrent < story.getTimeend()){
                        count++;
                    }
                }
                if(click){
                    if(count > 0){
                        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Ver historia", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                sendToStory(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                dialog.dismiss();
                            }
                        });

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"Añadir historia", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                senToAddStoryActivity();
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    }else {
                        senToAddStoryActivity();
                    }
                }else {
                    if(count > 0){
                        textView.setText("Mi historia");
                        imageView.setVisibility(View.GONE);
                    }else {
                        textView.setText("Añadir historia");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void senToAddStoryActivity() {
        Intent intentAdStory = new Intent(mContext, AddStoryActivity.class);
        mContext.startActivity(intentAdStory);
    }

    private void seenStory(final ViewHolder viewHolder, String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(!snapshot.child("views")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .exists() && System.currentTimeMillis() < snapshot.getValue(Story.class).getTimeend()){
                        i++;
                    }
                }
                if(i > 0){
                    viewHolder.story_photo.setVisibility(View.VISIBLE);//No cambia de color
                    viewHolder.story_photo_seen.setVisibility(View.GONE);
                }else{
                    viewHolder.story_photo.setVisibility(View.GONE);
                    viewHolder.story_photo_seen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
