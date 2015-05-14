package com.jmrodrigg.printing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
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
    private final static String doc = "/Download/AEC4_original.pdf";
    private final static String img = "/Download/tron.jpg";

    private ImageView mView;
    private int mCurrentPage;

    private Menu mMenu;

    Button mPrevButton, mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);


        // ImageView:
        mView = (ImageView) findViewById(R.id.imageView);

        // Buttons:
        mPrevButton = (Button) findViewById(R.id.prevPage);
        mNextButton = (Button) findViewById(R.id.nextPage);

        // Content Type:
        switch(((PrintingApplication)getApplication()).objectType) {
            case DOCUMENT:
                renderDocument(doc);
                break;
            case IMAGE:
                renderImage(img);
                break;
        }
    }

    private void renderDocument(String pPath) {
        ((PrintingApplication)getApplication()).objectType = PrintingApplication.JobType.DOCUMENT;

        File pathDir = Environment.getExternalStorageDirectory();
        ((PrintingApplication)getApplication()).filepath = pathDir.getAbsolutePath() + pPath;

        Log.d("JMRODRIGG", ((PrintingApplication)getApplication()).filepath);

        try {
            File f = new File(((PrintingApplication)getApplication()).filepath);

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

    private void renderImage(String pPath) {
        ((PrintingApplication)getApplication()).objectType = PrintingApplication.JobType.IMAGE;

        File pathDir = Environment.getExternalStorageDirectory();
        ((PrintingApplication)getApplication()).filepath = pathDir.getAbsolutePath() + pPath;

        Log.d("JMRODRIGG", ((PrintingApplication)getApplication()).filepath);

        Bitmap bmp = BitmapFactory.decodeFile(((PrintingApplication)getApplication()).filepath);
        mView.setImageBitmap(bmp);

        mPrevButton.setVisibility(View.GONE);
        mNextButton.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_viewer, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem selectedItem) {

        switch(selectedItem.getItemId()) {
            case R.id.action_print:
                Intent intent = new Intent(this,PrintingSettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.action_image:
                selectedItem.setVisible(false);
                mMenu.findItem(R.id.action_doc).setVisible(true);

                renderImage(img);
                break;

            case R.id.action_doc:
                selectedItem.setVisible(false);
                mMenu.findItem(R.id.action_image).setVisible(true);

                renderDocument(doc);
                break;
        }

        return super.onOptionsItemSelected(selectedItem);
    }
}
