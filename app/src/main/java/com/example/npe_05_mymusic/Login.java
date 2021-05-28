package com.example.npe_05_mymusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    private TextView etEmail, etPassword;
    private DatabaseReference reference;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private  FirebaseAuth.AuthStateListener authStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        etEmail = (TextView)findViewById(R.id.email);
        reference = FirebaseDatabase.getInstance().getReference("User");
        etPassword = (TextView) findViewById(R.id.password);

        btnLogin = (Button)findViewById(R.id.login);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser u = Login.this.mAuth.getCurrentUser();
                if (u != null) {
                    startActivity(new Intent(Login.this,Home.class));
                }
            }
        };

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }
        });

    }

    private void userLogin() {

        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();


        if (email.isEmpty()){
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("please provide valid email");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()){
            etPassword.setError("Password is required");
            etPassword.requestFocus();
        }
        if (password.length() < 6){
            etPassword.setError("password must be 6 character");
            etPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user.isEmailVerified()){
                        Intent a = new Intent(Login.this, Home.class);
                        startActivity(a);

                    }else{
                        user.sendEmailVerification();
                        Toast.makeText(Login.this, "check your email to verified your account", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(Login.this,"failed login", Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }
}