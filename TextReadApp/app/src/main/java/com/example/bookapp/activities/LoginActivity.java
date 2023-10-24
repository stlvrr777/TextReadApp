package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите...");
        progressDialog.setCanceledOnTouchOutside(false);


        //перейти к регистрации
        binding.noAccTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
                
            }
        });
    }

    private String email, pass;
    private void validateData() {
        email = binding.emailEt.getText().toString().trim();
        pass = binding.passwordEt.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Некорректный адрес электронной почты", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(pass)){
            Toast.makeText(this,"Введите пароль", Toast.LENGTH_SHORT).show();
        }
        else {
            loginUser();
        }


    }

    private void loginUser() {
        progressDialog.setMessage("Вход...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                checkUser();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this," "+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void checkUser() {
        progressDialog.setMessage("Выполняется вход...");
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressDialog.dismiss();
                String userType = ""+snapshot.child("userType").getValue();
                if (userType.equals("user")){
                    String role = "user";
                    Intent intent = new Intent(LoginActivity.this, DashboardUserActivity.class);
                    //intent.putExtra("role", role);
                    startActivity(intent);
                    finish();

                }
                else if (userType.equals("admin")){
                    String role = "admin";
                    Intent intent = new Intent(LoginActivity.this, DashboardAdminActivity.class);
                    //intent.putExtra("role", role);
                    startActivity(intent);
                    finish();

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}