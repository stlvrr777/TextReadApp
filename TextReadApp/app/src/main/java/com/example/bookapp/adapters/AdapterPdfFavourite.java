package com.example.bookapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.activities.DocDetailActivity;
import com.example.bookapp.databinding.RowPdfFavouriteBinding;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class AdapterPdfFavourite extends RecyclerView.Adapter<AdapterPdfFavourite.HolderPdfFavourite>{

    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;

    private RowPdfFavouriteBinding binding;

    private static final String TAG = "FAV_BOOK_TAG";

    public AdapterPdfFavourite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavourite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfFavouriteBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfFavourite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavourite holder, int position) {

        ModelPdf model = pdfArrayList.get(position);

        loadBookDetails(model, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DocDetailActivity.class);
                intent.putExtra("bookId", model.getId());
                context.startActivity(intent);
            }
        });

        holder.removeFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavourite(context, model.getId());
            }
        });

    }

    private void loadBookDetails(ModelPdf model, HolderPdfFavourite holder) {
        String bookId = model.getId();
        Log.d(TAG, "loadBookDetails: book details of book id"+ bookId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String bookTitle = ""+snapshot.child("title").getValue();
                String description = ""+snapshot.child("description").getValue();
                String categoryId = ""+snapshot.child("categoryId").getValue();
                String bookUrl = ""+snapshot.child("url").getValue();
                String timestamp = ""+snapshot.child("timestamp").getValue();
                String uid = ""+snapshot.child("uid").getValue();
                String format = ""+snapshot.child("format").getValue();
                String viewsCount = ""+snapshot.child("viewsCount").getValue();
                String downloadsCount = ""+snapshot.child("downloadsCount").getValue();

                model.setFavourite(true);
                model.setTitle(bookTitle);
                model.setDescription(description);
                model.setTimestamp(Long.parseLong(timestamp));
                model.setCategoryId(categoryId);
                model.setUid(uid);
                model.setUrl(bookUrl);
                model.setFormat(format);

                String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                if (Objects.equals(format, "application/x-fictionbook+xml")){
                    MyApplication.loadFb2FromUrlSinglePage(""+bookUrl,""+bookTitle,holder.progressBar,holder.fb2View);
                    holder.pdfView.setVisibility(View.INVISIBLE);
                }
                else if(Objects.equals(format, "text/plain"))
                {
                    MyApplication.txtSinglePage(holder.progressBar,holder.fb2View);
                    holder.pdfView.setVisibility(View.INVISIBLE);
                    //binding.progressBar.setVisibility(View.GONE);
                }
                else{
                    MyApplication.loadPdfFromUrlSinglePage(""+bookUrl,""+bookTitle, holder.pdfView, holder.progressBar,null);
                    holder.fb2View.setVisibility(View.INVISIBLE);
                }

                MyApplication.loadCategory(""+categoryId, holder.categoryTv);
                MyApplication.loadPdfSize(""+bookUrl, ""+bookTitle, holder.sizeTv);

                holder.titleTv.setText(bookTitle);
                holder.descriptionTv.setText(description);
                holder.dateTv.setText(date);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderPdfFavourite extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ImageView fb2View;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton removeFavBtn;

        public HolderPdfFavourite(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            fb2View = binding.fb2View;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            removeFavBtn = binding.removeFavBtn;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;


        }
    }

}
