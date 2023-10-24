package com.example.bookapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.activities.DocDetailActivity;
import com.example.bookapp.databinding.RowPdfUserBinding;
import com.example.bookapp.filters.FilterPdfUser;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;
import java.util.Objects;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {

    private Context context;

    public ArrayList<ModelPdf> pdfArrayList, filterList;

    private FilterPdfUser filter;

    private RowPdfUserBinding binding;

    private static final String TAG = "ADAPTER_PDF_USER_TAG";

    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }



    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfUser(binding.getRoot());

    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {
        ModelPdf model = pdfArrayList.get(position);
        String bookId = model.getId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfurl = model.getUrl();
        String categoryId = model.getCategoryId();
        String format = model.getFormat();
        long timestamp = model.getTimestamp();

        String date = MyApplication.formatTimestamp(timestamp);

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);

        if (Objects.equals(format, "application/x-fictionbook+xml")){
            MyApplication.loadFb2FromUrlSinglePage(""+pdfurl,""+title,holder.progressBar,holder.fb2View);
            holder.pdfView.setVisibility(View.INVISIBLE);
        }
        else if(Objects.equals(format, "text/plain"))
        {
            MyApplication.txtSinglePage(holder.progressBar,holder.fb2View);
            holder.pdfView.setVisibility(View.INVISIBLE);
            //binding.progressBar.setVisibility(View.GONE);
        }
        else{
            MyApplication.loadPdfFromUrlSinglePage(""+pdfurl,""+title, holder.pdfView, holder.progressBar,null);
            holder.fb2View.setVisibility(View.INVISIBLE);
        }
        MyApplication.loadCategory(""+categoryId, holder.categoryTv);
        MyApplication.loadPdfSize(""+pdfurl,""+title, holder.sizeTv);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DocDetailActivity.class);
                if (categoryId == null){
                    intent.putExtra("isUserBook", "1");
                }
                intent.putExtra("bookId", bookId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter = new FilterPdfUser(filterList, this);
        }
        return filter;
    }


    class HolderPdfUser extends RecyclerView.ViewHolder {

        ImageView fb2View;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        PDFView pdfView;
        ProgressBar progressBar;

        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);
            fb2View = binding.fb2View;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;

        }
    }
}
