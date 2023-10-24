package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.bookapp.Translate.ApiGetTask;
import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.adapters.AdapterPdfFavourite;
import com.example.bookapp.models.ModelPdf;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfFavourite adapterPdfFavourite;
    public static String apiKey = "\"";
    private static String TAG = "PROFILE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();
        loadFavouriteBooks();

        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class ));
            }
        });

        binding.favWordsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, activity_fav_words.class ));
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadFavouriteBooks(){
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favourites").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    String bookId = ""+ds.child("bookId").getValue();

                    ModelPdf modelPdf = new ModelPdf();
                    modelPdf.setId(bookId);

                    pdfArrayList.add(modelPdf);
                }

                binding.favouriteBookCountTv.setText(""+pdfArrayList.size());
                adapterPdfFavourite = new AdapterPdfFavourite(ProfileActivity.this, pdfArrayList);
                binding.booksRv.setAdapter(adapterPdfFavourite);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfo(){
        Log.d(TAG, "loadUserInfo: Loading user info of user" + firebaseAuth.getUid());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = ""+snapshot.child("email").getValue();
                String name = ""+snapshot.child("name").getValue();
                String profileImage = ""+snapshot.child("profileImage").getValue();
                String timestamp = ""+snapshot.child("timestamp").getValue();//timestamp
                String uid = ""+snapshot.child("uid").getValue();
                String userType = ""+snapshot.child("userType").getValue();

                String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));
                binding.emailTv.setText(email);
                binding.nameTv.setText(name);
                binding.memberDateTv.setText(formattedDate);
                binding.accountTypeTv.setText(userType);

                if (!isDestroyed()) {
                    // Ваш вызов Glide.with() и операции загрузки изображения
                    Glide.with(ProfileActivity.this).load(profileImage).placeholder(R.drawable.ic_person_gray).into(binding.profileIv);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ApiGetTask task = new ApiGetTask();
        task.execute();
        while (apiKey == "\""){
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}