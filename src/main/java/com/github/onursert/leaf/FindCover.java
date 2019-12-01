package com.github.onursert.leaf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FindCover {

    Context context;
    String pdfPath;

    ParcelFileDescriptor parcelFileDescriptor;
    PdfRenderer pdfRenderer;
    PdfRenderer.Page pdfRendererPage;

    public Bitmap FindCover(Context context, String pdfPath) {
        this.context = context;
        this.pdfPath = pdfPath;

        try {
            openPdfRenderer();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Unable to open - " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return showPage(0);
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

    public Bitmap showPage(int index) {
        if (pdfRendererPage != null) {
            pdfRendererPage.close();
        }
        pdfRendererPage = pdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(pdfRendererPage.getWidth(), pdfRendererPage.getHeight(), Bitmap.Config.ARGB_8888);
        pdfRendererPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        return bitmap;
    }
}
