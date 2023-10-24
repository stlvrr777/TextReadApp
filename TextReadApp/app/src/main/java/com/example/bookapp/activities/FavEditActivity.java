package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityFavEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class FavEditActivity extends AppCompatActivity {

    private ActivityFavEditBinding binding;
    private ProgressDialog progressDialog;
    private String selectedLangId = "en", selectedLang = " ", favwords, translatedFavwords = "";
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "DOC_VIEW_TAG";
    public static String transText = "\"";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите...");
        progressDialog.setCanceledOnTouchOutside(false);

        loadUserInfo();


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FavEditActivity.this);
                builder.setTitle("Сохранить изменения?");
                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Действия при выборе "Да"
                        firebaseAuth = FirebaseAuth.getInstance();
                        progressDialog.setMessage("Словарь обновляется...");
                        progressDialog.show();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("favwords", ""+binding.fbViewTv.getText().toString());
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                        databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                Toast.makeText(FavEditActivity.this, "Словарь обновлен", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: failed update to db "+ e.getMessage());
                                progressDialog.dismiss();
                                Toast.makeText(FavEditActivity.this, "Не удалось обновить словарь"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        //onBackPressed();
                        startActivity(new Intent(FavEditActivity.this, activity_fav_words.class ));
                    }
                });
                builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Действия при выборе "Нет"
                        onBackPressed();
                        //startActivity(new Intent(FavEditActivity.this, activity_fav_words.class ));
                    }
                });
                builder.setNeutralButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Действия при выборе "Отмена"
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void loadUserInfo() {
        firebaseAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "loadUserInfo: Loading user info of user" + firebaseAuth.getUid());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favwords = ""+snapshot.child("favwords").getValue();

                binding.fbViewTv.setText(favwords);
                binding.progressBar.setVisibility(View.GONE);
                binding.progressTv.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}