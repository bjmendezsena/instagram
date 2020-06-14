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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //Componentes del XML
    EditText userName, fullName, email, password;
    Button btRegister;
    TextView txt_login;
    View.OnClickListener listener;

    //Componentes de Base de datos de FireBase
    FirebaseAuth auth;
    DatabaseReference reference;
    ProgressDialog pd;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inicializeElements();
        prepareListener();

    }


    private void inicializeElements() {
        userName = (EditText) findViewById(R.id.etUsername);
        fullName = (EditText) findViewById(R.id.etFullname);
        email = (EditText) findViewById(R.id.etEmail);
        password = (EditText) findViewById(R.id.etPassword);
        btRegister = (Button) findViewById(R.id.btRegisterReg);
        txt_login = (TextView) findViewById(R.id.txt_login);
        auth = FirebaseAuth.getInstance();
    }

    private void prepareListener() {
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.txt_login:
                        sendToLoginActivity();
                        break;
                    case R.id.btRegisterReg:
                        pd = new ProgressDialog(RegisterActivity.this);
                        pd.setMessage("Por favor, epere...");
                        pd.show();
                        cargarDatos();
                        break;
                }
            }
        };
        txt_login.setOnClickListener(listener);
        btRegister.setOnClickListener(listener);


    }

    private void cargarDatos() {
        String str_userName = userName.getText().toString();
        String str_fullName = fullName.getText().toString();
        String str_email = email.getText().toString();
        String str_password = password.getText().toString();

        if(TextUtils.isEmpty(str_userName)){
            toastMessage("Tiene que insertar el nombre de usuario");
        }else if(TextUtils.isEmpty(str_fullName)){
            toastMessage("Tiene que insertar el nombre completo");
        }else if(TextUtils.isEmpty(str_email) ){
            toastMessage("Tiene que insertar la contraseña");
        }else if(TextUtils.isEmpty(str_password)){
            toastMessage("Tiene que insertar la contraseña");
        }else if (str_password.length() < 6){
            toastMessage("a contraseña tiene que tener 6 carácteres como mínimo");
        }else{
            register(str_userName, str_fullName, str_email, str_password);
        }
    }

    private void register(final String str_userName, final String str_fullName, String str_email, String str_password) {

            auth.createUserWithEmailAndPassword(str_email, str_password)
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                String userid = firebaseUser.getUid();
                                reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

                                HashMap<String, Object> hashMap = new HashMap<>();

                                hashMap.put("id", userid);
                                hashMap.put("username", str_userName.toLowerCase());
                                hashMap.put("fullname", str_fullName.toLowerCase());
                                hashMap.put("bio", "");
                                hashMap.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/instagramdatabase-afd09.appspot.com/o/icono_usuario.png?alt=media&token=f7bb6342-e0b8-463a-b05b-23698d4a2112");

                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            pd.dismiss();
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                            } else {
                                pd.dismiss();
                                toastMessage("No puedes registrarte con este correo electrónico");
                            }
                        }
                    });

    }

    private void toastMessage(String msg) {
        Toast.makeText(RegisterActivity.this,msg , Toast.LENGTH_SHORT).show();
    }

    private void sendToLoginActivity() {
        Intent loginIntent= new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

}
