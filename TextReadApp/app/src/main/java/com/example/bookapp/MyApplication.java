package com.example.bookapp;

import static com.example.bookapp.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class MyApplication extends Application {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";



    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd/MM/yyyy", cal).toString();
        return date;
    }

    public static void deleteUserBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "deleteBook: Удаление...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Пожалуйста, подождите...");
        progressDialog.setMessage("Удаление "+bookTitle+" ...");
        progressDialog.show();
        Log.d(TAG, "deleteBook: удаление из хранилища...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: Удалено из хранилища");
                Log.d(TAG, "onSuccess: удаление из бд...");
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(firebaseAuth.getUid()).child("Books").child(bookId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: удалено из бд");
                        progressDialog.dismiss();
                        Toast.makeText(context, "Книга успешно удалена", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: не удалось удалить из бд"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: не удалось удалить из хранилища "+e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "deleteBook: Удаление...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Пожалуйста, подождите...");
        progressDialog.setMessage("Удаление "+bookTitle+" ...");
        progressDialog.show();
        Log.d(TAG, "deleteBook: удаление из хранилища...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: Удалено из хранилища");
                Log.d(TAG, "onSuccess: удаление из бд...");
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                reference.child(bookId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: удалено из бд");
                        progressDialog.dismiss();
                        Toast.makeText(context, "Книга успешно удалена", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: не удалось удалить из бд"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: не удалось удалить из хранилища "+e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {

        String TAG = "PDF_SIZE_TAG";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {

                double bytes = storageMetadata.getSizeBytes();
                Log.d(TAG,"onSuccess: "+pdfTitle+" "+bytes);

                double kb = bytes/1024;
                double mb = kb/1024;

                if(mb >= 1){
                    sizeTv.setText(String.format("%.2f", mb)+" MB");
                }
                else if(kb >= 1){
                    sizeTv.setText(String.format("%.2f", kb)+" KB");
                }
                else{
                    sizeTv.setText(String.format("%.2f", bytes)+" bytes");
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"onFailure: "+e.getMessage());

            }
        });
    }


    public static void txtSinglePage(ProgressBar progressBar, ImageView fb2View) {
        fb2View.setBackgroundColor(Color.WHITE);
        fb2View.setImageResource(R.drawable.ic_txt);
        progressBar.setVisibility(View.GONE);
    }

    public static void loadFb2FromUrlSinglePage(String pdfUrl, String pdfTitle,ProgressBar progressBar,ImageView imageView){
        Log.d("картинку_грузим", "loadBookFromUrl: get pdf from storage");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(byte[] bytes) {
                String content = new String(bytes, StandardCharsets.UTF_8);
                int startBin = content.indexOf("<binary");
                int endBin = content.indexOf("</binary>", startBin);

                if (startBin != -1 && endBin != -1) {
                    String binaryEl = content.substring(startBin, endBin + 9); // Получаем первый блок <binary>

                    // Извлекаем код картинки из тега <binary>
                    int startData = binaryEl.indexOf(">") + 1;
                    int endData = binaryEl.indexOf("</binary>");
                    String base64Image = binaryEl.substring(startData, endData);

                    // Декодируем код Base64 в байтовый массив
                    byte[] imageBytes = Base64.getMimeDecoder().decode(base64Image);

                    try {
                        // Создаем Bitmap из байтового массива
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                        // Устанавливаем Bitmap в ImageView
                        imageView.setImageBitmap(bitmap);
                        progressBar.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //binding.fbViewTv.setText(fb2Text);
                //binding.progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("картинку_грузим", "onFailure: "+e.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView pagesTv) {

        String TAG = "PDF_LOAD_SINGLE_TAG";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG, "onSuccess: "+pdfTitle+" файл успешно получен");

                pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError(new OnErrorListener() {
                            @Override
                            public void onError(Throwable t) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "onError: "+t.getMessage());

                            }
                        }).onPageError(new OnPageErrorListener() {
                            @Override
                            public void onPageError(int page, Throwable t) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "onPageError: "+t.getMessage());
                            }
                        })
                        .onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "loadComplete: pdf загружена.");

                                if (pagesTv != null){
                                    pagesTv.setText(""+nbPages);
                                }
                            }
                        })
                        .load();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onFailure: ошибка при получении файла"+e.getMessage());

            }
        });

    }

    public static void loadCategory(String categoryId, TextView categoryTv) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String category = ""+snapshot.child("category").getValue();
                if (!category.equals("null")){
                    categoryTv.setText(category);
                }
                else{
                    categoryTv.setText("Загружены Вами");
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void incrementBookViewCount(String bookId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String viewsCount = ""+snapshot.child("viewsCount").getValue();
                if (viewsCount.equals("") || viewsCount.equals("null")){
                    viewsCount = "0";
                }

                long newViewsCount = Long.parseLong(viewsCount) + 1;
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("viewsCount", newViewsCount);

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                reference.child(bookId).updateChildren(hashMap);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void downloadBook(Context context, String bookId, String bookTitle, String bookUrl, String isUserBook, String format){
        Log.d(TAG_DOWNLOAD, "downloadBook: downloading book...");
        String nameWithExtension;

        if (Objects.equals(format, "application/x-fictionbook+xml")){
            nameWithExtension = bookTitle + ".fb2";
        }
        else if (Objects.equals(format, "text/plain")){
            nameWithExtension = bookTitle + ".txt";
        }
        else{
            nameWithExtension = bookTitle + ".pdf";
        }

        Log.d(TAG_DOWNLOAD, "downloadBook: NAME: " + nameWithExtension);

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Пожалуйста, подождите...");
        progressDialog.setMessage("Загрузка " + nameWithExtension+"...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG_DOWNLOAD, "onSuccess: Book downloaded");
                saveDownloadedBook(context, progressDialog, bytes, nameWithExtension, bookId, isUserBook);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG_DOWNLOAD, "onFailure: Failed to download" + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, "Не удалось загрузить книгу "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId, String isUserBook) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook: saving downloaded book");
        try{
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadFolder.mkdirs();

            String filePath = downloadFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Сохранено в Загрузки", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: saved to downloads");
            progressDialog.dismiss();

            if (!Objects.equals(isUserBook, "1")){
                incrementBookDownloadCount(bookId);
            }

        }
        catch (Exception e){
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: failed saving to downloads " + e.getMessage());
            Toast.makeText(context, "Не удалось загрузить книгу +" + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private static void incrementBookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD, "incrementBookDownloadCount: Incrementing book download book");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                Log.d(TAG_DOWNLOAD, "onDataChange: Downloads count" + downloadsCount);

                if (downloadsCount.equals("") || downloadsCount.equals("null")){
                    downloadsCount = "0";
                }
                long newDownloadsCount = Long.parseLong(downloadsCount) + 1;
                Log.d(TAG_DOWNLOAD, "onDataChange: New download count: " + newDownloadsCount);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("downloadsCount", newDownloadsCount);

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                reference.child(bookId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: downloads count updated");

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD, "onFailure: failed to update downloads count "+ e.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static void addToFavourite(Context context, String bookId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context, "Вы не авторизованы.", Toast.LENGTH_SHORT).show();
            
        }
        else {
            long timestamp = System.currentTimeMillis();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId", ""+bookId);
            hashMap.put("timestamp", ""+timestamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favourites").child(bookId).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(context, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Не удалось добавить в избранное " + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    public static void removeFromFavourite(Context context, String bookId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context, "Вы не авторизованы.", Toast.LENGTH_SHORT).show();

        }
        else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favourites").child(bookId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(context, "Удалено из избранного", Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Не удалось убрать из избранного " + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
    }
}
