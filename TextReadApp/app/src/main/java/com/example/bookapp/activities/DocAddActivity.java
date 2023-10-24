package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityPdfAddBinding;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class DocAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private static final String TAG = "ADD_PDF_TAG";

    private static final int PDF_PICK_CODE = 1000;

    private Uri fileUri = null;
    private String format, role;



    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        role = intent.getStringExtra("role");

        if (Objects.equals(role, "user")){
            //TextView textView = findViewById(categoryTv); // Замените R.id.textView на идентификатор вашего TextView
            binding.categoryTv.setVisibility(View.GONE);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите...");
        progressDialog.setCanceledOnTouchOutside(false);


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();

            }
        });
    }

    private String title = "", description = "";

    private void validateData() {
        String uid = firebaseAuth.getUid();
        Log.d(TAG,"validateData: Проверка данных...");
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        if (Objects.equals(role, "user")){
            selectedCategoryTitle = uid;
        }
        if(TextUtils.isEmpty(title)||TextUtils.isEmpty(description)||TextUtils.isEmpty(selectedCategoryTitle)){
            Toast.makeText(this,"Проверьте правильность введенных вами данных.", Toast.LENGTH_SHORT).show();
        }
        else if(fileUri==null){
            Toast.makeText(this,"Файл не выбран.", Toast.LENGTH_SHORT).show();
        }
        else{
            uploadPdfToStorage();
        }
    }
    private void uploadPdfToStorage() {
        Log.d(TAG,"uploadPdfToStorage: Загрузка файла в хранилище...");
        progressDialog.setMessage("Загрузка файла...");
        progressDialog.show();
        long timestamp = System.currentTimeMillis();
        String filePathAndName = "Books/" + timestamp;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG,"onSuccess: Файл загружен успешно.");
                Log.d(TAG,"onSuccess: Получение url файла...");
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedPdfUrl = ""+uriTask.getResult();

                if (Objects.equals(role, "user")){
                    uploadPdfInfoToDbUser(uploadedPdfUrl, timestamp);
                }
                else{
                    uploadPdfInfoToDb(uploadedPdfUrl, timestamp);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"onFailure: Ошибка при загрузке файла. "+e.getMessage());
                Toast.makeText(DocAddActivity.this,"Ошибка при загрузке файла. "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        Log.d(TAG,"uploadPdfInfoToDb: Загружается инфо о  файле в БД...");
        progressDialog.setMessage("Загружается инфо о  файле в БД...");
        String uid = firebaseAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);
        hashMap.put("url", ""+uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);
        hashMap.put("format", format);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Log.d(TAG,"onSuccess: Инфо о  файле успешно загружена в БД.");
                Toast.makeText(DocAddActivity.this,"Инфо о  файле успешно загружена в БД.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"onFailure: Не удалось загрузить инфо о  файле в БД..."+e.getMessage());
                Toast.makeText(DocAddActivity.this,"Не удалось загрузить инфо о  файле в БД. "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void uploadPdfInfoToDbUser(String uploadedPdfUrl, long timestamp) {
        Log.d(TAG,"uploadPdfInfoToDb: Загружается инфо о  файле в БД...");
        progressDialog.setMessage("Загружается инфо о  файле в БД...");
        String uid = firebaseAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("url", ""+uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);
        hashMap.put("format", format);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Books").child(""+timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Log.d(TAG,"onSuccess: Инфо о  файле успешно загружена в БД.");
                Toast.makeText(DocAddActivity.this,"Инфо о  файле успешно загружена в БД.", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG,"onFailure: Не удалось загрузить инфо о  файле в БД..."+e.getMessage());
                Toast.makeText(DocAddActivity.this,"Не удалось загрузить инфо о  файле в БД. "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPdfCategories() {
        Log.d(TAG,"loadPdfCategories: Загружаются категории...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){

                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private String selectedCategoryId, selectedCategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG,"categoryPickDialog: Показываются категории...");
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите категорию").setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedCategoryTitle = categoryTitleArrayList.get(which);
                selectedCategoryId = categoryIdArrayList.get(which);
                binding.categoryTv.setText(selectedCategoryTitle);

                Log.d(TAG,"onClick: Выбранная категория: "+selectedCategoryId+" "+selectedCategoryTitle);
            }
        }).show();

    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: начинается загрузка PDF или FB2...");
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        String[] mimetypes = {"application/pdf", "application/x-fictionbook+xml", "text/plain", "application/x-fictionbook", "application/x-fictionbook+xml","application/fb2", "text/fb2+xml", "application/x-fb2", "application/xml","application/fictionbook+xml","application/fictionbook", "text/xml", "text/fictionbook+xml","text/fictionbook", "text/x-fictionbook+xml","text/x-fictionbook", "application/x-fictionbook", "application/xml","application/x-fictionbook+xml", "application/x-fictionbook+xml","application/fb2", "text/fb2+xml", "application/xml", "application/fb2+xml", "application/x-fictionbook+xml", "application/fb2"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        startActivityForResult(Intent.createChooser(intent, "Выберите PDF или FB2"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == PDF_PICK_CODE){
                Log.d(TAG,"onActivityResult: Файл успешно загружен.");

                fileUri = data.getData();
                format = getContentResolver().getType(fileUri);
                Log.d(TAG,"onActivityResult: URI"+fileUri);

            }
        }
        else{
            Log.d(TAG,"onActivityResult: Файл НЕ загружен.");
            Toast.makeText(this,"Файл НЕ загружен.", Toast.LENGTH_SHORT).show();
        }
    }

}