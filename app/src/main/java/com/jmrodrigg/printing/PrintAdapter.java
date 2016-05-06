package com.jmrodrigg.printing;

import com.jmrodrigg.printing.model.PrintJob;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
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

    private PrintAttributes currentAttributes;
    private int mRenderPageWidth, mRenderPageHeight;

    private PrintDocumentInfo mPrintDocumentInfo;

    int mTotalPages;
    PrintingConstants.FitMode print_mode;
    PrintingConstants.MarginsMode margins_mode;

    private PrintedPdfDocument mDocument;

    public PrintAdapter(Activity act, PrintJob printJob) throws IOException {
        mParentActivity = act;

        File f = new File(printJob.getUri());
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f,ParcelFileDescriptor.MODE_READ_ONLY);

        mRenderer = new Renderer(pfd);
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

                int pageWidth = (int) (72 * (float) currentAttributes.getMediaSize().getWidthMils() / MILS_PER_INCH);
                int pageHeight = (int) (72 * (float) currentAttributes.getMediaSize().getHeightMils() / MILS_PER_INCH);

                int printable_width = pageWidth-(margin_left+margin_right);
                int printable_height = pageHeight-(margin_top+margin_bottom);

                int translateX, translateY;

                for (int i = 0; i < mTotalPages; i++) {
                    if (containsPage(pageRanges, i)) {
                        // --> START Print page i;
                        int[] dimensions = mRenderer.openPage(i);
//                        PdfDocument.Page page = mDocument.startPage(i);
                        PdfDocument.PageInfo pInfo = new PdfDocument.PageInfo.Builder(printable_width, printable_height, i).create();
                        PdfDocument.Page page = mDocument.startPage(pInfo);
                        Bitmap bmp = Bitmap.createBitmap(printable_width, printable_height, Bitmap.Config.ARGB_8888);
                        Matrix m = new Matrix();
                        float scale;

                        switch (print_mode) {
                            case PRINT_CLIP_CONTENT:
                                m.setScale(1.0f,1.0f);
                                // Center the bitmap on page while correct the margin offset:
                                translateX = Math.abs(dimensions[0]-pageWidth);
                                translateY = Math.abs(dimensions[1]-pageHeight);
                                m.setTranslate(translateX/2 -margin_left ,translateY/2 -margin_top);
                                break;

                            case PRINT_FIT_TO_PAGE:
                            case PRINT_FILL_PAGE:
                            default:
                                scale = Math.min((float) printable_width/dimensions[0], (float) printable_height/dimensions[1]);
                                if ((print_mode.equals(PrintingConstants.FitMode.PRINT_FIT_TO_PAGE) && (scale < 1))
                                    || (print_mode.equals(PrintingConstants.FitMode.PRINT_FILL_PAGE)))
                                    m.setScale(scale, scale);
                                else
                                    scale = 1;

                                translateX = Math.abs((int)(dimensions[0] * scale) - printable_width);
                                translateY = Math.abs((int)(dimensions[1] * scale) - printable_height);

                                // Rotate and translate if landscape:
                                if(dimensions[0] > dimensions[1]) {
                                    m.preRotate(90);
                                    m.postTranslate(dimensions[1] * scale,translateY/2);
                                    if(pageWidth > pageHeight)
                                        m.postTranslate(translateX/2,0);
                                } else {
                                    // Translate to center the content:
                                    m.postTranslate(translateX/2, translateY/2);
                                }


                                break;
                        }

                        mRenderer.renderPage(bmp, null, m, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);

                        Canvas c = page.getCanvas();
                        c.drawBitmap(bmp, 0, 0, null);
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
