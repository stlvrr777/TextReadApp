package com.example.bookapp.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.Constants;
import com.example.bookapp.R;
import com.example.bookapp.Translate.TranslateTask;
import com.example.bookapp.databinding.ActivityFb2ViewBinding;
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
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Fb2ViewActivity extends AppCompatActivity {

    private ActivityFb2ViewBinding binding;

    String bookId, bookTitle, bookUrl, isUserBook, apiKey, format;
    private String selectedLang = " ", wordToTrans = "", finalText, favwords = "";

    private ProgressDialog progressDialog;
    private String selectedLangId = "en";
    private String toTrans;
    private byte finalBytes[];



    private FirebaseAuth firebaseAuth;
    public static String transText = "\"";

    private static String langOfDoc = " ";

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    private static final String TAG = "DOC_VIEW_TAG";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFb2ViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.favBtn.setEnabled(false);

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        bookTitle = intent.getStringExtra("bookTitle");
        bookUrl = intent.getStringExtra("bookUrl");
        selectedLang = intent.getStringExtra("selectedLang");
        //selectedLangId = intent.getStringExtra("selectedLang");
        toTrans = intent.getStringExtra("toTrans");
        isUserBook = intent.getStringExtra("isUserBook");
        apiKey = intent.getStringExtra("apiKey");
        format = intent.getStringExtra("format");
        langOfDoc = selectedLang;


        Log.d(TAG, "onCreate: bookId"+bookId);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите...");
        progressDialog.setCanceledOnTouchOutside(false);
        loadUserInfo();
        loadBookDetails();

        binding.translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    wordtranslate(selectedLangId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

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

        binding.chooseLangTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] categoriesArray = {"русский", "English", "español","Deutsch","azərbaycan","shqip","አማርኛ","العربية", "հայերեն", "Afrikaans" ,"euskara" ,"беларуская" ,"বাংলা" ,"မြန်မာ" ,"български" ,"bosanski" ,"Cymraeg" ,"magyar" ,"Tiếng Việt" ,"galego" ,"Nederlands" ,"Ελληνικά" ,"ქართული" ,"ગુજરાતી" ,"dansk" ,"עברית" ,"ייִדיש" ,"Indonesia" ,"Gaeilge" ,"italiano" ,"íslenska" ,"español" ,"қазақ тілі" ,"ಕನ್ನಡ" ,"català" ,"кыргызча" ,"中文" ,"한국어" ,"ខ្មែរ" ,"ລາວ" ,"latviešu" ,"lietuvių" ,"Lëtzebuergesch" ,"Malagasy" ,"Melayu" ,"മലയാളം" ,"Malti" ,"македонски" ,"मराठी" ,"монгол" ,"Deutsch" ,"नेपाली" ,"norsk bokmål" ,"ਪੰਜਾਬੀ" ,"فارسی" ,"polski" ,"português" ,"română" ,"русский" ,"српски" ,"සිංහල" ,"slovenčina" ,"slovenščina" ,"Kiswahili" ,"тоҷикӣ" ,"ไทย" ,"Filipino" ,"தமிழ்" ,"татар" ,"తెలుగు" ,"Türkçe" ,"o‘zbek", "українська" ,"اردو", "suomi", "français", "हिन्दी", "hrvatski", "čeština", "svenska", "Gàidhlig", "eesti", "esperanto", "日本語"};

                AlertDialog.Builder builder = new AlertDialog.Builder(Fb2ViewActivity.this);
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
                        else if (selectedLang == "беларуская"){
                            selectedLangId = "be";
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
                        binding.chooseLangTv.setText(selectedLang);
                        Log.d("langPickDialog", "onClick: Выбранная категория: " + selectedLangId + " " + selectedLang);
                    }
                }).show();

            }
        });

        binding.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToFav();

            }
        });
        // Назначить слушатель для выделения слова при нажатии
        binding.fbViewTv.getSettings().setJavaScriptEnabled(true);
        binding.fbViewTv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Получаем выделенный текст с помощью JavaScript
                    binding.fbViewTv.evaluateJavascript("(function(){return window.getSelection().toString()})();",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    // Обновляем текст в TextView
                                    binding.extraTextView.setText(value.replaceAll("\"", ""));
                                    wordToTrans = value.replaceAll("\"", "");
                                    if (wordToTrans != null && !wordToTrans.equals("") && wordToTrans != " "){
                                        binding.favBtn.setEnabled(true);
                                        binding.favBtn.setImageResource(R.drawable.ic_fav);
                                    }
                                    else{
                                        binding.favBtn.setEnabled(false);
                                        binding.favBtn.setImageResource(R.drawable.ic_fav_grey);
                                    }
                                }
                            });
                }
                return false;
            }
        });
    }

    private void addToFav() {
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog.setMessage("Слово добавляется в словарь...");
        progressDialog.show();
        favwords = favwords + " \n"+wordToTrans;
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("favwords", ""+favwords);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Toast.makeText(Fb2ViewActivity.this, "Слово добавлено в словарь", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: failed update to db "+ e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(Fb2ViewActivity.this, "Не удалось добавить слово в словарь"+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void wordtranslate(String selectedLangId) throws InterruptedException {
        if (wordToTrans != null && !wordToTrans.equals("") && wordToTrans != " "){
            binding.extraTextView.setText("");
            TranslateTask task = new TranslateTask(Fb2ViewActivity.this, selectedLangId, wordToTrans, apiKey);
            task.execute();
            while (transText == "\""){
                TimeUnit.SECONDS.sleep(1);
            }
            binding.extraTextView.setText(wordToTrans + " - " + transText);
            transText = "\"";
            PdfViewActivity.transText = "\"";
            activity_fav_words.transText = "\"";
        }
    }

    private void loadUserInfo(){
        firebaseAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "loadUserInfo: Loading user info of user" + firebaseAuth.getUid());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            favwords = ""+snapshot.child("favwords").getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    private String[] splitStringByLength(String inputString, int length) {
        int substringsCount = (int) Math.ceil((double) inputString.length() / length);
        String[] substrings = new String[substringsCount];

        int startIndex = 0;

        for (int i = 0; i < substringsCount; i++) {
            int endIndex = Math.min(startIndex + length, inputString.length());

            if (endIndex < inputString.length()) {
                while (endIndex > startIndex && !isEndingCharacter(inputString.charAt(endIndex - 1))) {
                    endIndex--;
                }
            }

            substrings[i] = inputString.substring(startIndex, endIndex);
            startIndex = endIndex;
        }

        return substrings;
    }

    private boolean isEndingCharacter(char character) {
        return character == '.' || character == '!' || character == '?';
    }

    private void loadBookDetails() {

        if (Objects.equals(isUserBook, "1")){
            Log.d(TAG, "loadBookDetails: get doc url from db...");
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Books").child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String pdfUrl = ""+snapshot.child("url").getValue();
                    Log.d(TAG, "onDataChange: doc URL " + pdfUrl);
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
                if (Objects.equals(format, "application/x-fictionbook+xml") ||format.equals("text/plain")){
                    detectCharset(bytes);
                }
                else {
                    try {
                        readPdfFile(bytes);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                finalBytes = bytes;


                //binding.fbViewTv.setText(fb2Text);
                //binding.progressBar.setVisibility(View.GONE);
                //binding.progressTv.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: "+e.getMessage());
                binding.progressBar.setVisibility(View.GONE);
                binding.progressTv.setVisibility(View.GONE);
            }
        });
    }

    private void detectCharset(byte[] fileBytes) {
        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            byte[] bytes = new byte[4096];
            UniversalDetector detector = new UniversalDetector(null);
            int nread;
            while ((nread = inputStream.read(bytes)) > 0 && !detector.isDone()) {
                detector.handleData(bytes, 0, nread);
            }
            detector.dataEnd();
            String chars = detector.getDetectedCharset();
            readFb2text(fileBytes, chars);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        // Метод для чтения содержимого текстового файла, извлечения данных,кодировки в UTF-8 и записи в переменную String
    private void readFb2text(byte[] fileBytes, String charset) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(fileBytes), charset))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            String content =  builder.toString();
            if (Objects.equals(format, "text/plain")){
                if (Objects.equals(toTrans, "0")){
                    String mimeType = "text/html";
                    String encoding = "UTF-8";
                    binding.fbViewTv.loadDataWithBaseURL("", content, mimeType, encoding, null);
                    finalText = content;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.progressTv.setVisibility(View.GONE);

                }
                else{
                    translatedToWebView(content);
                }
            }
            else{
                textFromFb2(content);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void textFromFb2(String content) throws InterruptedException {
        int start = content.indexOf("<description>");
        int end = content.lastIndexOf("</description>");

        end = end + 14;

        char[] dest = new char[end - start];
        content.getChars(start, end, dest, 0);
        String description = new String(dest);

// Получение текста между тегами body
        int startBody = content.indexOf("<body>");
        int endBody = content.lastIndexOf("</body>");

        endBody = endBody + 7;

        char[] dst = new char[endBody - startBody];
        content.getChars(startBody, endBody, dst, 0);
        String body = new String(dst);
        description = description;
        String fullText = (description + "\n" + body).replace("</","\n</");
        /*fullText = fullText
                .replace("</description>", "\n")
                .replace("<description>","\n")
                .replace("</title-info>", "\n")
                .replace("<title-info>", "\n")
                .replace("</genre>", "\n")
                .replace("<genre>", "\n")
                .replace("</author>", "\n")
                .replace("<author>", "\n")
                .replace("</first-name>", "\n")
                .replace("<first-name>", "\n")
                .replace("</last-name>", "\n")
                .replace("<last-name>", "\n")
                .replace("</book-title>", "\n")
                .replace("<book-title>", "\n")
                .replace("</annotation>", "\n")
                .replace("<annotation>", "\n")
                .replace("</p>", "\n")
                .replace("<p>", "\n")
                .replace("</date>", "\n")
                .replace("<date>", "\n")
                .replace("</coverpage>", "\n")
                .replace("<coverpage>", "\n")
                .replace("</lang>", "\n")
                .replace("<lang>", "\n")
                .replace("</title-info>", "\n")
                .replace("<title-info>", "\n")
                .replace("</publish-info>", "\n")
                .replace("<publish-info>", "\n")
                .replace("</book-name>", "\n")
                .replace("<book-name>", "\n")
                .replace("</publisher>", "\n")
                .replace("<publisher>", "\n")
                .replace("</year>", "\n")
                .replace("<year>", "\n")
                .replace("</isbn>", "\n")
                .replace("<isbn>", "\n")
                .replace("</body>", "\n")
                .replace("<body>", "\n")
                .replace("</title>", "\n")
                .replace("<title>", "\n")
                .replace("</empty-line>", "\n")
                .replace("<empty-line />", "\n")
                .replace("<empty-line>", "\n")
                .replace("</epigraph>", "\n")
                .replace("<epigraph>", "\n")
                .replace("</section", "\n")
                .replace("<section>", "\n")
                .replace("</stanza>", "\n")
                .replace("<stanza>", "\n")
                .replace("</v>", "\n")
                .replace("<v>", "\n")
                .replace("</poem>", "\n")
                .replace("<poem>", "\n")
                .replace("</text-author>", "\n")
                .replace("<text-author>", "\n")
                .replace("sf_action", "\n")
                .replace("</emphasis>", "\n")
                .replace("<emphasis>", "\n")
                .replace("</document-info>", "\n")
                .replace("<document-info>", "\n")
                .replace("</nickname>", "\n")
                .replace("<nickname>", "\n")
                .replace("</program-used>", "\n")
                .replace("<program-used>", "\n")
                .replace("</src-ocr>", "\n")
                .replace("<src-ocr>", "\n")
                .replace("</keywords>", "\n")
                .replace("<keywords>", "\n")
                .replace("</middle-name>", "\n")
                .replace("<middle-name>", "\n")
                .replace("</history>", "\n")
                .replace("<history>", "\n")
                .replace("</id>", "\n")
                .replace("<id>", "\n")
                .replace("prose_classic", "\n")
                .replace("<emphasis>", "\n")
                .replace("</subtitle>", "\n")
                .replace("<subtitle>", "\n");*/
        String mimeType = "text/html";
        String encoding = "UTF-8";
        if (Objects.equals(toTrans, "0")){
            binding.fbViewTv.loadDataWithBaseURL("", fullText, mimeType, encoding, null);
            finalText = fullText;
            binding.progressBar.setVisibility(View.GONE);
            binding.progressTv.setVisibility(View.GONE);
        }
        else{
            translatedToWebView(fullText);
        }


    }

    private void readPdfFile(byte[] pdfBytes) throws InterruptedException {
        String readedText = "";
        PdfReader reader = null;
        try {
            reader = new PdfReader(pdfBytes);
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                sb.append(PdfTextExtractor.getTextFromPage(reader, i));
            }
            readedText = sb.toString();
            //String res = text.replace("\"", "|||||");

            //return res;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        //String text = transText.toString();
        translatedToWebView(readedText);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void translatedToWebView(String textToTrans) throws InterruptedException {
        Document doc = Jsoup.parse(textToTrans);
        String text = doc.text();
        String[] substrings = splitStringByLength(text, 10000);
        String fullTrans = "";
        float percente = 100f / substrings.length;
        float progress = 0f;
        for (String substring : substrings) {
            final String textToProgress ="Текст обработан на "+ String.format("%.1f", progress) + "%";
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    binding.progressTv.setText(textToProgress);
                }
            });
            binding.progressTv.post(new Runnable() {
                public void run() {
                    binding.progressTv.setText(textToProgress);
                }
            });
            TranslateTask task = new TranslateTask(Fb2ViewActivity.this, selectedLang, substring, apiKey);
            task.execute();
            while (transText == "\""){
                TimeUnit.SECONDS.sleep(1);
            }
            fullTrans = fullTrans + transText;
            transText = "\"";
            PdfViewActivity.transText = "\"";
            activity_fav_words.transText = "\"";
            progress = progress + percente;
        }

        String mimeType = "text/html";
        String encoding = "UTF-8";

        binding.fbViewTv.loadDataWithBaseURL("", fullTrans, mimeType, encoding, null);
        finalText = fullTrans;
        binding.progressBar.setVisibility(View.GONE);
        binding.progressTv.setVisibility(View.GONE);

    }


    private void moreDialog() {
        String[] categoriesArray = {"Скачать"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Сохранить на устройство").setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createTxt(finalText, bookTitle, Fb2ViewActivity.this);

            }
        }).show();

    }

    private void createTxt(String finalText, String bookTitle, Fb2ViewActivity fb2ViewActivity) {
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
            saveFileToDevice(fileBytes, bookTitle, Fb2ViewActivity.this);
            // Использование массива байтов по вашему усмотрению

            // Удаление временного файла


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void  saveFileToDevice(byte[] fileBytes, String bookTitle, Context context) {
        Log.d(TAG_DOWNLOAD, "downloadBook: downloading book...");

        String nameWithExtension = bookTitle + " "+ langOfDoc + ".txt";
        Log.d(TAG_DOWNLOAD, "downloadBook: NAME: " + nameWithExtension);



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


        }
        catch (Exception e){
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: failed saving to downloads " + e.getMessage());
            Toast.makeText(context, "Не удалось загрузить книгу +" + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

}
