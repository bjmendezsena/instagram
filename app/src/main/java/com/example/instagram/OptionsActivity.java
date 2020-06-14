package com.example.instagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class OptionsActivity extends AppCompatActivity {

    TextView logout, settings;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        inicializeElements();


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Opciones");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prepareListener();

    }

    private void prepareListener() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                sendToStartActivity();
            }
        });
    }

    private void sendToStartActivity() {
        Intent intentStart = new Intent(OptionsActivity.this, StartActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentStart);

    }

    private void inicializeElements() {
        logout = findViewById(R.id.logout_options);
        settings = findViewById(R.id.settings_options);
        toolbar = findViewById(R.id.toolbar_options);
    }
}
