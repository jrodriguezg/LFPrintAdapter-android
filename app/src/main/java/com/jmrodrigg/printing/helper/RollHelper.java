package com.jmrodrigg.printing.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import android.graphics.pdf.PdfDocument.Page;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Author: jsanchez
 * Date: 18/11/2015.
 */
public class RollHelper implements RollHelperConstants {
    private static final String LOG_TAG = "RollHelper";
    // will be <= 300 dpi on A4 (8.3×11.7) paper (worst case of 150 dpi)
    private final static int MAX_PRINT_SIZE = 3500;
    private final Context mContext;
    private BitmapFactory.Options mDecodeOptions = null;
    private final Object mLock = new Object();
    private int mOriginalBitmapWidth;
    private int mOriginalBitmapLenght;

    /**
     * image will be scaled but leave white space
     */
    public static final int SCALE_MODE_FIT = 1;
    /**
     * image will fill the paper and be cropped (default)
     */
    public static final int SCALE_MODE_FILL = 2;

    /**
     * select landscape (default)
     */
    public static final int ORIENTATION_LANDSCAPE = 1;

    /**
     * select portrait
     */
    public static final int ORIENTATION_PORTRAIT = 2;

    /**
     * this is a black and white image
     */
    public static final int COLOR_MODE_MONOCHROME = 1;
    /**
     * this is a color image (default)
     */
    public static final int COLOR_MODE_COLOR = 2;

    public interface OnPrintFinishCallback {
        void onFinish();
    }

    int mScaleMode = SCALE_MODE_FILL;

    int mColorMode = COLOR_MODE_COLOR;

    int mOrientation = ORIENTATION_LANDSCAPE;

    boolean mIsPreview;

    public RollHelper(Context context) {
        mContext = context;
    }

    /**
     * Selects whether the image will fill the paper and be cropped
     * <p/>
     * {@link #SCALE_MODE_FIT}
     * or whether the image will be scaled but leave white space
     * {@link #SCALE_MODE_FILL}.
     *
     * @param scaleMode {@link #SCALE_MODE_FIT} or
     *                  {@link #SCALE_MODE_FILL}
     */
    public void setScaleMode(int scaleMode) {
        mScaleMode = scaleMode;
    }

    /**
     * Sets whether the image will be printed in color (default)
     * {@link #COLOR_MODE_COLOR} or in back and white
     * {@link #COLOR_MODE_MONOCHROME}.
     *
     * @param colorMode The color mode which is one of
     *                  {@link #COLOR_MODE_COLOR} and {@link #COLOR_MODE_MONOCHROME}.
     */
    @SuppressWarnings("unused")
    public void setColorMode(int colorMode) {
        mColorMode = colorMode;
    }

    /**
     * Sets whether to select landscape (default), {@link #ORIENTATION_LANDSCAPE}
     * or portrait {@link #ORIENTATION_PORTRAIT}
     * @param orientation The page orientation which is one of
     *                    {@link #ORIENTATION_LANDSCAPE} or {@link #ORIENTATION_PORTRAIT}.
     */
    @SuppressWarnings("unused")
    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    /**
     * Gets the page orientation with which the image will be printed.
     *
     * @return The preferred orientation which is one of
     * {@link #ORIENTATION_LANDSCAPE} or {@link #ORIENTATION_PORTRAIT}
     */
    @SuppressWarnings("unused")
    public int getOrientation() {
        return mOrientation;
    }

    /**
     * Gets the color mode with which the image will be printed.
     *
     * @return The color mode which is one of {@link #COLOR_MODE_COLOR}
     * and {@link #COLOR_MODE_MONOCHROME}.
     */
    @SuppressWarnings("unused")
    public int getColorMode() {
        return mColorMode;
    }

    /**
     * Calculates the transform {@link Matrix} to print an image. It will always fit to roll width
     * according to the selected orientation.
     *
     * @param imageWidth width of the image that will be printed.
     * @param content a {@link Rect} that defines the output page dimensions.
     * @return {@link Matrix} to be used to transform the @{Bitmap} that will be drawn.
     */
    private Matrix getRollMatrix(int imageWidth, RectF content) {
        Matrix matrix = new Matrix();
        float scale;
        if (content.width() > content.height()) {
            //portrait - fit to content width:
            scale = (content.width() - content.left) / imageWidth;

            matrix.postScale(scale, scale);
        } else{
            //landscape - fit to content height:
            scale = (content.height() - content.top) / imageWidth;
            matrix.postRotate(90);
            matrix.postScale(scale, scale);
            matrix.postTranslate(content.width(),0f);
        }

        return matrix;
    }

