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
 * Created by Juan Manuel on 12/04/2015.
 */
public class PrintAdapter extends PrintDocumentAdapter {

    private Activity mParentActivity;
    private Renderer mRenderer;

    private PrintedPdfDocument mDocument;
    private final SparseIntArray mWrittenPages;

    public PrintAdapter(Activity act,Renderer rend) {
        mParentActivity = act;
        mRenderer = rend;

        mWrittenPages = new SparseIntArray();
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
        mDocument = new PrintedPdfDocument(mParentActivity,newAttributes);

        if(cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        int pages = computePageCount(newAttributes);

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

    private int computePageCount(PrintAttributes printAttributes) {
        //Just print current page:
        return 1;
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
        // --> START Print page 0;
        PdfDocument.Page page = mDocument.startPage(0);

        int [] dimensions = mRenderer.openPage(0);
        Bitmap bmp = Bitmap.createBitmap(dimensions[0],dimensions[1], Bitmap.Config.ARGB_8888);
        mRenderer.renderPage(bmp,null,null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);

        Canvas c = page.getCanvas();
        c.drawBitmap(bmp,0,0,null);

        mDocument.finishPage(page);

        mWrittenPages.append(mWrittenPages.size(),0);
        // --> END Print page 0.

        try {
            mDocument.writeTo(new FileOutputStream(
                    destination.getFileDescriptor()));;
        }catch(IOException ex) {
            mDocument.close();
            mDocument = null;
        }

        PageRange[] pageRanges = computeWrittenPageRanges(mWrittenPages);
        callback.onWriteFinished(pageRanges);
    }

    private PageRange[] computeWrittenPageRanges(SparseIntArray writtenPages) {
        List<PageRange> pageRanges = new ArrayList<PageRange>();

        int start = -1;
        int end = -1;
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
            start = end = -1;
        }

        PageRange[] pageRangesArray = new PageRange[pageRanges.size()];
        pageRanges.toArray(pageRangesArray);
        return pageRangesArray;
    }
}
