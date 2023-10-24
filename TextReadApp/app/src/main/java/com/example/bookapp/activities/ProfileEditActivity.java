package com.example.bookapp.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {

    private ActivityProfileEditBinding binding;
    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private static final String TAG = "PROFILE_EDIT_TAG";

    private Uri imageUri = null;

    private String name = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите");
        progressDialog.setCanceledOnTouchOutside(false);


        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageAttachMenu();

            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
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

                binding.nameEt.setText(name);

                Glide.with(ProfileEditActivity.this).load(profileImage).placeholder(R.drawable.ic_person_gray).into(binding.profileIv);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void validateData(){
        name = binding.nameEt.getText().toString().trim();
        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Заполните поле.", Toast.LENGTH_SHORT).show();
        }
        else {
            if(imageUri == null){
                updateProfile("");
            }
            else{
                uploadImage();
            }
        }
    }
    private void uploadImage(){
        Log.d(TAG, "uploadImage: Uploading profile image");
        progressDialog.setMessage("Обновляется Фотография Профиля...");
        progressDialog.show();

        String filePathAndName = "ProfileImages/"+firebaseAuth.getUid();

        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: profile image uploaded");
                Log.d(TAG, "onSuccess: getting url of uploaded image");
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedImageUrl = "" + uriTask.getResult();
                Log.d(TAG, "onSuccess: Uploaded Image URL: "+uploadedImageUrl);

                updateProfile(uploadedImageUrl);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: failed to upload image "+e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(ProfileEditActivity.this, "Не удалось загрузить изображение "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile(String imageUrl){
        Log.d(TAG, "updateProfile: Updating User profile");
        progressDialog.setMessage("Обновляется информация о пользователе...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", ""+name);
        if (imageUri != null){
            hashMap.put("profileImage", ""+imageUrl);
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: фото профиля обновлено");
                progressDialog.dismiss();
                Toast.makeText(ProfileEditActivity.this, "Информация профиля обновлена", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: failed update to db "+ e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(ProfileEditActivity.this, "Не удалось обновить фото профиля "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showImageAttachMenu(){
        PopupMenu popupMenu = new PopupMenu(this, binding.profileIv);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Камера");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Галерея");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int which = item.getItemId();
                if(which==0){
                    pickImageCamera();
                }
                else if(which==1){
                    pickImageGallery();
                }
                return false;

            }
        });
    }

    private void pickImageCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Новое Изображение");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Пример описания");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);


    }

    private void pickImageGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);

    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                Log.d(TAG, "onActivityResult: picked from camera"+imageUri);
                Intent data = result.getData();
                binding.profileIv.setImageURI(imageUri);
            }
            else {
                Toast.makeText(ProfileEditActivity.this, "Не удалось", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: "+imageUri);
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: picked from gallery"+imageUri);
                        binding.profileIv.setImageURI(imageUri);
                    }
                    else {
                        Toast.makeText(ProfileEditActivity.this, "Не удалось", Toast.LENGTH_SHORT).show();
                    }

                }
            });
}