    /**
     * Calculates the transform {@link Matrix} to print an image. It will always fit to roll width
     * according to the selected orientation.
     *
     * @param imageWidth width of the image that will be printed.
     * @param content a {@link Rect} that defines the output page dimensions.
     * @param fittingMode The mode of fitting {@link #SCALE_MODE_FILL} vs {@link #SCALE_MODE_FIT}
     * @return {@link Matrix} to be used to transform the @{Bitmap} that will be drawn.
     */
    private Matrix getMatrix(int imageWidth, int imageHeight, RectF content, int fittingMode) {
        Matrix matrix = new Matrix();

        // Compute and apply scale to fill the page.
        float scale = content.width() / imageWidth;
        if (fittingMode == SCALE_MODE_FILL) {
            scale = Math.max(scale, content.height() / imageHeight);
        } else {
            scale = Math.min(scale, content.height() / imageHeight);
        }

        matrix.postScale(scale, scale);

        // Center the content.
        final float translateX = (content.width()
                - imageWidth * scale) / 2;
        final float translateY = (content.height()
                - imageHeight * scale) / 2;

        matrix.postTranslate(translateX,translateY);
        return matrix;
    }

    /**
     * Checks if a given MediaSize is encapsulating a roll media.
     * With the current framework, it is only possible by checking the MediaSize ID: HP Print
     * Service Plugin ensures that a roll media size id will contain the string "roll_current".
     *
     * @param size The media size to check.
     * @return true if the MediaSize is mapping a roll, false otherwise.
     */
    protected boolean isRoll(PrintAttributes.MediaSize size){
        return size.getId().contains("roll_current");
    }

