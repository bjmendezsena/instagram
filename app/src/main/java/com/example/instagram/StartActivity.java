package com.example.instagram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    View.OnClickListener listener;


    //Elementos del xml
    Button login, register;

    //Elementos de firebase
    FirebaseUser firebaseUser;


    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        instantiateElements();

        prepareToListener();

        addElementstoListener();


    }

    private void addElementstoListener() {
        login.setOnClickListener(listener);
        register.setOnClickListener(listener);
    }


    private void prepareToListener() {
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.bLoginStart:
                        sendToLoginActivity();
                        break;
                    case  R.id.bRegisterStart:
                        sendToRegisterActivity();
                }
            }
        };

    }

    private void sendToLoginActivity() {
        Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
        startActivity(loginIntent);

    }

    private void instantiateElements() {
        login = (Button) findViewById(R.id.bLoginStart);
        register = (Button) findViewById(R.id.bRegisterStart);
    }

    private void sendToRegisterActivity() {
        Intent registerIntent = new Intent(StartActivity.this, RegisterActivity.class);
        startActivity(registerIntent);

    }
}
