package com.jmrodrigg.printing;

import com.jmrodrigg.printing.model.PrintJob;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Author: jrodriguezg
 * Date: 12/04/2015.
 */
public class Viewer extends Activity {

    private ImageView mView;
    private ContentLoadingProgressBar mProgressSpinner;
    private int mCurrentPage;
    private Button mPrevButton, mNextButton;
    private PrintJob mPrintJob;
    private Renderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        mPrintJob = getIntent().getParcelableExtra(PrintingConstants.PRINT_JOB_CLASS);

        // ImageView:
        mView = (ImageView) findViewById(R.id.imageView);
        mProgressSpinner = (ContentLoadingProgressBar) findViewById(R.id.progressSpinner);

        // Buttons:
        mPrevButton = (Button) findViewById(R.id.prevPage);
        mNextButton = (Button) findViewById(R.id.nextPage);

        // Content Type:
        switch(mPrintJob.getMimeType()) {
            case DOCUMENT:
                try {
                    renderDocument();
                } catch (IOException ex) {
                    Log.e(PrintingConstants.LOG_TAG,"IOException when rendering the Document.");
                }
                break;
            case IMAGE:
                renderImage();
                break;
        }
    }

    private void renderDocument() throws IOException {

        File f = new File(mPrintJob.getUri());
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);

        mRenderer = new Renderer(pfd);

        mCurrentPage = 0;

        // Buttons:
        mPrevButton.setVisibility(View.VISIBLE);
        mPrevButton.setEnabled(false);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNextButton.setEnabled(true);
                mRenderer.openPage(--mCurrentPage);
                if(mCurrentPage == 0) mPrevButton.setEnabled(false);
                renderPage();
            }
        });
        mPrevButton.setVisibility(View.VISIBLE);
        mNextButton.setEnabled(mRenderer.getPageCount() > 1);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPrevButton.setEnabled(true);
                mRenderer.openPage(++mCurrentPage);
                if(mCurrentPage == (mRenderer.getPageCount() -1)) mNextButton.setEnabled(false);
                renderPage();
            }
        });

        // Render first page:
        renderPage();
    }

    private void renderPage() {
        int [] dimensions = mRenderer.openPage(mCurrentPage);
        Bitmap bmp = Bitmap.createBitmap(dimensions[0],dimensions[1], Bitmap.Config.ARGB_8888);
        mRenderer.renderPage(bmp,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        mView.setMaxWidth(dimensions[0]);mView.setMinimumWidth(dimensions[0]);
        mView.setMaxHeight(dimensions[1]);mView.setMinimumHeight(dimensions[1]);
        mView.setBackgroundColor(Color.WHITE);
        mView.setImageBitmap(bmp);
    }

    private void renderImage() {
        mProgressSpinner.setVisibility(View.VISIBLE);

        BitmapWorkerTask task = new BitmapWorkerTask(mView, mProgressSpinner);
        task.execute(mPrintJob.getUri());

        mPrevButton.setVisibility(View.GONE);
        mNextButton.setVisibility(View.GONE);
    }

    private Bitmap decodeBitmap(String uri) {
        // 1.- First decode the image to gather dimensions (inJustDecodeBounds = true):
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri,opts);

        // 2.- Adjust the image:
        opts.inSampleSize = calculateInSampleSize(opts, 1024, 1024);

        // 3.- Render the whole Bitmap in the final desired size:
        opts.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(uri,opts);
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

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                     || (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

        private WeakReference<ImageView> mImgViewRef;
        private WeakReference<ContentLoadingProgressBar> mProgressSpinnerRef;

        public BitmapWorkerTask(ImageView imgView, ContentLoadingProgressBar progressSpinner) {
            mImgViewRef = new WeakReference<>(imgView);
            mProgressSpinnerRef = new WeakReference<>(progressSpinner);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String uri = strings[0];
            return decodeBitmap(uri);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if ((mImgViewRef != null) && bitmap != null) {
                final ImageView imageView = mImgViewRef.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);

                    if (mProgressSpinnerRef != null) {
                        final ContentLoadingProgressBar progressSpinner = mProgressSpinnerRef.get();
                        progressSpinner.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

}
