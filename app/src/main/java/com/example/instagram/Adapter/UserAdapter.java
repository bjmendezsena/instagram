package com.example.instagram.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.Fragment.ProfileFragment;
import com.example.instagram.MainActivity;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

     private Context mContext;
     private List<User> mUsers;
     private boolean isFragment;


     private FirebaseUser fieBaseUser;


    public UserAdapter(Context mContext, List<User> mUsers, boolean isFragment) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isFragment = isFragment;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent,  false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( final ViewHolder viewHolder, int position) {


        fieBaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final User user = mUsers.get(position);


        viewHolder.bt_Follow.setVisibility(View.VISIBLE);


        viewHolder.userName.setText(user.getUsername());
        viewHolder.fullName.setText(user.getFullname());
        Glide.with(mContext).load(user.getImageurl()).into(viewHolder.image_profile);
        isFollowing(user.getId(), viewHolder.bt_Follow);



        if(user.getId().equals(fieBaseUser.getUid())){
            viewHolder.bt_Follow.setVisibility(View.GONE);
        }

        prepareToListener(viewHolder, user);



    }

    private void prepareToListener(final ViewHolder viewHolder, final User user) {
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFragment) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREPS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", user.getId());
                    editor.apply();

                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit();
                }else {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra("publisherid", user.getId());
                    mContext.startActivity(intent);
                }
            }
        });

        viewHolder.bt_Follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.bt_Follow.getText().toString().equals("Seguir")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(fieBaseUser.getUid())
                            .child("following").child(user.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(fieBaseUser.getUid()).setValue(true);

                    addNotifications(user.getId());

                }else{
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(fieBaseUser.getUid())
                            .child("following").child(user.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(fieBaseUser.getUid()).removeValue();
                }
            }
        });
    }

    private void addNotifications(String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", fieBaseUser.getUid());
        hashMap.put("text", "Ha comenzado a seguirte");
        hashMap.put("postid","");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView userName;
        public TextView fullName;
        public CircleImageView image_profile;
        public Button bt_Follow;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            inicializeElements(itemView);

        }

        private void inicializeElements(View itemView) {
            userName = itemView.findViewById(R.id.username);
            fullName = itemView.findViewById(R.id.fullname);
            image_profile = itemView.findViewById(R.id.image_profile);
            bt_Follow = itemView.findViewById(R.id.btn_follow);
        }
    }


    private void isFollowing(final String userId, final Button button){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(fieBaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userId).exists()){
                    button.setText("Siguiendo");
                }else {
                    button.setText("Seguir");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
