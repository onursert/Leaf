package com.github.onursert.leaf;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class EditPdfDialog extends Dialog implements View.OnClickListener {

    public Activity activity;
    private String pdfTitle;
    private String pdfPath;
    RefreshPdf refreshPdf;
    CustomAdapter customAdapter;

    public Button update;
    public Button cancel;
    public EditText title;

    public EditPdfDialog(Activity activity, String pdfTitle, String pdfPath, RefreshPdf refreshPdf, CustomAdapter customAdapter) {
        super(activity);
        this.activity = activity;
        this.pdfTitle = pdfTitle;
        this.pdfPath = pdfPath;
        this.refreshPdf = refreshPdf;
        this.customAdapter = customAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_pdf_dialog);

        title = (EditText) findViewById(R.id.editPdfTitle);
        title.setText(pdfTitle, TextView.BufferType.EDITABLE);

        update = (Button) findViewById(R.id.updateButton);
        update.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.cancelEditButton);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.updateButton:
                try {
                    refreshPdf.editPdf(refreshPdf.pdfList, title.getText().toString(), pdfPath);
                    refreshPdf.editPdf(customAdapter.searchedPdfList, title.getText().toString(), pdfPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.cancelEditButton:
                dismiss();
                break;

            default:
                break;
        }
        dismiss();
    }
}
