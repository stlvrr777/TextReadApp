package com.example.bookapp.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.activities.DocDetailActivity;
import com.example.bookapp.activities.DocEditActivity;
import com.example.bookapp.databinding.RowPdfAdminBinding;
import com.example.bookapp.filters.FilterPdfAdmin;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;
import java.util.Objects;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

    private Context context;

    public ArrayList<ModelPdf> pdfArrayList, filterList;

    private RowPdfAdminBinding binding;

    private FilterPdfAdmin filter;

    private static final String TAG = "PDF_ADAPTER_TAG";

    private ProgressDialog progressDialog;

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Пожалуйста, подождите...");
        progressDialog.setCanceledOnTouchOutside(false);

    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPdfAdmin.HolderPdfAdmin holder, int position) {
        ModelPdf model = pdfArrayList.get(position);
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        String format = model.getFormat();
        long timestamp = model.getTimestamp();

        String formattedDate = MyApplication.formatTimestamp(timestamp);

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        if (Objects.equals(format, "application/x-fictionbook+xml")){
            MyApplication.loadFb2FromUrlSinglePage(""+pdfUrl,""+title,holder.progressBar,holder.fb2View);
            holder.pdfView.setVisibility(View.INVISIBLE);
        }
        else if(Objects.equals(format, "text/plain"))
        {
            MyApplication.txtSinglePage(holder.progressBar,holder.fb2View);
            holder.pdfView.setVisibility(View.INVISIBLE);
            //binding.progressBar.setVisibility(View.GONE);
        }
        else{
            MyApplication.loadPdfFromUrlSinglePage(""+pdfUrl,""+title, holder.pdfView, holder.progressBar,null);
            holder.fb2View.setVisibility(View.INVISIBLE);
        }

        MyApplication.loadCategory(""+categoryId, holder.categoryTv);
        MyApplication.loadPdfSize(""+pdfUrl,""+title, holder.sizeTv);

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model, holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DocDetailActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });

    }

    private void moreOptionsDialog(ModelPdf model, HolderPdfAdmin holder) {

        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        String[] options = {"Изменить","Удалить"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Опции").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    Intent intent = new Intent(context, DocEditActivity.class);
                    intent.putExtra("bookId",bookId);
                    context.startActivity(intent);


                }
                else if(which == 1){
                    MyApplication.deleteBook(context,
                            ""+bookId,
                            ""+bookUrl,
                            ""+bookTitle);
                }
            }
        }).show();
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{
        ImageView fb2View;
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv,categoryTv, sizeTv, dateTv;
        ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            fb2View = binding.fb2View;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;

        }
    }
}
