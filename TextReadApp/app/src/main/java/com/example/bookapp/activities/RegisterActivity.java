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

import com.example.bookapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {


    private ActivityRegisterBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите...");
        progressDialog.setCanceledOnTouchOutside(false);

        //назад
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //регистрация
        binding.regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();

            }
        });
    }
    //проверка введенных данных
    private String name = "", email = "", pass = "";
    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        pass = binding.passwordEt.getText().toString().trim();
        String cpass = binding.cPasswordEt.getText().toString().trim();

        if (TextUtils.isEmpty(name)|TextUtils.isEmpty(pass)|TextUtils.isEmpty(cpass)){
            Toast.makeText(this,"Проверьте правильность введенных вами данных", Toast.LENGTH_SHORT).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Некорректный адрес электронной почты", Toast.LENGTH_SHORT).show();
        }
        else if (!pass.equals(cpass)){
            Toast.makeText(this,"Пароли не совпадают", Toast.LENGTH_SHORT).show();
        }
        else{
            createUserAccount();
        }

    }

    private void createUserAccount() {
        progressDialog.setMessage("Создание аккаунта...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                updateUserInfo();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this,"Не удалось создать аккаунт. Ошибка: "+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Сохранение информации...");

        long timestamp = System.currentTimeMillis();
        String uid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", "");
        hashMap.put("userType", "user");
        hashMap.put("timestamp", timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this,"Аккаунт создан!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}