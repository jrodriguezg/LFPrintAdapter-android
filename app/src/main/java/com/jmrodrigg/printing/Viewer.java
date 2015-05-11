package com.jmrodrigg.printing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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

    ImageView mView;
    int mCurrentPage;

    Button mPrevButton, mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        String path = "example.pdf";
        Log.d("JMRODRIGG", path);

            File pathDir = Environment.getExternalStorageDirectory();
            String pdfFile = "/Download/AEC4_original.pdf";
            Log.d("JMRODRIGG", pathDir.getAbsolutePath() + pdfFile);

        try {
            File f = new File(pathDir,pdfFile);

            if(!f.exists()) this.finish();

            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f,ParcelFileDescriptor.MODE_READ_ONLY);
            ((PrintingApplication)getApplication()).renderer = new Renderer(pfd);
        }catch(IOException ex) {
            ex.printStackTrace();
            this.finish();
        }

        mCurrentPage = 0;

        //ImageView:
        mView = (ImageView) findViewById(R.id.imageView);

        // Buttons:
        mPrevButton = (Button) findViewById(R.id.prevPage);
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
        mNextButton = (Button) findViewById(R.id.nextPage);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_print) {
            Intent intent = new Intent(this,PrintingSettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
