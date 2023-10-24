package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.Translate.TranslateTask;
import com.example.bookapp.databinding.ActivityFavWordsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class activity_fav_words extends AppCompatActivity {

    private ActivityFavWordsBinding binding;
    private ProgressDialog progressDialog;
    private String selectedLangId = "en", selectedLang = " ", favwords, translatedFavwords = "";

    private FirebaseAuth firebaseAuth;
    private static final String TAG = "DOC_VIEW_TAG";

    public static String transText = "\"";


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavWordsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите...");
        progressDialog.setCanceledOnTouchOutside(false);




        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity_fav_words.this, ProfileActivity.class ));

                /*AlertDialog.Builder builder = new AlertDialog.Builder(activity_fav_words.this);
                builder.setTitle("Сохранить изменения?");
                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Действия при выборе "Да"
                        firebaseAuth = FirebaseAuth.getInstance();
                        progressDialog.setMessage("Словарь обновляется...");
                        progressDialog.show();
                        favwords = favwords + " \n"+wordToTrans;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("favwords", ""+binding.fbViewTv.getText().toString());
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                        databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                Toast.makeText(activity_fav_words.this, "Словарь обновлен", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: failed update to db "+ e.getMessage());
                                progressDialog.dismiss();
                                Toast.makeText(activity_fav_words.this, "Не удалось обновить словарь"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        onBackPressed();
                    }
                });
                builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Действия при выборе "Нет"
                        onBackPressed();
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
                dialog.show();*/
            }
        });

        binding.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreDialog();
            }
        });

        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity_fav_words.this, FavEditActivity.class ));
            }
        });

        loadUserInfo();


    }

    private void loadUserInfo(){

        firebaseAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "loadUserInfo: Loading user info of user" + firebaseAuth.getUid());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favwords = ""+snapshot.child("favwords").getValue();
                String[] favMas = favwords.split("\n");
                for (String line : favMas) {
                    TranslateTask task = new TranslateTask(activity_fav_words.this, "ru", line, ProfileActivity.apiKey);
                    task.execute();
                    while (transText == "\""){
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    translatedFavwords = translatedFavwords + line + " - " + transText + "\n";
                    transText = "\"";
                    PdfViewActivity.transText = "\"";
                    activity_fav_words.transText = "\"";
                }
                binding.fbViewTv.setText(translatedFavwords);
                binding.progressBar.setVisibility(View.GONE);
                binding.progressTv.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void moreDialog() {
        String[] categoriesArray = {"Скачать"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Сохранить на устройство").setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createTxt(binding.fbViewTv.getText().toString(), "Your dictionary", activity_fav_words.this);

            }
        }).show();

    }

    private void createTxt(String finalText, String bookTitle, activity_fav_words activity_fav_words) {
        try {
            // Создание временного файла
            File tempFile = File.createTempFile("temp", ".txt");

            // Запись текста во временный файл
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(finalText.getBytes());
            fos.close();

            // Чтение содержимого файла в виде массива байтов
            byte[] fileBytes = new byte[(int) tempFile.length()];
            FileInputStream fis = new FileInputStream(tempFile);
            fis.read(fileBytes);
            fis.close();
            tempFile.delete();
            saveFileToDevice(fileBytes, bookTitle, activity_fav_words.this);
            // Использование массива байтов по вашему усмотрению

            // Удаление временного файла


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void  saveFileToDevice(byte[] fileBytes, String bookTitle, Context context) {
        Log.d("TAG_DOWNLOAD", "downloadBook: downloading book...");

        String nameWithExtension = bookTitle + ".txt";
        Log.d("TAG_DOWNLOAD", "downloadBook: NAME: " + nameWithExtension);



        Log.d("TAG_DOWNLOAD", "saveDownloadedBook: saving downloaded book");
        try{
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadFolder.mkdirs();

            String filePath = downloadFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(fileBytes);
            out.close();

            Toast.makeText(context, "Сохранено в Загрузки", Toast.LENGTH_SHORT).show();
            Log.d("TAG_DOWNLOAD", "saveDownloadedBook: saved to downloads");


        }
        catch (Exception e){
            Log.d("TAG_DOWNLOAD", "saveDownloadedBook: failed saving to downloads " + e.getMessage());
            Toast.makeText(context, "Не удалось загрузить книгу +" + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }
}