package com.github.onursert.leaf;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> implements Filterable {

    Context context;
    List<List> pdfList;

    RefreshPdf refreshPdf;

    LayoutInflater inflater;
    int position;

    public List<List> searchedPdfList;

    public CustomAdapter(Context context, List<List> pdfList) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.pdfList = pdfList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        List pdfInfo = pdfList.get(i);
        try {
            myViewHolder.setData(pdfInfo);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return pdfList.size();
    }

    boolean refreshingDoneBool = false;
    public void refreshingDone(List<List> pdfList) {
        searchedPdfList = new ArrayList<>(pdfList);
        refreshingDoneBool = true;
    }
    @Override
    public Filter getFilter() {
        return filter;
    }
    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<List> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(searchedPdfList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (List infos : searchedPdfList) {
                    if (infos.get(0).toString().toLowerCase().contains(filterPattern)) {
                        filteredList.add(infos);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (refreshingDoneBool) {
                pdfList.clear();
                pdfList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, importTime, openTime;
        ImageView image;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.pdfTitle);
            image = (ImageView) view.findViewById(R.id.pdfCover);
            importTime = (TextView) view.findViewById(R.id.pdfImportTime);
            openTime = (TextView) view.findViewById(R.id.pdfOpenTime);
            refreshPdf = MainActivity.getInstance().refreshPdf;

            updateViews();

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    position = getLayoutPosition();
                    return false;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentPdfViewer = new Intent(context, PdfViewer.class);
                    intentPdfViewer.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intentPdfViewer.putExtra("title", pdfList.get(getLayoutPosition()).get(0).toString());
                    intentPdfViewer.putExtra("path", pdfList.get(getLayoutPosition()).get(2).toString());
                    intentPdfViewer.putExtra("currentPage", pdfList.get(getLayoutPosition()).get(5).toString());
                    context.startActivity(intentPdfViewer);

                    String openTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                    try {
                        String clickedPdfPath = pdfList.get(getLayoutPosition()).get(2).toString();
                        refreshPdf.addOpenTime(pdfList, clickedPdfPath, openTime);
                        if (refreshingDoneBool) {
                            refreshPdf.addOpenTime(searchedPdfList, clickedPdfPath, openTime);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void setData(List pdfInfo) throws ParseException {
            title.setText(pdfInfo.get(0).toString());
            Picasso.get().load("file://" + pdfInfo.get(1).toString()).error(R.drawable.ic_book_black_24dp).resize(240, 320).into(image);
            importTime.setText("Import: " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new SimpleDateFormat("yyyyMMdd_HHmmss").parse(pdfInfo.get(3).toString())));
            openTime.setText("Open: " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new SimpleDateFormat("yyyyMMdd_HHmmss").parse(pdfInfo.get(4).toString())));
        }

        public void updateViews() {
            if (refreshPdf.getFromPreferences("showHideImportTime").equals("Invisible")) {
                importTime.setVisibility(View.INVISIBLE);
            } else if (refreshPdf.getFromPreferences("showHideImportTime").equals("Visible")) {
                importTime.setVisibility(View.VISIBLE);
            } else {
                importTime.setVisibility(View.VISIBLE);
            }
            if (refreshPdf.getFromPreferences("showHideOpenTime").equals("Invisible")) {
                openTime.setVisibility(View.INVISIBLE);
            } else if (refreshPdf.getFromPreferences("showHideOpenTime").equals("Visible")) {
                openTime.setVisibility(View.VISIBLE);
            } else {
                openTime.setVisibility(View.VISIBLE);
            }
        }
    }
}