    /**
     * Prints an image located at the Uri. Image types supported are those of
     * <code>BitmapFactory.decodeStream</code> (JPEG, GIF, PNG, BMP, WEBP)
     *
     * @param jobName   The print job name.
     * @param imageFile The <code>Uri</code> pointing to an image to print.
     * @param callback Optional callback to observe when printing is finished.
     * @throws FileNotFoundException if <code>Uri</code> is not pointing to a valid image.
     */
    public void printBitmap(final String jobName, final Uri imageFile,
                            final OnPrintFinishCallback callback) throws FileNotFoundException {
        final int fittingMode = mScaleMode;

        PrintDocumentAdapter printDocumentAdapter = new PrintDocumentAdapter() {
            private PrintAttributes mAttributes;
            AsyncTask<Uri, Boolean, Bitmap> mLoadBitmap;
            Bitmap mBitmap = null;

            @Override
            public void onLayout(final PrintAttributes oldPrintAttributes,
                                 final PrintAttributes newPrintAttributes,
                                 final CancellationSignal cancellationSignal,
                                 final LayoutResultCallback layoutResultCallback,
                                 Bundle bundle) {

                Log.d(LOG_TAG, "onLayout() - Init.");

                mIsPreview = bundle.getBoolean(PrintDocumentAdapter.EXTRA_PRINT_PREVIEW);
                if (mIsPreview) Log.d(LOG_TAG,"Prepare for preview.");
                else Log.d(LOG_TAG,"Prepare for print.");
                mAttributes = newPrintAttributes;

                if (cancellationSignal.isCanceled()) {
                    Log.i(LOG_TAG, "onLayout() - Cancelled.");
                    layoutResultCallback.onLayoutCancelled();
                    return;
                }
                // we finished the load
                if (mBitmap != null) {
                    PrintDocumentInfo info = new PrintDocumentInfo.Builder(jobName)
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                            .build();
                    boolean changed = !newPrintAttributes.equals(oldPrintAttributes);
                    Log.d(LOG_TAG, "onLayout() - Finished.");
                    layoutResultCallback.onLayoutFinished(info, changed || !mIsPreview);
                    return;
                }

                mLoadBitmap = new AsyncTask<Uri, Boolean, Bitmap>() {

                    @Override
                    protected void onPreExecute() {
                        // First register for cancellation requests.
                        cancellationSignal.setOnCancelListener(
                                new CancellationSignal.OnCancelListener() {
                                    @Override
                                    public void onCancel() { // on different thread
                                        cancelLoad();
                                        cancel(false);
                                    }
                                });
                    }

                    @Override
                    protected Bitmap doInBackground(Uri... uris) {
                        try {
                            return loadConstrainedBitmap(imageFile, MAX_PRINT_SIZE);
                        } catch (FileNotFoundException e) {
                          /* ignore */
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        super.onPostExecute(bitmap);
                        mBitmap = bitmap;
                        if (bitmap != null) {
                            PrintDocumentInfo info = new PrintDocumentInfo.Builder(jobName)
                                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                    .setPageCount(1)
                                    .build();
                            boolean changed = !newPrintAttributes.equals(oldPrintAttributes);

                            Log.d(LOG_TAG, "onLayout() - Finished.");
                            layoutResultCallback.onLayoutFinished(info, changed);

                        } else {
                            Log.e(LOG_TAG, "onLayout() - Finished with errors.");
                            layoutResultCallback.onLayoutFailed(null);
                        }
                        mLoadBitmap = null;
                    }

                    @Override
                    protected void onCancelled(Bitmap result) {
                        // Task was cancelled, report that.
                        Log.i(LOG_TAG, "onLayout() - Cancelled.");
                        layoutResultCallback.onLayoutCancelled();
                        mLoadBitmap = null;
                    }
                }.execute();
            }

            private void cancelLoad() {
                synchronized (mLock) { // prevent race with set null below
                    if (mDecodeOptions != null) {
                        mDecodeOptions.requestCancelDecode();
                        mDecodeOptions = null;
                    }
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                cancelLoad();
                if (mLoadBitmap != null) {
                    mLoadBitmap.cancel(true);
                }
                if (callback != null) {
                    callback.onFinish();
                }
                if (mBitmap != null) {
                    mBitmap.recycle();
                    mBitmap = null;
                }
            }

            @Override
            public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor fileDescriptor,
                                CancellationSignal cancellationSignal,
                                WriteResultCallback writeResultCallback) {
                PrintedPdfDocument pdfDocument;

                Log.d(LOG_TAG, "onWrite() - Init.");

                if (isRoll(mAttributes.getMediaSize())) {
                    float aspectRatio;

                    if(mAttributes.getMediaSize().isPortrait()) aspectRatio = (float) mBitmap.getWidth() / (float) mBitmap.getHeight();
                    else aspectRatio = (float) mBitmap.getHeight() / (float) mBitmap.getWidth();

                    Log.d(LOG_TAG, "Printing file to roll.");
                    Log.d(LOG_TAG, "Size: " + mBitmap.getWidth() + " x " + mBitmap.getHeight() + ". Aspect ratio: " + aspectRatio);

                    PrintAttributes.MediaSize oldMediaSize = mAttributes.getMediaSize();
                    Log.d(LOG_TAG, "PageSize: " + oldMediaSize.getWidthMils() + " x " + oldMediaSize.getHeightMils() + ". Aspect ratio:" + aspectRatio);
                    int width = Math.min(oldMediaSize.getWidthMils(), 36000);
                    //int width = oldMediaSize.getWidthMils();

                    int pageHeight;
                    if(oldMediaSize.isPortrait()){
                        Log.d(LOG_TAG, "Portrait mode.");
                        pageHeight = Math.round((float)width/aspectRatio);
                    }
                    else{
                        Log.d(LOG_TAG, "Landscape mode.");
                        pageHeight = Math.round((float)width/aspectRatio);
                    }
                    PrintAttributes.MediaSize newMediaSize = new PrintAttributes.MediaSize(oldMediaSize.getId(),
                            "Test paper",
                            width,
                            pageHeight);
                    mAttributes = new PrintAttributes.Builder()
                            .setMediaSize(newMediaSize)
                            .setResolution(mAttributes.getResolution())
                            .setMinMargins(mAttributes.getMinMargins())
                            .build();
                    Log.d(LOG_TAG, "PDF created: Size " + width + " x " + pageHeight);

                    pdfDocument = new PrintedPdfDocument(mContext, mAttributes);
                } else {
                    Log.d(LOG_TAG, "Printing file on standard media size.");
                    Log.d(LOG_TAG, "PDF created - Size: " + mAttributes.getMediaSize().getWidthMils() + " x " + mAttributes.getMediaSize().getHeightMils());
                    pdfDocument = new PrintedPdfDocument(mContext, mAttributes);
                }
                Bitmap maybeGrayscale = convertBitmapForColorMode(mBitmap, mAttributes.getColorMode());

                try {
                    Page page = pdfDocument.startPage(1);

                    RectF content = new RectF(page.getInfo().getContentRect());
                    Log.d(LOG_TAG,"Content Rect: " + content.toString() + ". Width: " + content.width() + ". Height: " + content.height());
                    if (!mIsPreview && mOriginalBitmapLenght > mBitmap.getHeight() && mOriginalBitmapWidth > mBitmap.getWidth()) {
                        //generate a pdf with tiles
                        GeneratePDF(imageFile, page, isRoll(mAttributes.getMediaSize()), fittingMode);
                    } else {
                        // Compute and apply scale to fill the page.
                        Matrix matrix = (isRoll(mAttributes.getMediaSize())) ? getRollMatrix(mBitmap.getWidth(), content) : getMatrix(mBitmap.getWidth(), mBitmap.getHeight(), content, fittingMode);
                        // Draw the bitmap.
                        page.getCanvas().drawBitmap(maybeGrayscale, matrix, null);
                    }
                    // Finish the page.
                    pdfDocument.finishPage(page);

                    try {
                        Log.d(LOG_TAG, "Writing print Job to file descriptor.");

                        pdfDocument.writeTo(new FileOutputStream(fileDescriptor.getFileDescriptor()));

                        if (DUMP_FILE) {
                            // Save a copy in the External storage for debugging purposes:
                            long date = Calendar.getInstance().getTime().getTime();
                            pdfDocument.writeTo(new FileOutputStream(Environment.getExternalStorageDirectory() + "/printing/" + date + "_" + jobName + ".pdf"));
                            Log.d(LOG_TAG, "A copy has been stored on " + Environment.getExternalStorageDirectory() + "/printing/" + date + "_" + jobName + ".pdf");
                        }

                        writeResultCallback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                    } catch (IOException ioe) {
                        // Failed.
                        Log.e(LOG_TAG, "onWrite() - Finished with errors: Error writing printed content.", ioe);
                        writeResultCallback.onWriteFailed(null);
                    }
                } finally {
                    pdfDocument.close();

                    if (fileDescriptor != null) {
                        try {
                            fileDescriptor.close();
                        } catch (IOException ioe) {
                            /* ignore */
                        }
                    }
                    // If we created a new instance for grayscaling, then recycle it here.
                    if (maybeGrayscale != mBitmap) {
                        maybeGrayscale.recycle();
                    }

                    Log.d(LOG_TAG, "onWrite() - Finished.");
                }
            }
        };

        PrintManager printManager = (PrintManager) mContext.getSystemService(Context.PRINT_SERVICE);
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setColorMode(mColorMode);

        if (mOrientation == ORIENTATION_LANDSCAPE) {
            builder.setMediaSize(PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE);
        } else if (mOrientation == ORIENTATION_PORTRAIT) {
            builder.setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT);
        }
        PrintAttributes attr = builder.build();

        printManager.print(jobName, printDocumentAdapter, attr);
    }

