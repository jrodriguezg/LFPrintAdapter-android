package com.jmrodrigg.printing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by Juan Manuel on 09/04/2015.
 */
public class Renderer {

    PdfRenderer mRenderer;
    PdfRenderer.Page mPage;

    public Renderer(ParcelFileDescriptor pfd) {
        mPage = null;

        try {
            mRenderer = new PdfRenderer(pfd);
        } catch (IOException e) {
            mRenderer = null;
            e.printStackTrace();
        }
    }

    public int getPageCount() {
        try {
            return mRenderer.getPageCount();
        }catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public int[] openPage(int pageNum) {
        try {
            if ((pageNum < 0) || pageNum > mRenderer.getPageCount())
                throw new Exception();

            if(mPage != null) mPage.close();

            mPage = mRenderer.openPage(pageNum);
            int[] dimensions = {mPage.getWidth(),mPage.getHeight()};
            return dimensions;
        }catch (Exception ex) {
            ex.printStackTrace();
            mPage = null;
            return null;
        }
    }

    public void closePage() {
        if(mPage != null) mPage.close();
    }

    public void renderPage(Bitmap bmp, Rect rectClip, Matrix transf, int mode) {
        try {
            if(mPage == null)
                if((mode != PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY ) && (mode != PdfRenderer.Page.RENDER_MODE_FOR_PRINT))
                    throw new Exception();

            mPage.render(bmp, rectClip, transf, mode);
        } catch (Exception ex) {
            ex.printStackTrace();
            bmp = null;
        }
    }

    public void closeRenderer() {
        closePage();
        mRenderer.close();
    }
}
