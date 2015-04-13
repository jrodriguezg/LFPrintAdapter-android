package com.jmrodrigg.printing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

    private Activity mParentActivity;
    private Renderer mRenderer;
    int mTotalPages;

    private PrintedPdfDocument mDocument;

    public PrintAdapter(Activity act,Renderer rend) {
        mParentActivity = act;
        mRenderer = rend;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
        mDocument = new PrintedPdfDocument(mParentActivity,newAttributes);

        if(cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
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

        for(int i=0;i<mTotalPages;i++) {
            if(containsPage(pageRanges,i)) {
                // --> START Print page i;
                PdfDocument.Page page = mDocument.startPage(i);

                int[] dimensions = mRenderer.openPage(i);
                Bitmap bmp = Bitmap.createBitmap(dimensions[0], dimensions[1], Bitmap.Config.ARGB_8888);
                mRenderer.renderPage(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);

                Canvas c = page.getCanvas();
                c.drawBitmap(bmp, 0, 0, null);

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