    /**
     * Draws a file in a {@link Page} following a tile-based strategy, which allows to render large
     * images without memory exceptions.
     *
     * @param uri Location of a valid image.
     * @param page The Page where the given file will be drawn.
     */
    private void GeneratePDF(Uri uri, Page page, boolean isRoll, int fittingMode){

        Log.d(LOG_TAG,"GeneratePDF - Generating PDF file using tiling strategy.");

        InputStream is;
        try {
            is = mContext.getContentResolver().openInputStream(uri);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.
                    newInstance(is, false);
            Rect tileBounds = new Rect();
            RectF contentRect = new RectF(page.getInfo().getContentRect());
            //set matrix for rotation and/or scale
            Matrix m = (isRoll) ? getRollMatrix(mOriginalBitmapWidth, contentRect) : getMatrix(mOriginalBitmapWidth, mOriginalBitmapLenght, contentRect, fittingMode);
            page.getCanvas().setMatrix(m);

            int height = mOriginalBitmapLenght;
            int width = mOriginalBitmapWidth;
            BitmapFactory.Options options = setBitmapOptions();
            Log.d(LOG_TAG,"Page: " + width + "x" + height + ". Density: " + page.getCanvas().getDensity());
            Bitmap tile;

            // loop block:
            for (int i=0; i<height; i+=TILE_SIZE) {
                // get vertical bounds limited by image height
                tileBounds.top = i;
                int h = i+TILE_SIZE<height ? TILE_SIZE : height-i;
                tileBounds.bottom = i+h;
                for (int j=0; j<width; j+=TILE_SIZE) {
                    // get hotizontal bounds limited by image width
                    tileBounds.left = j;
                    int w = j+TILE_SIZE<width ? TILE_SIZE : width-j;
                    tileBounds.right = j + w;
                    // load tile
                    tile = decoder.decodeRegion(tileBounds, options);
                    page.getCanvas().drawBitmap(tile,
                                                new Rect(0, 0, TILE_SIZE/options.inSampleSize, TILE_SIZE/options.inSampleSize),
                                                new Rect(tileBounds.left, tileBounds.top, tileBounds.right, tileBounds.bottom),
                                                null);
                    
//                    Log.d(LOG_TAG,"Drawing tile on " + i + "x" + j + ". Bitmap: " + tile.getWidth() + "x" + tile.getHeight());
                    tile.recycle();
                }
            }

        Log.d(LOG_TAG,"GeneratePDF - Generating PDF file using tiling strategy.");

        }
        catch (Exception ex){
            Log.e(LOG_TAG,"GeneratePDF - An exception occured while generating tiles.");
            ex.printStackTrace();
        }
    }

