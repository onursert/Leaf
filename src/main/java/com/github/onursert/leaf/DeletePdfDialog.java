package com.github.onursert.leaf;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

import java.io.IOException;

public class DeletePdfDialog extends Dialog implements View.OnClickListener {

    public Activity activity;
    private String pdfPath;
    RefreshPdf refreshPdf;
    CustomAdapter customAdapter;

    public Button delete;
    public Button cancel;
    public CheckBox deleteDevice;

    public DeletePdfDialog(Activity activity, String pdfPath, RefreshPdf refreshPdf, CustomAdapter customAdapter) {
        super(activity);
        this.activity = activity;
        this.pdfPath = pdfPath;
        this.refreshPdf = refreshPdf;
        this.customAdapter = customAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.delete_pdf_dialog);

        deleteDevice = (CheckBox) findViewById(R.id.deleteCheckBox);

        delete = (Button) findViewById(R.id.deleteButton);
        delete.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.cancelDeleteButton);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deleteButton:
                try {
                    refreshPdf.deletePdf(refreshPdf.pdfList, pdfPath, deleteDevice.isChecked());
                    refreshPdf.deletePdf(customAdapter.searchedPdfList, pdfPath, deleteDevice.isChecked());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.cancelDeleteButton:
                dismiss();
                break;

            default:
                break;
        }
        dismiss();
    }
}
