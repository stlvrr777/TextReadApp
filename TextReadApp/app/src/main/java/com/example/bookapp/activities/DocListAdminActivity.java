package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.bookapp.adapters.AdapterPdfAdmin;
import com.example.bookapp.databinding.ActivityPdfListAdminBinding;
import com.example.bookapp.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DocListAdminActivity extends AppCompatActivity {

    private ActivityPdfListAdminBinding binding;
    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfAdmin adapterPdfAdmin;
    private String categoryId, categoryTitle;
    private static final String TAG = "PDF_LIST_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");

        binding.subTitleTv.setText(categoryTitle);

        loadPdfList();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                try{
                    adapterPdfAdmin.getFilter().filter(s);
                }
                catch (Exception e){
                    Log.d(TAG, "beforeTextChanged: "+e.getMessage());

                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
    }

    private void loadPdfList() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPdf model = ds.getValue(ModelPdf.class);

                    pdfArrayList.add(model);

                    Log.d(TAG, "onDataChange: "+model.getId()+" "+model.getTitle());
                }
                adapterPdfAdmin = new AdapterPdfAdmin(DocListAdminActivity.this, pdfArrayList);
                binding.bookRv.setAdapter(adapterPdfAdmin);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}