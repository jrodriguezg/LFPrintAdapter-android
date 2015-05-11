package com.jmrodrigg.printing;

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
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.util.SparseIntArray;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: jrodriguezg
 * Date: 12/04/2015.
 */
public class PrintAdapter extends PrintDocumentAdapter {

    private static final int LEFT_MARGIN    = 0;
    private static final int TOP_MARGIN     = 1;
    private static final int RIGHT_MARGIN   = 2;
    private static final int BOTTOM_MARGIN  = 3;

    private Activity mParentActivity;
    private Renderer mRenderer;

    int mMediaSize[];
    int mMargins[];
    int mTotalPages;
    PrintingApplication.PrintMode print_mode;
    PrintingApplication.MarginsMode margins_mode;

    private PrintedPdfDocument mDocument;

    public PrintAdapter(Activity act,Renderer rend) {
        mMediaSize = new int[2];
        mMargins = new int[4];

        mParentActivity = act;
        mRenderer = rend;
        print_mode = ((PrintingApplication)act.getApplication()).print_mode;
        margins_mode = ((PrintingApplication)act.getApplication()).margins_mode;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
        mDocument = new PrintedPdfDocument(mParentActivity,newAttributes);

        if(cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        // store the media size:
        mMediaSize[0] = newAttributes.getMediaSize().getWidthMils();
        mMediaSize[1] = newAttributes.getMediaSize().getHeightMils();

        // store the margins:
        switch(margins_mode){
            case NO_MARGINS:
                mMargins[LEFT_MARGIN]   = 0;
                mMargins[TOP_MARGIN]    = 0;
                mMargins[RIGHT_MARGIN]  = 0;
                mMargins[BOTTOM_MARGIN] = 0;
                break;
            default:
            case PRINTER_MARGINS:
                mMargins[LEFT_MARGIN]   = newAttributes.getMinMargins().getLeftMils();
                mMargins[TOP_MARGIN]    = newAttributes.getMinMargins().getTopMils();
                mMargins[RIGHT_MARGIN]  = newAttributes.getMinMargins().getRightMils();
                mMargins[BOTTOM_MARGIN] = newAttributes.getMinMargins().getBottomMils();
                break;
        }

        int pages = computePageCount();

        if(pages > 0) {
            PrintDocumentInfo info = new PrintDocumentInfo
                                        .Builder("print_output.pdf")
                                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                        .setPageCount(pages)
                                        .build();

            callback.onLayoutFinished(info,true);
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

        int marginWidth = mMargins[LEFT_MARGIN] + mMargins[RIGHT_MARGIN];
        int marginHeight = mMargins[TOP_MARGIN] + mMargins[BOTTOM_MARGIN];

        for(int i=0;i<mTotalPages;i++) {
            if(containsPage(pageRanges,i)) {
                // --> START Print page i;
                PdfDocument.Page page = mDocument.startPage(i);

                int[] dimensions = mRenderer.openPage(i);
                Bitmap bmp = Bitmap.createBitmap(dimensions[0], dimensions[1], Bitmap.Config.ARGB_8888);
                Matrix m = new Matrix();

                switch (print_mode) {
                    case PRINT_CLIP_CONTENT:
                        m.setScale(1.0f,1.0f);
                        mRenderer.renderPage(bmp, new Rect(mMargins[LEFT_MARGIN],mMargins[TOP_MARGIN],dimensions[0]-mMargins[RIGHT_MARGIN],dimensions[1]-mMargins[BOTTOM_MARGIN]), m, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                        break;
                    default:
                    case PRINT_FIT_TO_PAGE:
                        float widthScale = (float)(dimensions[0]-marginWidth)/(float)mMediaSize[0];
                        float heightScale = (float)(dimensions[1]-marginHeight)/(float)mMediaSize[1];
                        m.setScale(Math.min(widthScale,heightScale),Math.min(widthScale,heightScale));
                        break;

                }

                mRenderer.renderPage(bmp, new Rect(mMargins[LEFT_MARGIN],mMargins[TOP_MARGIN],dimensions[0]-mMargins[RIGHT_MARGIN],dimensions[1]-mMargins[BOTTOM_MARGIN]), m, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                //Bitmap bmp = Bitmap.createBitmap(dimensions[0], dimensions[1], Bitmap.Config.ARGB_8888);
                //mRenderer.renderPage(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);

                Canvas c = page.getCanvas();
                c.drawBitmap(bmp, 0, 0, null);

                Paint myPaint = new Paint();
                myPaint.setColor(Color.argb(20,1,0,0));
                myPaint.setStrokeWidth(10);
                c.drawRect(new Rect(mMargins[LEFT_MARGIN],mMargins[TOP_MARGIN],dimensions[0]-mMargins[RIGHT_MARGIN],dimensions[1]-mMargins[BOTTOM_MARGIN]),myPaint);


                mDocument.finishPage(page);

                writtenPages.append(writtenPages.size(), i);
                // --> END Print page 0.
            }
        }

        try {
            mDocument.writeTo(new FileOutputStream(
                    destination.getFileDescriptor()));
        }catch(IOException ex) {
            mDocument.close();
            mDocument = null;
        }

        PageRange[] writtenPageRange = computeWrittenPageRanges(writtenPages);
        callback.onWriteFinished(writtenPageRange);
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
