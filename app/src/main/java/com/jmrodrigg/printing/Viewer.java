package com.jmrodrigg.printing;

import com.jmrodrigg.printing.model.PrintJob;
import com.jmrodrigg.printing.samples.PrintCustomContent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

/**
 * Author: jrodriguezg
 * Date: 12/04/2015.
 */
public class Viewer extends Activity {

    private ImageView mView;
    private int mCurrentPage;
    private Button mPrevButton, mNextButton;
    private PrintJob mPrintJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        mPrintJob = getIntent().getParcelableExtra(PrintingConstants.PRINT_JOB_CLASS);

        // ImageView:
        mView = (ImageView) findViewById(R.id.imageView);

        // Buttons:
        mPrevButton = (Button) findViewById(R.id.prevPage);
        mNextButton = (Button) findViewById(R.id.nextPage);

        // Content Type:
        switch(mPrintJob.getMimeType()) {
            case DOCUMENT:
                renderDocument();
                break;
            case IMAGE:
                renderImage();
                break;
        }
    }

    private void renderDocument() {
        try {
            File f = new File(mPrintJob.getUri());

            if(!f.exists()) this.finish();

            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f,ParcelFileDescriptor.MODE_READ_ONLY);
            ((PrintingApplication)getApplication()).renderer = new Renderer(pfd);
        }catch(IOException ex) {
            ex.printStackTrace();
            this.finish();
        }

        mCurrentPage = 0;

        // Buttons:
        mPrevButton.setVisibility(View.VISIBLE);
        mPrevButton.setEnabled(false);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNextButton.setEnabled(true);
                ((PrintingApplication)getApplication()).renderer.openPage(--mCurrentPage);
                if(mCurrentPage == 0) mPrevButton.setEnabled(false);
                renderPage();
            }
        });
        mPrevButton.setVisibility(View.VISIBLE);
        mNextButton.setEnabled(((PrintingApplication)getApplication()).renderer.getPageCount() > 1);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPrevButton.setEnabled(true);
                ((PrintingApplication)getApplication()).renderer.openPage(++mCurrentPage);
                if(mCurrentPage == (((PrintingApplication)getApplication()).renderer.getPageCount() -1)) mNextButton.setEnabled(false);
                renderPage();
            }
        });

        // Render first page:
        renderPage();
    }

    private void renderPage() {
        int [] dimensions = ((PrintingApplication)getApplication()).renderer.openPage(mCurrentPage);
        Bitmap bmp = Bitmap.createBitmap(dimensions[0],dimensions[1], Bitmap.Config.ARGB_8888);
        ((PrintingApplication)getApplication()).renderer.renderPage(bmp,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        mView.setMaxWidth(dimensions[0]);mView.setMinimumWidth(dimensions[0]);
        mView.setMaxHeight(dimensions[1]);mView.setMinimumHeight(dimensions[1]);
        mView.setBackgroundColor(Color.WHITE);
        mView.setImageBitmap(bmp);
    }

    private void renderImage() {
        Bitmap bmp = BitmapFactory.decodeFile(mPrintJob.getUri());
        mView.setImageBitmap(bmp);

        mPrevButton.setVisibility(View.GONE);
        mNextButton.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem selectedItem) {
        Intent intent;

        switch(selectedItem.getItemId()) {
            case R.id.action_print:
                intent = new Intent(this,PrintingSettingsActivity.class);
                intent.putExtra(PrintingConstants.PRINT_JOB_CLASS,mPrintJob);
                startActivityForResult(intent,PrintingConstants.ACTION_PRINT);
                return true;
        }

        return super.onOptionsItemSelected(selectedItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case PrintingConstants.ACTION_PRINT:
                // Update the print job with the settings selected:
                mPrintJob = intent.getParcelableExtra(PrintingConstants.PRINT_JOB_CLASS);
                break;
        }
    }
}
