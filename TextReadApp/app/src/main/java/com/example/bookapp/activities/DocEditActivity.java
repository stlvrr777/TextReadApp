package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DocEditActivity extends AppCompatActivity {

    private ActivityPdfEditBinding binding;

    private String bookId;

    private ProgressDialog progressDialog;

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    private static final String TAG = "BOOK_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите...");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        loadBookInfo();

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();

            }
        });
    }

    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: загружаетсяя инфо о книге...");

        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                selectedCategoryId = ""+snapshot.child("categoryId").getValue();
                String description = ""+snapshot.child("description").getValue();
                String title = ""+snapshot.child("title").getValue();

                binding.titleEt.setText(title);
                binding.descriptionEt.setText(description);

                Log.d(TAG, "onDataChange: загружается инфо о категории книги");
                DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                refBookCategory.child(selectedCategoryId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();
                        binding.categoryTv.setText(category);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private String title ="", description = "";

    private void validateData(){
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        if(TextUtils.isEmpty(title) | TextUtils.isEmpty(description)){
            Toast.makeText(this, "Проверьте правильность введенных вами данных.", Toast.LENGTH_SHORT).show();

        }
        else if (TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, "Выберите категорию.", Toast.LENGTH_SHORT).show();
        }
        else {
            updatePdf();
        }
    }

    private void updatePdf() {
        Log.d(TAG, "updatePdf: инфо обновляется в бд...");
        progressDialog.setMessage("Обновляется информация о книге...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: книга загружена");
                progressDialog.dismiss();
                Toast.makeText(DocEditActivity.this, "Информация о книге обновлена.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: ошибка при обновлении в бд"+e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(DocEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });



    }

    private String selectedCategoryId="", selectedCategoryTitle="";

    private void categoryDialog(){
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i<categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);

        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите категорию").setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedCategoryId = categoryIdArrayList.get(which);
                selectedCategoryTitle = categoryTitleArrayList.get(which);

                binding.categoryTv.setText(selectedCategoryTitle);
            }
        }).show();

    }
    
    private void loadCategories() {
        Log.d(TAG, "loadCategories: загружаются категории");
        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String id = ""+ds.child("id").getValue();
                    String category = ""+ds.child("category").getValue();
                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);

                    Log.d(TAG, "onDataChange: id: "+id);
                    Log.d(TAG, "onDataChange: Category: "+category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}