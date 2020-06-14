package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    View.OnClickListener listener;


    //Componentes del xml
    EditText etEmail, etPassword;
    Button btLogin;
    TextView txt_register;

    //Componentes de Firebase
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializeElements();

        prepareToListener();

    }

    private void prepareToListener() {


        listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.tvRegister:
                        sendToRegisterActivity();
                        break;
                    case R.id.btLoginLog:
                        entrarALaApp();
                        break;
                }
            }
        };

        btLogin.setOnClickListener(listener);
        txt_register.setOnClickListener(listener);
    }

    private void entrarALaApp() {
        final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage("Entrando");
        pd.show();

        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            toastMessage("Es necesario el correo electrónico");
        }else if(TextUtils.isEmpty(password)){
            toastMessage("Es necesaria la contraseña");
        }else{
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                        .child(auth.getCurrentUser().getUid());

                                reference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        pd.dismiss();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        pd.dismiss();
                                    }
                                });
                            }else{
                                pd.dismiss();
                                toastMessage("Fallo de autentificación...");
                            }
                        }
                    });

        }
    }

    private void toastMessage(String msg) {
        Toast.makeText(LoginActivity.this,msg , Toast.LENGTH_SHORT).show();
    }

    private void sendToRegisterActivity() {
        Intent registerIntent= new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        finish();
    }

    private void inicializeElements() {
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btLogin = (Button) findViewById(R.id.btLoginLog);
        txt_register = (TextView) findViewById(R.id.tvRegister);
        auth = FirebaseAuth.getInstance();
    }
}
