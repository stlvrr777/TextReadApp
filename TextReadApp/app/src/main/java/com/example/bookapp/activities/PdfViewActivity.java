package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.Constants;
import com.example.bookapp.Translate.TranslateTask;
import com.example.bookapp.databinding.ActivityPdfViewBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
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
import com.google.firebase.storage.StorageReference;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/*
$yandexPassportOauthToken = "y0_AgAAAAAFXzbtAATuwQAAAADh3Qr17PM3DuNjQo6dXhNQx4PDwVdMPRc"
$Body = @{ yandexPassportOauthToken = "$yandexPassportOauthToken" } | ConvertTo-Json -Compress
Invoke-RestMethod -Method 'POST' -Uri 'https://iam.api.cloud.yandex.net/iam/v1/tokens' -Body $Body -ContentType 'Application/json' | Select-Object -ExpandProperty iamToken
*/

public class PdfViewActivity extends AppCompatActivity {

    private ActivityPdfViewBinding binding;

    String bookId, bookTitle, bookUrl, isUserBook, apiKey;
    private String selectedLang;
    private String toTrans;
    private byte finalBytes[];
    private byte[] buffer;
    private Font font;

    private FirebaseAuth firebaseAuth;
    public static String transText = "\"";


    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    private static final String TAG = "PDF_VIEW_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        bookTitle = intent.getStringExtra("bookTitle");
        bookUrl = intent.getStringExtra("bookUrl");
        selectedLang = intent.getStringExtra("selectedLang");
        toTrans = intent.getStringExtra("toTrans");
        isUserBook = intent.getStringExtra("isUserBook");
        apiKey = intent.getStringExtra("apiKey");

        Log.d(TAG, "onCreate: bookId"+bookId);

        loadBookDetails();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreDialog();
            }
        });
    }



    private void loadBookDetails() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (Objects.equals(isUserBook, "1")){
            Log.d(TAG, "loadBookDetails: get pdf url from db...");
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Books").child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String pdfUrl = ""+snapshot.child("url").getValue();
                    Log.d(TAG, "onDataChange: PDF URL " + pdfUrl);
                    loadBookFromUrl(pdfUrl);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        else
        {
            Log.d(TAG, "loadBookDetails: get pdf url from db...");
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
            ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String pdfUrl = ""+snapshot.child("url").getValue();
                    Log.d(TAG, "onDataChange: PDF URL " + pdfUrl);
                    loadBookFromUrl(pdfUrl);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }


    }

    private void loadBookFromUrl(String pdfUrl) {
        Log.d(TAG, "loadBookFromUrl: get pdf from storage");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                if (Objects.equals(toTrans, "0")){
                    pdfInApp(bytes);
                }
                else{
                    readPdfFile(bytes);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: "+e.getMessage());
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void readPdfFile(byte[] pdfBytes) {
        PdfReader reader = null;
        try {
            reader = new PdfReader(pdfBytes);
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                sb.append(PdfTextExtractor.getTextFromPage(reader, i));
            }
            String text = sb.toString();
            String res = text.replace("\"", "|||||");

            InputStream is = getAssets().open("days2.ttf");
            int size = is.available();
            buffer = new byte[size]; //declare the size of the byte array with size of the file
            is.read(buffer); //read file
            is.close(); //close file
            BaseFont bf = BaseFont.createFont("days2.ttf", BaseFont.IDENTITY_H, true, false, buffer, null);
            font = new Font(bf, 14);

            TranslateTask task = new TranslateTask(PdfViewActivity.this, selectedLang, res, apiKey);
            task.execute();
            while (transText == "\""){
                TimeUnit.SECONDS.sleep(1);
            }

            //return res;
        } catch (IOException | DocumentException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        //String text = transText.toString();
        pdfFromText();
    }

    public void toGo(){
        pdfFromText();
    }


    public void pdfFromText() {
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            String pdfText = transText.toString();

            Paragraph paragraph = new Paragraph(pdfText, font);// Добавляем параграф в документ
            document.add(paragraph);
        }
        catch (DocumentException e) {
            Log.d(TAG, e.toString());
            Log.d(TAG, e.getMessage());
            // (DocumentException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            // Закрываем документ
            document.close();
        }
        // Получаем байты из ByteArrayOutputStream
        byte[] pdfBytes = outputStream.toByteArray();
        pdfInApp(pdfBytes);
    }

    private void pdfInApp(byte bytes[]){
        binding.pdfView.fromBytes(bytes).swipeHorizontal(false).onPageChange(new OnPageChangeListener() {
            @Override
            public void onPageChanged(int page, int pageCount) {
                int currentPage = (page + 1);
                binding.toolbarSubtitleTv.setText(currentPage+"/"+pageCount);
                Log.d(TAG, "onPageChanged: "+currentPage+"/"+pageCount);
            }
        }).onError(new OnErrorListener() {
            @Override
            public void onError(Throwable t) {
                Log.d(TAG, "onError: "+t.getMessage());
                Toast.makeText(PdfViewActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).onPageError(new OnPageErrorListener() {
            @Override
            public void onPageError(int page, Throwable t) {
                Log.d(TAG, "onPageError: "+t.getMessage());
                Toast.makeText(PdfViewActivity.this, "Ошибка на странице "+page+" "+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).load();
        finalBytes = bytes;
        transText = "\"";
        Fb2ViewActivity.transText = "\"";
        binding.progressBar.setVisibility(View.GONE);

    }

    private void moreDialog() {
        String[] categoriesArray = {"Скачать"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Сохранить на устройство").setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveFileToDevice(finalBytes, bookTitle, PdfViewActivity.this);

            }

        }).show();

    }

    public static void  saveFileToDevice(byte[] fileBytes, String bookTitle, Context context) {
        Log.d(TAG_DOWNLOAD, "downloadBook: downloading book...");

        String nameWithExtension = bookTitle + ".pdf";
        Log.d(TAG_DOWNLOAD, "downloadBook: NAME: " + nameWithExtension);

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Пожалуйста, подождите...");
        progressDialog.setMessage("Загрузка " + nameWithExtension+"...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        Log.d(TAG_DOWNLOAD, "saveDownloadedBook: saving downloaded book");
        try{
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadFolder.mkdirs();

            String filePath = downloadFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(fileBytes);
            out.close();

            Toast.makeText(context, "Сохранено в Загрузки", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: saved to downloads");
            progressDialog.dismiss();

        }
        catch (Exception e){
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: failed saving to downloads " + e.getMessage());
            Toast.makeText(context, "Не удалось загрузить книгу +" + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

}