    /**
     * Creates a {@link android.graphics.BitmapFactory.Options} object with the rendering parameters.
     * This rendering parameters can be set by modifying the constants in {@link RollHelperConstants}.
     * Please note that the modification of this parameters will impact over the rendering time of the
     * print job.
     *
     * @return The {@link android.graphics.BitmapFactory.Options} that have been set.
     */
    private BitmapFactory.Options setBitmapOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = COLOR_CONFIG;
        options.inDither = IN_DITHER;
        options.inPreferQualityOverSpeed = QUALITY_OVER_SPEED;
        options.inSampleSize = SUBSAMPLING_VALUE;
        return options;
    }

    /**
     * Loads a bitmap while limiting its size.
     *
     * @param uri Location of a valid image
     * @param maxSideLength The maximum length of a size
     * @return the Bitmap
     * @throws FileNotFoundException if the Uri does not resolves to an image.
     */
    private Bitmap loadConstrainedBitmap(Uri uri, int maxSideLength) throws FileNotFoundException {
        if (maxSideLength <= 0 || uri == null || mContext == null) {
            throw new IllegalArgumentException("bad argument to getScaledBitmap");
        }
        // Get width and height of stored bitmap
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        loadBitmap(uri, opt);

        int w = opt.outWidth;
        int h = opt.outHeight;
        mOriginalBitmapWidth = opt.outWidth;
        mOriginalBitmapLenght = opt.outHeight;
        // If bitmap cannot be decoded, return null
        if (w <= 0 || h <= 0) {
            return null;
        }

        // Find best downsampling size
        int imageSide = Math.max(w, h);

        int sampleSize = 1;
        while (imageSide > maxSideLength) {
            imageSide >>>= 1;
            sampleSize <<= 1;
        }

        // Make sure sample size is reasonable
        if (sampleSize <= 0 || 0 >= (Math.min(w, h) / sampleSize)) {
            return null;
        }
        BitmapFactory.Options decodeOptions;
        synchronized (mLock) { // prevent race with set null below
            mDecodeOptions = new BitmapFactory.Options();
            mDecodeOptions.inMutable = true;
            mDecodeOptions.inSampleSize = sampleSize;
            decodeOptions = mDecodeOptions;
        }
        try {
            return loadBitmap(uri, decodeOptions);
        } finally {
            synchronized (mLock) {
                mDecodeOptions = null;
            }
        }
    }

    /**
     *
     * @param uri Location of a valid image.
     * @param options The rendering options.
     * @return The bitmap from the given uri loaded using the given options. Otherwise, it returns null.
     * @throws FileNotFoundException if the Uri does not resolves to an image.
     */
    private Bitmap loadBitmap(Uri uri, BitmapFactory.Options options) throws FileNotFoundException {
        if (uri == null || mContext == null) {
            throw new IllegalArgumentException("bad argument to loadBitmap");
        }
        InputStream is = null;
        try {
            is = mContext.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(is, null, options);
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException t) {
                    Log.e(LOG_TAG, "Failed to close InputStream.", t);
                }
            }
        }
        return null;
    }


    /**
     * Converts a {@link Bitmap} by applying a given <code>colorMode</code>.
     *
     * @param original The original {@link Bitmap} that will be converted.
     * @param colorMode The colorMode to apply on the {@param original} {@link Bitmap}.
     * @return The result of applying the given <code>colorMode</code> on the original {@link Bitmap}.
     */
    private Bitmap convertBitmapForColorMode(Bitmap original, int colorMode) {
        if (colorMode != COLOR_MODE_MONOCHROME) {
            return original;
        }
        // Create a grayscale bitmap
        Bitmap grayscale = Bitmap.createBitmap(original.getWidth(), original.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(grayscale);
        Paint p = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        p.setColorFilter(f);
        c.drawBitmap(original, 0, 0, p);
        c.setBitmap(null);

        return grayscale;
    }
}
