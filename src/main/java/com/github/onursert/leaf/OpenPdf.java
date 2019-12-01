package com.github.onursert.leaf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class OpenPdf {

    Context context;
    ImageView imageView;
    String pdfPath;
    int pageNumber;

    ParcelFileDescriptor parcelFileDescriptor;
    PdfRenderer pdfRenderer;
    PdfRenderer.Page pdfRendererPage;

    public OpenPdf(Context context, ImageView imageView, String pdfPath, int pageNumber) {
        this.context = context;
        this.imageView = imageView;
        this.pdfPath = pdfPath;
        this.pageNumber = pageNumber;

        try {
            openPdfRenderer();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Unable to open - " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        showPage(pageNumber);
    }

    public void openPdfRenderer() throws IOException {
        File file = new File(pdfPath);
        if (!file.exists()) {
            InputStream asset = context.getAssets().open(pdfPath);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        if (parcelFileDescriptor != null) {
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        }
    }
    public void closePdfRenderer() throws IOException {
        if (pdfRendererPage != null) {
            pdfRendererPage.close();
        }
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
        if (parcelFileDescriptor != null) {
            parcelFileDescriptor.close();
        }
    }

    public void showPrevious() {
        if (pdfRenderer == null || pdfRendererPage == null) {
            return;
        }
        final int index = pdfRendererPage.getIndex();
        if (index > 0) {
            showPage(index - 1);
        }
    }
    public void showNext() {
        if (pdfRenderer == null || pdfRendererPage == null) {
            return;
        }
        final int index = pdfRendererPage.getIndex();
        if (index + 1 < pdfRenderer.getPageCount()) {
            showPage(index + 1);
        }
    }

    public void showPage(int index) {
        if (pdfRendererPage != null) {
            pdfRendererPage.close();
        }
        pdfRendererPage = pdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(context.getResources().getDisplayMetrics().densityDpi * pdfRendererPage.getWidth() / 144, context.getResources().getDisplayMetrics().densityDpi * pdfRendererPage.getHeight() / 144, Bitmap.Config.ARGB_8888);
        pdfRendererPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        imageView.setImageBitmap(bitmap);
    }
}
