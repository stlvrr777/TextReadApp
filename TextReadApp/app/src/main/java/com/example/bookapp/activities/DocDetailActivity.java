package com.example.bookapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.Translate.ApiGetTask;
import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.adapters.AdapterComment;
import com.example.bookapp.databinding.ActivityPdfDetailBinding;
import com.example.bookapp.databinding.DialogCommentAddBinding;
import com.example.bookapp.databinding.DialogTranslateChooseBinding;
import com.example.bookapp.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DocDetailActivity extends AppCompatActivity {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    private ActivityPdfDetailBinding binding;

    private String bookId, bookTitle, bookUrl, isUserBook;

    private String format;

    boolean isInMyFavourite = false;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;

    public static String apiKey = "\"";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        isUserBook = intent.getStringExtra("isUserBook");

        binding.moreBtn.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);

        progressDialog.setTitle("Пожалуйста, подождите");
        progressDialog.setCanceledOnTouchOutside(false);
        ApiGetTask task = new ApiGetTask();
        task.execute();
        while (apiKey == "\""){
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        firebaseAuth = FirebaseAuth.getInstance();

        if (!Objects.equals(isUserBook, "1")){
            if (firebaseAuth.getCurrentUser()!= null){
                checkIsFavourite();
            }
            loadComments();
            MyApplication.incrementBookViewCount(bookId);
        }
        else
        {
            binding.commentsLabelTv.setText("");
            binding.addCommentBtn.setEnabled(false);
            binding.favouriteBtn.setVisibility(View.GONE);
            //binding.favouriteBtn.setEnabled(false);
        }
        loadBookDetails();




        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Objects.equals(format, "application/x-fictionbook+xml") && !Objects.equals(format, "text/plain")){
                    Intent intent1 = new Intent(DocDetailActivity.this, PdfViewActivity.class);
                    intent1.putExtra("bookId", bookId);
                    intent1.putExtra("bookTitle", bookTitle);
                    intent1.putExtra("bookUrl", bookUrl);
                    intent1.putExtra("toTrans", "0");
                    intent1.putExtra("isUserBook", isUserBook);
                    intent1.putExtra("apiKey", apiKey);
                    intent1.putExtra("format", format);
                    startActivity(intent1);
                }
                else{
                    Intent intent1 = new Intent(DocDetailActivity.this, Fb2ViewActivity.class);
                    intent1.putExtra("bookId", bookId);
                    intent1.putExtra("bookTitle", bookTitle);
                    intent1.putExtra("bookUrl", bookUrl);
                    intent1.putExtra("toTrans", "0");
                    intent1.putExtra("isUserBook", isUserBook);
                    intent1.putExtra("apiKey", apiKey);
                    intent1.putExtra("format", format);
                    startActivity(intent1);
                }

            }
        });

        if (!Objects.equals(isUserBook, "1")){
            binding.moreBtn.setImageResource(R.drawable.ic_download_white);
            binding.moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG_DOWNLOAD, "onClick: Checking permission");
                    if (ContextCompat.checkSelfPermission(DocDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Log.d(TAG_DOWNLOAD, "onClick: Permission already granted, can download book");
                        MyApplication.downloadBook(DocDetailActivity.this, ""+bookId, ""+bookTitle, ""+bookUrl, ""+isUserBook, ""+format);
                    }
                    else {
                        Log.d(TAG_DOWNLOAD, "onClick: permission was not granted, request permission");
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
            });
        }
        else{
            binding.moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moreDialog();
                }
            });
        }


        binding.favouriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(DocDetailActivity.this, "Вы не авторизованы", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(isInMyFavourite){
                        MyApplication.removeFromFavourite(DocDetailActivity.this, bookId);

                    }
                    else{
                        MyApplication.addToFavourite(DocDetailActivity.this, bookId);
                    }
                }
            }
        });

        binding.translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                chooseLangDialog();
                
            }
        });

        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(DocDetailActivity.this, "Вы не авторизованы.", Toast.LENGTH_SHORT).show();
                }
                else{
                    addCommentDialog();
                }
            }
        });
    }

    private String lang = "";
    private String selectedLang, selectedLangId = "";


    public void chooseLangDialog() {
        DialogTranslateChooseBinding translateChooseBinding = DialogTranslateChooseBinding.inflate(LayoutInflater.from(this));


        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(translateChooseBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        translateChooseBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        translateChooseBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lang = translateChooseBinding.languageTv.getText().toString().trim();
                if(TextUtils.isEmpty(lang)){
                    Toast.makeText(DocDetailActivity.this, "Выберите язык", Toast.LENGTH_SHORT).show();

                }
                else{
                    alertDialog.dismiss();
                    Intent intent1 = new Intent(DocDetailActivity.this, Fb2ViewActivity.class);
                    intent1.putExtra("bookId", bookId);
                    intent1.putExtra("bookTitle", bookTitle);
                    intent1.putExtra("bookUrl", bookUrl);
                    intent1.putExtra("selectedLangId", selectedLangId);
                    intent1.putExtra("selectedLang", selectedLangId);
                    intent1.putExtra("toTrans", "1");
                    intent1.putExtra("isUserBook", isUserBook);
                    intent1.putExtra("apiKey", apiKey);
                    intent1.putExtra("format", format);
                    startActivity(intent1);

                }
            }
        });

        translateChooseBinding.languageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] categoriesArray = {"русский", "English", "español","Deutsch","azərbaycan","shqip","አማርኛ", "العربية", "հայերեն", "Afrikaans" ,"euskara" ,"беларуская" ,"বাংলা" ,"မြန်မာ" ,"български" ,"bosanski" ,"Cymraeg" ,"magyar" ,"Tiếng Việt" ,"galego" ,"Nederlands" ,"Ελληνικά" ,"ქართული" ,"ગુજરાતી" ,"dansk" ,"עברית" ,"ייִדיש" ,"Indonesia" ,"Gaeilge" ,"italiano" ,"íslenska" ,"español" ,"қазақ тілі" ,"ಕನ್ನಡ" ,"català" ,"кыргызча" ,"中文" ,"한국어" ,"ខ្មែរ" ,"ລາວ" ,"latviešu" ,"lietuvių" ,"Lëtzebuergesch" ,"Malagasy" ,"Melayu" ,"മലയാളം" ,"Malti" ,"македонски" ,"मराठी" ,"монгол" ,"Deutsch" ,"नेपाली" ,"norsk bokmål" ,"ਪੰਜਾਬੀ" ,"فارسی" ,"polski" ,"português" ,"română" ,"русский" ,"српски" ,"සිංහල" ,"slovenčina" ,"slovenščina" ,"Kiswahili" ,"тоҷикӣ" ,"ไทย" ,"Filipino" ,"தமிழ்" ,"татар" ,"తెలుగు" ,"Türkçe" ,"o‘zbek", "українська" ,"اردو", "suomi", "français", "हिन्दी", "hrvatski", "čeština", "svenska", "Gàidhlig", "eesti", "esperanto", "日本語"};

                AlertDialog.Builder builder = new AlertDialog.Builder(DocDetailActivity.this);
                builder.setTitle("Выберите язык").setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedLang = categoriesArray[which];
                        // Вы можете использовать порядковый номер выбранной категории как ID категории
                        if(selectedLang == "русский")
                        {
                            selectedLangId = "ru";
                        }
                        else if (selectedLang == "English"){
                            selectedLangId = "en";
                        }
                        else if (selectedLang == "español"){
                            selectedLangId = "es";
                        }
                        else if (selectedLang == "Deutsch"){
                            selectedLangId = "de";
                        }
                        else if (selectedLang == "azərbaycan"){
                            selectedLangId = "az";
                        }
                        else if (selectedLang == "shqip"){
                            selectedLangId = "sq";
                        }
                        else if (selectedLang == "አማርኛ"){
                            selectedLangId = "am";
                        }
                        else if (selectedLang == "العربية"){
                            selectedLangId = "ar";
                        }
                        else if (selectedLang == "հայերեն"){
                            selectedLangId = "hy";
                        }
                        else if (selectedLang == "Afrikaans"){
                            selectedLangId = "af";
                        }
                        else if (selectedLang == "euskara"){
                            selectedLangId = "eu";
                        }
                        else if (selectedLang == "বাংলা"){
                            selectedLangId = "bn";
                        }
                        else if (selectedLang == "မြန်မာ"){
                            selectedLangId = "my";
                        }
                        else if (selectedLang == "български"){
                            selectedLangId = "bg";
                        }
                        else if (selectedLang == "bosanski"){
                            selectedLangId = "bs";
                        }
                        else if (selectedLang == "Cymraeg"){
                            selectedLangId = "cy";
                        }
                        else if (selectedLang == "magyar"){
                            selectedLangId = "hu";
                        }
                        else if (selectedLang == "Tiếng Việt"){
                            selectedLangId = "vi";
                        }
                        else if (selectedLang == "galego"){
                            selectedLangId = "gl";
                        }
                        else if (selectedLang == "Nederlands"){
                            selectedLangId = "nl";
                        }
                        else if (selectedLang == "Ελληνικά"){
                            selectedLangId = "el";
                        }
                        else if (selectedLang == "ქართული"){
                            selectedLangId = "ka";
                        }
                        else if (selectedLang == "ગુજરાતી"){
                            selectedLangId = "gu";
                        }
                        else if (selectedLang == "dansk"){
                            selectedLangId = "da";
                        }
                        else if (selectedLang == "עברית"){
                            selectedLangId = "he";
                        }
                        else if (selectedLang == "ייִדיש"){
                            selectedLangId = "yi";
                        }
                        else if (selectedLang == "Indonesia"){
                            selectedLangId = "id";
                        }
                        else if (selectedLang == "Gaeilge"){
                            selectedLangId = "ga";
                        }
                        else if (selectedLang == "italiano"){
                            selectedLangId = "it";
                        }
                        else if (selectedLang == "íslenska"){
                            selectedLangId = "is";
                        }
                        else if (selectedLang == "қазақ тілі"){
                            selectedLangId = "kk";
                        }
                        else if (selectedLang == "ಕನ್ನಡ"){
                            selectedLangId = "kn";
                        }
                        else if (selectedLang == "català"){
                            selectedLangId = "ca";
                        }
                        else if (selectedLang == "кыргызча"){
                            selectedLangId = "ky";
                        }
                        else if (selectedLang == "中文"){
                            selectedLangId = "zh";
                        }
                        else if (selectedLang == "한국어"){
                            selectedLangId = "ko";
                        }
                        else if (selectedLang == "ខ្មែរ"){
                            selectedLangId = "km";
                        }
                        else if (selectedLang == "ລາວ"){
                            selectedLangId = "lo";
                        }
                        else if (selectedLang == "latviešu"){
                            selectedLangId = "lv";
                        }
                        else if (selectedLang == "lietuvių"){
                            selectedLangId = "lt";
                        }
                        else if (selectedLang == "Lëtzebuergesch"){
                            selectedLangId = "lb";
                        }
                        else if (selectedLang == "Malagasy"){
                            selectedLangId = "mg";
                        }
                        else if (selectedLang == "Melayu"){
                            selectedLangId = "ms";
                        }
                        else if (selectedLang == "മലയാളം"){
                            selectedLangId = "ml";
                        }
                        else if (selectedLang == "Malti"){
                            selectedLangId = "mt";
                        }
                        else if (selectedLang == "македонски"){
                            selectedLangId = "mk";
                        }
                        else if (selectedLang == "मराठी"){
                            selectedLangId = "mr";
                        }
                        else if (selectedLang == "монгол"){
                            selectedLangId = "mn";
                        }
                        else if (selectedLang == "नेपाली"){
                            selectedLangId = "ne";
                        }
                        else if (selectedLang == "norsk bokmål"){
                            selectedLangId = "no";
                        }
                        else if (selectedLang == "ਪੰਜਾਬੀ"){
                            selectedLangId = "pa";
                        }
                        else if (selectedLang == "فارسی"){
                            selectedLangId = "fa";
                        }
                        else if (selectedLang == "polski"){
                            selectedLangId = "pl";
                        }
                        else if (selectedLang == "português"){
                            selectedLangId = "pt";
                        }
                        else if (selectedLang == "română"){
                            selectedLangId = "ro";
                        }
                        else if (selectedLang == "српски"){
                            selectedLangId = "sr";
                        }
                        else if (selectedLang == "සිංහල"){
                            selectedLangId = "si";
                        }
                        else if (selectedLang == "slovenčina"){
                            selectedLangId = "sk";
                        }
                        else if (selectedLang == "slovenščina"){
                            selectedLangId = "sl";
                        }
                        else if (selectedLang == "Kiswahili"){
                            selectedLangId = "sw";
                        }
                        else if (selectedLang == "тоҷикӣ"){
                            selectedLangId = "tg";
                        }
                        else if (selectedLang == "ไทย"){
                            selectedLangId = "th";
                        }
                        else if (selectedLang == "Filipino"){
                            selectedLangId = "tl";
                        }
                        else if (selectedLang == "தமிழ்"){
                            selectedLangId = "ta";
                        }
                        else if (selectedLang == "татар"){
                            selectedLangId = "tt";
                        }
                        else if (selectedLang == "తెలుగు"){
                            selectedLangId = "te";
                        }
                        else if (selectedLang == "Türkçe"){
                            selectedLangId = "tr";
                        }
                        else if (selectedLang == "o‘zbek"){
                            selectedLangId = "uz";
                        }
                        else if (selectedLang == "українська"){
                            selectedLangId = "uk";
                        }
                        else if (selectedLang == "اردو"){
                            selectedLangId = "ur";
                        }
                        else if (selectedLang == "suomi"){
                            selectedLangId = "fi";
                        }
                        else if (selectedLang == "français"){
                            selectedLangId = "fr";
                        }
                        else if (selectedLang == "हिन्दी"){
                            selectedLangId = "hi";
                        }
                        else if (selectedLang == "hrvatski"){
                            selectedLangId = "hr";
                        }
                        else if (selectedLang == "čeština"){
                            selectedLangId = "cs";
                        }
                        else if (selectedLang == "svenska"){
                            selectedLangId = "sv";
                        }
                        else if (selectedLang == "Gàidhlig"){
                            selectedLangId = "gd";
                        }
                        else if (selectedLang == "eesti"){
                            selectedLangId = "et";
                        }
                        else if (selectedLang == "esperanto"){
                            selectedLangId = "eo";
                        }
                        else if (selectedLang == "日本語"){
                            selectedLangId = "ja";
                        }
                        translateChooseBinding.languageTv.setText(selectedLang);
                        Log.d("langPickDialog", "onClick: Выбранная категория: " + selectedLangId + " " + selectedLang);
                    }
                }).show();

            }
        });
    }


    private void loadComments() {
        commentArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelComment model = ds.getValue(ModelComment.class);
                    commentArrayList.add(model);

                }
                adapterComment = new AdapterComment(DocDetailActivity.this, commentArrayList);
                binding.commentRv.setAdapter(adapterComment);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void moreDialog() {
        String[] categoriesArray = {"Скачать", "Удалить"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Опции").setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Обработка выбранного варианта
                switch (which) {
                    case 0:
                        // Действия при выборе "Скачать"
                        // Ваш код для скачивания
                        Log.d(TAG_DOWNLOAD, "onClick: Checking permission");
                        if (ContextCompat.checkSelfPermission(DocDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                            Log.d(TAG_DOWNLOAD, "onClick: Permission already granted, can download book");
                            MyApplication.downloadBook(DocDetailActivity.this, ""+bookId, ""+bookTitle, ""+bookUrl, ""+isUserBook, ""+format);
                        }
                        else {
                            Log.d(TAG_DOWNLOAD, "onClick: permission was not granted, request permission");
                            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }
                            break;
                            case 1:
                                // Действия при выборе "Удалить"
                                // Ваш код для удаления
                                MyApplication.deleteUserBook(DocDetailActivity.this,
                                        ""+bookId,
                                        ""+bookUrl,
                                        ""+bookTitle);
                                break;
                        }
            }
        }).show();
    }




    private String comment = "";


    private void addCommentDialog() {
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment = commentAddBinding.commentEt.getText().toString().trim();
                if(TextUtils.isEmpty(comment)){
                    Toast.makeText(DocDetailActivity.this, "Введите комментарий", Toast.LENGTH_SHORT).show();

                }
                else{
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });
    }
