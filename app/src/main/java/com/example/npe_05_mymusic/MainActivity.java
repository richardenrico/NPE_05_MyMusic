package com.example.npe_05_mymusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etName, etEmail, etPassword;
    private Button btnSignUp;
    private TextView toLogin;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.email);
        etPassword = (EditText) findViewById(R.id.password);
        btnSignUp = (Button) findViewById(R.id.signUp);
        toLogin = (TextView) findViewById(R.id.toLogin);

        btnSignUp.setOnClickListener(this);
        toLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.toLogin:
                Intent a = new Intent(MainActivity.this, Login.class);
                startActivity(a);
                break;
            case R.id.signUp:
                final String email = etEmail.getText().toString();
                final String password = etPassword.getText().toString();
                final String fullName = etName.getText().toString();
                if (fullName.isEmpty()){
                    etName.setError("name is required");
                    etName.requestFocus();
                    return;
                }

                if (email.isEmpty()){
                    etEmail.setError("email is required");
                    etEmail.requestFocus();
                    return;
                }
                if (password.isEmpty()){
                    etPassword.setError("password is required");
                    etPassword.requestFocus();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    etEmail.setError("please provide valid email");
                    etEmail.requestFocus();
                    return;
                }
                if (password.length() < 6){
                    etPassword.setError("password should be 6 character");
                    etPassword.requestFocus();
                }

                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            User users = new User(fullName, email);
                            FirebaseDatabase.getInstance().getReference("User").
                                    child(FirebaseAuth.getInstance()
                                            .getCurrentUser()
                                            .getUid())
                                    .setValue(users)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(MainActivity.this, "success register", Toast.LENGTH_LONG).show();
                                                etEmail.setText("");
                                                etName.setText("");
                                                etPassword.setText("");
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "failed to add data in firebase", Toast.LENGTH_LONG).show();
                                }
                            });
                        }else{
                            Toast.makeText(MainActivity.this, "failed registration",Toast.LENGTH_LONG).show();


                        }
                    }
                });


                break;
        }
    }
}