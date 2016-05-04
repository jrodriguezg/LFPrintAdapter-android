package com.jmrodrigg.printing;

import com.jmrodrigg.printing.model.PrintJob;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import android.util.SparseIntArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Author: jrodriguezg
 * Date: 12/04/2015.
 */
public class PrintAdapter extends PrintDocumentAdapter {

    private static final int MILS_PER_INCH = 1000;

    private Activity mParentActivity;
    private Renderer mRenderer;
    private String mPdfFile;
    private String mPdfFileName;

    PrintAttributes currentAttributes;
    int mRenderPageWidth, mRenderPageHeight;

    PrintDocumentInfo mPrintDocumentInfo;

    int mTotalPages;
    PrintingConstants.FitMode print_mode;
    PrintingConstants.MarginsMode margins_mode;

    private PrintedPdfDocument mDocument;

    public PrintAdapter(Activity act, PrintJob printJob) {
        mParentActivity = act;
        mRenderer = ((PrintingApplication)act.getApplication()).renderer;
        mPdfFile = printJob.getUri();
        mPdfFileName = printJob.getFilename();

        print_mode = printJob.getFitMode();
        margins_mode = printJob.getMarginsMode();
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
        mDocument = new PrintedPdfDocument(mParentActivity,newAttributes);
        currentAttributes = newAttributes;

        if(cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        boolean shouldLayout = false;

        final int density = Math.max(currentAttributes.getResolution().getHorizontalDpi(),currentAttributes.getResolution().getVerticalDpi());

        final int margin_left = (int) (density * (float) currentAttributes.getMinMargins().getLeftMils() / MILS_PER_INCH);
        final int margin_right = (int) (density * (float) currentAttributes.getMinMargins().getRightMils() / MILS_PER_INCH);
        final int contentWidth = (int) (density * (float) currentAttributes.getMediaSize()
                .getWidthMils() / MILS_PER_INCH) - margin_left - margin_right;
        if (mRenderPageWidth != contentWidth) {
            mRenderPageWidth = contentWidth;
            shouldLayout = true;
        }

        final int margin_top = (int) (density * (float) currentAttributes.getMinMargins().getTopMils() / MILS_PER_INCH);
        final int margin_bottom = (int) (density * (float) currentAttributes.getMinMargins().getBottomMils() / MILS_PER_INCH);
        final int contentHeight = (int) (density * (float) currentAttributes.getMediaSize()
                .getHeightMils() / MILS_PER_INCH) - margin_top - margin_bottom;
        if (mRenderPageHeight != contentHeight) {
            mRenderPageHeight = contentHeight;
            shouldLayout = true;
        }

        if (!shouldLayout) {
            callback.onLayoutFinished(mPrintDocumentInfo,false);
            return;
        }

        int pages = computePageCount();

        if(pages > 0) {
            mPrintDocumentInfo = new PrintDocumentInfo
                                        .Builder("print_output.pdf")
                                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                        .setPageCount(pages)
                                        .build();

            callback.onLayoutFinished(mPrintDocumentInfo,true);
        } else {
            callback.onLayoutFailed("Page count calculation failed.");
        }
    }

    private int computePageCount() {
        //Just print current page:
        mTotalPages = mRenderer.getPageCount();
        return mTotalPages;
    }

    @Override
    public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {

        SparseIntArray writtenPages = new SparseIntArray();

        try {
            if (print_mode == PrintingConstants.FitMode.PASS_PDF_AS_IS) {
                printPdfAsIs(destination);

                for (int i = 0; i < mTotalPages; i++) {
                    writtenPages.append(writtenPages.size(), i);
                }
            } else {
                int margin_left = (int) (72 * (float) currentAttributes.getMinMargins().getLeftMils() / MILS_PER_INCH);
                int margin_right = (int) (72 * (float) currentAttributes.getMinMargins().getRightMils() / MILS_PER_INCH);
                int margin_top = (int) (72 * (float) currentAttributes.getMinMargins().getTopMils() / MILS_PER_INCH);
                int margin_bottom = (int) (72 * (float) currentAttributes.getMinMargins().getBottomMils() / MILS_PER_INCH);

//                margin_left = (int) ( 72 * (296/2.54f) / MILS_PER_INCH);
//                margin_right = (int) ( 72 * (296/2.54f) / MILS_PER_INCH);
//                margin_top = (int) ( 72 * (296/2.54f) / MILS_PER_INCH);
//                margin_bottom = (int) ( 72 * (296/2.54f) / MILS_PER_INCH);

                final int contentWidth = (int) (72 * (float) currentAttributes.getMediaSize()
                        .getWidthMils() / MILS_PER_INCH) - margin_left - margin_right;
                final int contentHeight = (int) (72 * (float) currentAttributes.getMediaSize()
                        .getHeightMils() / MILS_PER_INCH) - margin_top - margin_bottom;

                final float scale = Math.min((float) mDocument.getPageContentRect().width() / mRenderPageWidth,
                                             (float) mDocument.getPageContentRect().height() / mRenderPageHeight);

                for (int i = 0; i < mTotalPages; i++) {
                    if (containsPage(pageRanges, i)) {
                        // --> START Print page i;
                        PdfDocument.Page page = mDocument.startPage(i);
                        int[] dimensions = mRenderer.openPage(i);

                        switch (print_mode) {
                            case PRINT_CLIP_CONTENT:
                                Bitmap bmp = Bitmap.createBitmap(dimensions[0],dimensions[1], Bitmap.Config.ARGB_8888);

                                Matrix m = new Matrix();
                                m.setScale(1.0f,1.0f);
//                                m.setTranslate(-margin_left, -margin_top);
                                Rect rect = new Rect(0,0,dimensions[0],dimensions[1]);
                                mRenderer.renderPage(bmp, rect, m, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);

                                Canvas c = page.getCanvas();
                                c.drawBitmap(bmp, -margin_left, -margin_top, null);

//                                Paint myPaint = new Paint();
//                                myPaint.setColor(Color.argb(20,1,0,0));
//                                myPaint.setStrokeWidth(1);
//                                c.drawRect(rect, myPaint);
                                break;
                            default:
                                break;
                        }

                        mDocument.finishPage(page);

                        writtenPages.append(writtenPages.size(), i);
                        // --> END Print page 0.
                    }
                }
                mDocument.writeTo(new FileOutputStream(
                        destination.getFileDescriptor()));

                // Save a copy in the External storage for debugging purposes:
                long date = Calendar.getInstance().getTime().getTime();
                mDocument.writeTo(new FileOutputStream(Environment.getExternalStorageDirectory() + "/printing/" + date + "_" + mPdfFileName));
                Log.d(PrintingConstants.LOG_TAG,"A copy has been stored on " + Environment.getExternalStorageDirectory() + "/printing/" + date + "_" + mPdfFileName);
            }
        }catch(IOException ex) {
            mDocument.close();
            mDocument = null;
        }

        PageRange[] writtenPageRange = computeWrittenPageRanges(writtenPages);
        callback.onWriteFinished(writtenPageRange);
    }

    private void printPdfAsIs(ParcelFileDescriptor destination) throws IOException{
        File f = new File(mPdfFile);
        InputStream in = new FileInputStream(f);
        OutputStream out = new FileOutputStream(destination.getFileDescriptor());

        copyFile(in, out);
        in.close();
        out.flush();
        out.close();
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException{
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1) {
            out.write(buffer,0,read);
        }
    }

    private PageRange[] computeWrittenPageRanges(SparseIntArray writtenPages) {
        List<PageRange> pageRanges = new ArrayList<>();

        int start = -1;
        int end;
        final int writtenPageCount = writtenPages.size();
        for (int i = 0; i < writtenPageCount; i++) {
            if (start < 0) {
                start = writtenPages.valueAt(i);
            }
            int oldEnd = end = start;
            while (i < writtenPageCount && (end - oldEnd) <= 1) {
                oldEnd = end;
                end = writtenPages.valueAt(i);
                i++;
            }
            PageRange pageRange = new PageRange(start, end);
            pageRanges.add(pageRange);
            start = -1;
        }

        PageRange[] pageRangesArray = new PageRange[pageRanges.size()];
        pageRanges.toArray(pageRangesArray);
        return pageRangesArray;
    }

    private boolean containsPage(PageRange [] pageRanges,int numPage) {
        for(PageRange pr:pageRanges) {
            if((numPage >= pr.getStart()) && (numPage <= pr.getEnd())) return true;
        }
        return false;
    }
}
