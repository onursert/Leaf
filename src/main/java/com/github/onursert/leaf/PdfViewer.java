package com.github.onursert.leaf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class PdfViewer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Context context;
    SharedPreferences sharedPreferences;

    int pageCount = 0;
    int pageNumber = 0;

    DrawerLayout drawer;
    NavigationView navigationViewContent;

    String path;
    String pdfTitle;

    OpenPdf openPdf;

    RefreshPdf refreshPdf;

    SeekBar seekBar;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        context = getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //Toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Navigation Drawer
        drawer = findViewById(R.id.drawer_layout);
        navigationViewContent = findViewById(R.id.nav_view_content);
        if (navigationViewContent != null) {
            navigationViewContent.setNavigationItemSelectedListener(this);
        }

        //Open Pdf
        path = getIntent().getStringExtra("path");
        pdfTitle = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(pdfTitle);
        if (sharedPreferences.getBoolean("where_i_left", false) == true) {
            if (getIntent().getStringExtra("currentPage") != null) {
                pageNumber = Integer.parseInt(getIntent().getStringExtra("currentPage"));
            } else {
                pageNumber = 0;
            }
        }
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.seekLayout);
        CustomImageView imageView = (CustomImageView) findViewById(R.id.imageView);
        openPdf = new OpenPdf(context, imageView, path, pageNumber);
        pageCount = openPdf.pdfRenderer.getPageCount();
        for (int i = 0; i < pageCount; i++) {
            navigationViewContent.getMenu().add(Integer.toString(i));
            navigationViewContent.getMenu().getItem(i).setCheckable(true);
        }

        refreshPdf = MainActivity.getInstance().refreshPdf;

        //Seekbar
        final TextView textViewNumber = (TextView) findViewById(R.id.textViewNumber);
        textViewNumber.setText(pageNumber + "/" + (pageCount - 1));
        navigationViewContent.getMenu().getItem(pageNumber).setChecked(true);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        imageView.setParameters(context, toolbar, getWindow(), relativeLayout, openPdf, pageNumber, seekBar);
        seekBar.setMax(pageCount - 1);
        seekBar.setProgress(pageNumber);
        seekBar.setPadding(100, 0, 100, 0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pageNumber = progress;
                textViewNumber.setText(pageNumber + "/" + (pageCount - 1));
                navigationViewContent.getMenu().getItem(pageNumber).setChecked(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                openPdf.showPage(pageNumber);
            }
        });

        checkSharedPreferences();
    }

    //On Activity Stop
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public void onStop() {
        try {
            refreshPdf.addCurrentPage(refreshPdf.pdfList, path, pageNumber);
            refreshPdf.addCurrentPage(refreshPdf.customAdapter.searchedPdfList, path, pageNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            openPdf.closePdfRenderer();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Unable to close - " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finish();
        super.onStop();
    }

    //Check Shared Preferences
    public void checkSharedPreferences() {
        if (sharedPreferences.getBoolean("keep_screen_on", false) == true) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (sharedPreferences.getBoolean("rotation_lock", false) == true) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkSharedPreferences();
    }

    //Navigation Item Clicked
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        for (int i = 0; i < pageCount; i++) {
            if (Integer.toString(i).equals(menuItem.toString())) {
                pageNumber = i;
                seekBar.setProgress(pageNumber);
                openPdf.showPage(pageNumber);
                break;
            }
        }
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }
}