//todo сделать просмотр книги юзера читабельным(работа с интерфейсом)
    private void addComment() {
        progressDialog.setMessage("Комментарий добавляется...");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("comment", ""+comment);
        hashMap.put("bookId", ""+bookId);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(DocDetailActivity.this, "Комментарий добавлен.", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(DocDetailActivity.this, "Не удалось добавить комментарий "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


   }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
               if (isGranted){
                   Log.d(TAG_DOWNLOAD, ": Permission granted");
                   MyApplication.downloadBook(this, ""+bookId, ""+bookTitle, ""+bookUrl, ""+isUserBook, ""+format);
               }
               else {
                   Log.d(TAG_DOWNLOAD, "Permission was denied...: ");
                   Toast.makeText(this, "Разрешение отклонено", Toast.LENGTH_SHORT).show();
               }
            });

    private void loadBookDetails() {
        if (Objects.equals(isUserBook, "1")){
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Books").child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    bookTitle = ""+snapshot.child("title").getValue();
                    String description = ""+snapshot.child("description").getValue();
                    String categoryId = ""+snapshot.child("categoryId").getValue();
                    String viewsCount = ""+snapshot.child("viewsCount").getValue();
                    String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                    format = ""+snapshot.child("format").getValue();
                    bookUrl = ""+snapshot.child("url").getValue();
                    String timestamp = ""+snapshot.child("timestamp").getValue();

                    binding.moreBtn.setVisibility(View.VISIBLE);

                    String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                    if (Objects.equals(format, "application/x-fictionbook+xml")){
                        MyApplication.loadFb2FromUrlSinglePage(""+bookUrl,""+bookTitle,binding.progressBar,binding.fb2View);
                        binding.pdfView.setVisibility(View.INVISIBLE);
                    }
                    else if(Objects.equals(format, "text/plain"))
                    {
                        MyApplication.txtSinglePage(binding.progressBar,binding.fb2View);
                        binding.pdfView.setVisibility(View.INVISIBLE);
                        //binding.progressBar.setVisibility(View.GONE);
                    }
                    else{
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl,""+bookTitle, binding.pdfView, binding.progressBar,null);
                        binding.fb2View.setVisibility(View.INVISIBLE);
                    }

                    MyApplication.loadCategory(""+categoryId, binding.categoryTv);
                    //MyApplication.loadPdfFromUrlSinglePage(""+bookUrl, ""+bookTitle, binding.pdfView, binding.progressBar, binding.pagesTv);

                    MyApplication.loadPdfSize(""+bookUrl,""+bookTitle, binding.sizeTv);

                    binding.titleTv.setText(bookTitle);
                    binding.descriptionTv.setText(description);
                    binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                    binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));
                    binding.dateTv.setText(date);


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        else{
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
            ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    bookTitle = ""+snapshot.child("title").getValue();
                    String description = ""+snapshot.child("description").getValue();
                    String categoryId = ""+snapshot.child("categoryId").getValue();
                    String viewsCount = ""+snapshot.child("viewsCount").getValue();
                    String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                    bookUrl = ""+snapshot.child("url").getValue();
                    format = ""+snapshot.child("format").getValue();
                    String timestamp = ""+snapshot.child("timestamp").getValue();

                    binding.moreBtn.setVisibility(View.VISIBLE);

                    String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                    MyApplication.loadCategory(""+categoryId, binding.categoryTv);
                    if (Objects.equals(format, "application/x-fictionbook+xml")){
                        MyApplication.loadFb2FromUrlSinglePage(""+bookUrl,""+bookTitle,binding.progressBar,binding.fb2View);
                        binding.pdfView.setVisibility(View.INVISIBLE);
                    }
                    else if(Objects.equals(format, "text/plain"))
                    {
                        MyApplication.txtSinglePage(binding.progressBar,binding.fb2View);
                        binding.pdfView.setVisibility(View.INVISIBLE);
                        //binding.progressBar.setVisibility(View.GONE);
                    }
                    else{
                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl,""+bookTitle, binding.pdfView, binding.progressBar,null);
                        binding.fb2View.setVisibility(View.INVISIBLE);
                    }

                    MyApplication.loadPdfSize(""+bookUrl,""+bookTitle, binding.sizeTv);

                    binding.titleTv.setText(bookTitle);
                    binding.descriptionTv.setText(description);
                    binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                    binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));
                    binding.dateTv.setText(date);


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    }

    private void checkIsFavourite(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favourites").child(bookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isInMyFavourite = snapshot.exists();
                if (isInMyFavourite){
                    binding.favouriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white, 0,0);
                    binding.favouriteBtn.setText("Убрать из избранного");
                }
                else{
                    binding.favouriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0,0);
                    binding.favouriteBtn.setText("Добавить в избранное");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
