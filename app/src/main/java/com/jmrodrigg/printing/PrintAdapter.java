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
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.util.SparseIntArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: jrodriguezg
 * Date: 12/04/2015.
 */
public class PrintAdapter extends PrintDocumentAdapter {

    private Activity mParentActivity;
    private Renderer mRenderer;
    private String mPdfFile;

    int mMediaSize[];
    int mMargins[];
    int mTotalPages;
    PrintingConstants.FitMode print_mode;
    PrintingConstants.MarginsMode margins_mode;

    private PrintedPdfDocument mDocument;

    public PrintAdapter(Activity act, PrintJob printJob) {
        mMediaSize = new int[2];
        mMargins = new int[4];

        mParentActivity = act;
        mRenderer = ((PrintingApplication)act.getApplication()).renderer;
        mPdfFile = printJob.getUri();

        print_mode = printJob.getFitMode();
        margins_mode = printJob.getMarginsMode();
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
                mMargins[PrintingConstants.LEFT_MARGIN]   = 0;
                mMargins[PrintingConstants.TOP_MARGIN]    = 0;
                mMargins[PrintingConstants.RIGHT_MARGIN]  = 0;
                mMargins[PrintingConstants.BOTTOM_MARGIN] = 0;
                break;
            default:
            case PRINTER_MARGINS:
                mMargins[PrintingConstants.LEFT_MARGIN]   = newAttributes.getMinMargins().getLeftMils();
                mMargins[PrintingConstants.TOP_MARGIN]    = newAttributes.getMinMargins().getTopMils();
                mMargins[PrintingConstants.RIGHT_MARGIN]  = newAttributes.getMinMargins().getRightMils();
                mMargins[PrintingConstants.BOTTOM_MARGIN] = newAttributes.getMinMargins().getBottomMils();
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

        int marginWidth = mMargins[PrintingConstants.LEFT_MARGIN] + mMargins[PrintingConstants.RIGHT_MARGIN];
        int marginHeight = mMargins[PrintingConstants.TOP_MARGIN] + mMargins[PrintingConstants.BOTTOM_MARGIN];

        try {
            if(print_mode == PrintingConstants.FitMode.PASS_PDF_AS_IS){
                File f = new File(mPdfFile);
                InputStream in = new FileInputStream(f);
                OutputStream out = new FileOutputStream(destination.getFileDescriptor());

                copyFile(in, out);
                in.close();
                out.flush();
                out.close();

                for (int i = 0; i < mTotalPages; i++) {
                    writtenPages.append(writtenPages.size(), i);
                }

            }else {
                for (int i = 0; i < mTotalPages; i++) {
                    if (containsPage(pageRanges, i)) {
                        // --> START Print page i;
                        PdfDocument.Page page = mDocument.startPage(i);

                        int[] dimensions = mRenderer.openPage(i);
                        Bitmap bmp = Bitmap.createBitmap(dimensions[0], dimensions[1], Bitmap.Config.ARGB_8888);
                        Matrix m = new Matrix();

                        switch (print_mode) {
                            case PRINT_CLIP_CONTENT:
                                m.setScale(1.0f, 1.0f);
                                mRenderer.renderPage(bmp, new Rect(mMargins[PrintingConstants.LEFT_MARGIN], mMargins[PrintingConstants.TOP_MARGIN], dimensions[0] - mMargins[PrintingConstants.RIGHT_MARGIN], dimensions[1] - mMargins[PrintingConstants.BOTTOM_MARGIN]), m, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                                break;
                            default:
                            case PRINT_FIT_TO_PAGE:
                                float widthScale = (float) (dimensions[0] - marginWidth) / (float) mMediaSize[0];
                                float heightScale = (float) (dimensions[1] - marginHeight) / (float) mMediaSize[1];
                                m.setScale(Math.min(widthScale, heightScale), Math.min(widthScale, heightScale));
                                break;
                        }

                        mRenderer.renderPage(bmp, new Rect(mMargins[PrintingConstants.LEFT_MARGIN], mMargins[PrintingConstants.TOP_MARGIN], dimensions[0] - mMargins[PrintingConstants.RIGHT_MARGIN], dimensions[1] - mMargins[PrintingConstants.BOTTOM_MARGIN]), m, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                        //Bitmap bmp = Bitmap.createBitmap(dimensions[0], dimensions[1], Bitmap.Config.ARGB_8888);
                        //mRenderer.renderPage(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);

                        Canvas c = page.getCanvas();
                        c.drawBitmap(bmp, 0, 0, null);

                        Paint myPaint = new Paint();
                        myPaint.setColor(Color.argb(20, 1, 0, 0));
                        myPaint.setStrokeWidth(10);
                        c.drawRect(new Rect(mMargins[PrintingConstants.LEFT_MARGIN], mMargins[PrintingConstants.TOP_MARGIN], dimensions[0] - mMargins[PrintingConstants.RIGHT_MARGIN], dimensions[1] - mMargins[PrintingConstants.BOTTOM_MARGIN]), myPaint);


                        mDocument.finishPage(page);

                        writtenPages.append(writtenPages.size(), i);
                        // --> END Print page 0.
                    }
                }
                mDocument.writeTo(new FileOutputStream(
                        destination.getFileDescriptor()));
            }
        }catch(IOException ex) {
            mDocument.close();
            mDocument = null;
        }

        PageRange[] writtenPageRange = computeWrittenPageRanges(writtenPages);
        callback.onWriteFinished(writtenPageRange);
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
