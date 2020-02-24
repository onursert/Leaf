package com.github.onursert.leaf;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class RefreshPdf {

    Context context;
    CustomAdapter customAdapter;
    List<List> pdfList;

    public RefreshPdf(Context context, CustomAdapter customAdapter, List<List> pdfList) {
        this.context = context;
        this.customAdapter = customAdapter;
        this.pdfList = pdfList;
    }

    //Custom Shared Preferences
    public static final String myPref = "preferenceName";
    public String getFromPreferences(String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(myPref, 0);
        String str = sharedPreferences.getString(key, "null");
        return str;
    }
    public void setToPreferences(String key, String thePreference) {
        SharedPreferences.Editor editor = context.getSharedPreferences(myPref, 0).edit();
        editor.putString(key, thePreference);
        editor.commit();
    }

    //Read File From Internal Storage
    public void readFileFromInternalStorage() throws IOException {
        FileInputStream fileInputStream = context.openFileInput("pdfList.txt");
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] arrOfLine = line.split("½½");
            List pdfInfo = new LinkedList();
            pdfInfo.add(arrOfLine[0]); //pdfTitle
            pdfInfo.add(arrOfLine[1]); //pdfCover
            pdfInfo.add(arrOfLine[2]); //pdfPath
            pdfInfo.add(arrOfLine[3]); //importTime
            pdfInfo.add(arrOfLine[4]); //openTime
            pdfInfo.add(arrOfLine[5]); //currentPage

            pdfList.add(pdfInfo);
        }
        fileInputStream.close();
        inputStreamReader.close();
        bufferedReader.close();
    }

    /*Search Begin*/
    FindCover findCover = new FindCover();

    File file;
    File fileImages;

    FileOutputStream fileOutputStream;
    FileOutputStream fileOutputStreamImages;
    OutputStreamWriter writer;

    Bitmap bitmap;

    public void SearchPdf() throws IOException {
        file = new File(context.getFilesDir(), "pdfList.txt");
        fileImages = new File(context.getFilesDir() + File.separator + "pdfImages");

        if (!file.exists()) {
            file.createNewFile();
        }
        fileImages.mkdirs();

        fileOutputStream = new FileOutputStream(file, false);
        writer = new OutputStreamWriter(fileOutputStream);

        bitmap = null;

        isRemoved(0);
        FindPdf(Environment.getExternalStorageDirectory());
        sortByPreferences(pdfList);

        writer.close();
        if (fileOutputStream != null) {
            fileOutputStream.flush();
            fileOutputStream.close();
        }

        if (fileOutputStreamImages != null) {
            fileOutputStreamImages.flush();
            fileOutputStreamImages.close();
        }
    }
    public void isRemoved(int turn) {
        for (int i = turn; i < pdfList.size(); i++) {
            File file = new File((String) pdfList.get(i).get(2));
            if (!file.exists()) {
                pdfList.remove(i);
                removedCount++;
                isRemoved(i);
            }
        }
    }
    public void FindPdf(File dir) throws IOException {
        File listFile[] = dir.listFiles();
        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    FindPdf(listFile[i]);
                } else {
                    if (listFile[i].getName().endsWith(".pdf") || listFile[i].getName().endsWith(".PDF")) {
                        if (!isExist(listFile[i].getAbsolutePath())) {
                            String title = listFile[i].getName().substring(0, listFile[i].getName().length() - 4);
                            String imageName = title + ".png";
                            File imageItem = new File(fileImages, imageName);
                            if (!imageItem.exists()) {
                                bitmap = (Bitmap) findCover.FindCover(context, listFile[i].getAbsolutePath());
                                if (bitmap != null) {
                                    fileOutputStreamImages = new FileOutputStream(imageItem);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStreamImages);
                                } else {
                                    File fileNull = new File("null");
                                    imageItem = fileNull;
                                }
                            }

                            String importTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

                            List pdfInfo = new LinkedList();
                            pdfInfo.add(title); //pdfTitle
                            pdfInfo.add(imageItem); //pdfCover
                            pdfInfo.add(listFile[i].getAbsolutePath()); //pdfPath
                            pdfInfo.add(importTime); //importTime
                            pdfInfo.add("19200423_000000"); //openTime
                            pdfInfo.add(0); //currentPage

                            if (fileOutputStreamImages != null) {
                                fileOutputStreamImages.flush();
                                fileOutputStreamImages.close();
                            }

                            addedCount++;
                            pdfList.add(pdfInfo);
                        }
                    }
                }
            }
        }
    }
    public boolean isExist(String path) {
        for (int i = 0; i < pdfList.size(); i++) {
            if (pdfList.get(i).get(2).equals(path)) {
                return true;
            }
        }
        return false;
    }
    public void sortByPreferences(List<List> pdfList) throws IOException {
        if (getFromPreferences("sort").equals("sortTitle")) {
            sortTitle(pdfList);
        } else if (getFromPreferences("sort").equals("sortImportTime")) {
            sortImportTime(pdfList);
        } else if (getFromPreferences("sort").equals("sortOpenTime")) {
            sortOpenTime(pdfList);
        } else {
            sortTitle(pdfList);
        }
    }
    /*Search End*/

    //Menu Refresh, Sort, Show/Hide
    int addedCount;
    int removedCount;
    final Handler handler = new Handler();
    class refreshPdfs extends AsyncTask<Void, Void, Void> {
        Toast toast = Toast.makeText(context, "Searching...", Toast.LENGTH_LONG);

        @Override
        protected void onPreExecute() {
            addedCount = 0;
            removedCount = 0;
            customAdapter.refreshingDoneBool = false;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.show();
                    customAdapter.notifyDataSetChanged();
                    handler.postDelayed(this, 1000);
                }
            }, 1000);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                SearchPdf();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            handler.removeCallbacksAndMessages(null);
            toast.cancel();
            customAdapter.notifyDataSetChanged();
            customAdapter.refreshingDone(pdfList);
            Toast.makeText(context, addedCount + " pdf(s) added and " + removedCount + " pdf(s) removed", Toast.LENGTH_SHORT).show();
            try {
                findCover.closePdfRenderer();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Unable to close - " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void sortTitle(List<List> pdfList) throws IOException {
        setToPreferences("sort", "sortTitle");
        Collections.sort(pdfList, new Comparator<List>() {
            @Override
            public int compare(List o1, List o2) {
                try {
                    return o1.get(0).toString().compareTo(o2.get(0).toString());
                } catch (NullPointerException e) {
                    return 0;
                }
            }
        });
        pdfListChanged(pdfList);
    }
    public void sortImportTime(List<List> pdfList) throws IOException {
        setToPreferences("sort", "sortImportTime");
        Collections.sort(pdfList, new Comparator<List>() {
            @Override
            public int compare(List o1, List o2) {
                try {
                    if (o2.get(3).toString().compareTo(o1.get(3).toString()) == 0) {
                        return o1.get(0).toString().compareTo(o2.get(0).toString());
                    } else {
                        return o2.get(3).toString().compareTo(o1.get(3).toString());
                    }
                } catch (NullPointerException e) {
                    return 0;
                }
            }
        });
        pdfListChanged(pdfList);
    }
    public void sortOpenTime(List<List> pdfList) throws IOException {
        setToPreferences("sort", "sortOpenTime");
        Collections.sort(pdfList, new Comparator<List>() {
            @Override
            public int compare(List o1, List o2) {
                try {
                    return o2.get(4).toString().compareTo(o1.get(4).toString());
                } catch (NullPointerException e) {
                    return 0;
                }
            }
        });
        pdfListChanged(pdfList);
    }

    //Write PdfList To Cache
    public void pdfListChanged(List<List> pdfList) throws IOException {
        file = new File(context.getFilesDir(), "pdfList.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        fileOutputStream = new FileOutputStream(file, false);
        writer = new OutputStreamWriter(fileOutputStream);
        updateCache(pdfList);
        writer.close();
        if (fileOutputStream != null) {
            fileOutputStream.flush();
            fileOutputStream.close();
        }
    }
    public void updateCache(List<List> pdfList) throws IOException {
        for (int i = 0; i < pdfList.size(); i++) {
            writer.append(pdfList.get(i).get(0) + "½½" + pdfList.get(i).get(1) + "½½" + pdfList.get(i).get(2) + "½½" + pdfList.get(i).get(3) + "½½" + pdfList.get(i).get(4) + "½½" + pdfList.get(i).get(5) + "\r\n");
        }
    }

    //Functions Which Come From Another Class
    public void addOpenTime(List<List> pdfList, String path, String openTime) throws IOException {
        if (pdfList != null) {
            final int pdfListSize = pdfList.size();
            for (int i = 0; i < pdfListSize; i++) {
                if (pdfList.get(i).get(2).equals(path)) {
                    List pdfInfo = new LinkedList();
                    pdfInfo.add(pdfList.get(i).get(0)); //pdfTitle
                    pdfInfo.add(pdfList.get(i).get(1)); //pdfCover
                    pdfInfo.add(pdfList.get(i).get(2)); //pdfPath
                    pdfInfo.add(pdfList.get(i).get(3)); //importTime
                    pdfInfo.add(openTime); //openTime
                    pdfInfo.add(pdfList.get(i).get(5)); //currentPage
                    pdfList.set(i, pdfInfo);
                    break;
                }
            }
            sortByPreferences(pdfList);
            customAdapter.notifyDataSetChanged();
        }
    }
    public void editPdf(List<List> pdfList, String title, String path) throws IOException {
        if (pdfList != null) {
            final int pdfListSize = pdfList.size();
            for (int i = 0; i < pdfListSize; i++) {
                if (pdfList.get(i).get(2).equals(path)) {
                    List pdfInfo = new LinkedList();
                    pdfInfo.add(title); //pdfTitle
                    pdfInfo.add(pdfList.get(i).get(1)); //pdfCover
                    pdfInfo.add(pdfList.get(i).get(2)); //pdfPath
                    pdfInfo.add(pdfList.get(i).get(3)); //importTime
                    pdfInfo.add(pdfList.get(i).get(4)); //openTime
                    pdfInfo.add(pdfList.get(i).get(5)); //currentPage
                    pdfList.set(i, pdfInfo);
                    break;
                }
            }
            sortByPreferences(pdfList);
            customAdapter.notifyDataSetChanged();
        }
    }
    public void deletePdf(List<List> pdfList, String path, Boolean deleteDevice) throws IOException {
        if (pdfList != null) {
            final int pdfListSize = pdfList.size();
            for (int i = 0; i < pdfListSize; i++) {
                if (pdfList.get(i).get(2).equals(path)) {
                    pdfList.remove(i);
                    if (deleteDevice) {
                        File file = new File(path);
                        file.delete();
                    }
                    break;
                }
            }
            sortByPreferences(pdfList);
            customAdapter.notifyDataSetChanged();
        }
    }
    public void addCurrentPage(List<List> pdfList, String path, Integer currentPage) throws IOException {
        if (pdfList != null) {
            final int pdfListSize = pdfList.size();
            for (int i = 0; i < pdfListSize; i++) {
                if (pdfList.get(i).get(2).equals(path)) {
                    List pdfInfo = new LinkedList();
                    pdfInfo.add(pdfList.get(i).get(0)); //pdfTitle
                    pdfInfo.add(pdfList.get(i).get(1)); //pdfCover
                    pdfInfo.add(pdfList.get(i).get(2)); //pdfPath
                    pdfInfo.add(pdfList.get(i).get(3)); //importTime
                    pdfInfo.add(pdfList.get(i).get(4)); //openTime
                    pdfInfo.add(currentPage); //currentPage
                    pdfList.set(i, pdfInfo);
                    break;
                }
            }
            sortByPreferences(pdfList);
            customAdapter.notifyDataSetChanged();
        }
    }
}
