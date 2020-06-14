package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.instagram.Fragment.HomeFragment;
import com.example.instagram.Fragment.NotificationsFragment;
import com.example.instagram.Fragment.ProfileFragment;
import com.example.instagram.Fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializeElements();

        prepareToListener();


        Bundle intent = getIntent().getExtras();

        if(intent != null){
            String publisher = intent.getString("publisherid");

            SharedPreferences.Editor editor = getSharedPreferences("PREPS", MODE_PRIVATE).edit();
            editor.putString("publisherid", publisher);
            editor.apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();

        }else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }




    }

    private void prepareToListener() {
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
    }

    private void inicializeElements() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            selectedFragment = null;
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_search:
                            selectedFragment = null;
                            selectedFragment = new SearchFragment();
                            break;
                        case R.id.nav_add:
                            selectedFragment = null;
                            sendToPostActivity();
                            break;
                        case R.id.nav_notifications:
                            selectedFragment = null;
                            selectedFragment = new NotificationsFragment();
                            break;
                        case R.id.nav_profile:
                            selectedFragment = null;
                            SharedPreferences.Editor editor = getSharedPreferences("PREPS", MODE_PRIVATE).edit();
                            editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            editor.apply();
                            selectedFragment = new ProfileFragment();
                            break;
                    }

                    if(selectedFragment != null){
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    }

                    return true;
                }
            };

    private void sendToPostActivity() {
        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(postIntent);
    }

}
