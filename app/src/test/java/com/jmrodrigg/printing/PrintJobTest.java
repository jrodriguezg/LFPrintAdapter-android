package com.jmrodrigg.printing;

import com.jmrodrigg.printing.model.PrintJob;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Author: jrodriguezg
 * Date: 25/05/16.
 */
public class PrintJobTest {
    PrintJob mPrintJob;

    @Before
    public void setUp() {
        mPrintJob = Mockito.mock(PrintJob.class);
    }


    @Test
    public void testPrintJob_Document_Valid() {
        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .pdf
        *  FitMode: PDF_AS_IS
        *  JobType: DOCUMENT
        *  Margins: PRINTER_MARGINS.
        * */
        when(mPrintJob.verifyFileIntegrity()).thenReturn(true);
        when(mPrintJob.isValid()).thenCallRealMethod();
        when(mPrintJob.getMimeType()).thenReturn(PrintingConstants.JobType.DOCUMENT);
        when(mPrintJob.getFilename()).thenReturn(".pdf");
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PASS_PDF_AS_IS);
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.PRINTER_MARGINS);
        assertTrue(mPrintJob.isValid());

        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .pdf
        *  FitMode: CLIP_CONTENT
        *  JobType: DOCUMENT
        *  Margins: PRINTER_MARGINS.
        * */
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PRINT_CLIP_CONTENT);
        assertTrue(mPrintJob.isValid());

        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .pdf
        *  FitMode: PRINT_FILL_PAGE
        *  JobType: DOCUMENT
        *  Margins: PRINTER_MARGINS.
        * */
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PRINT_FILL_PAGE);
        assertTrue(mPrintJob.isValid());

        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .pdf
        *  FitMode: PRINT_FILL_PAGE
        *  JobType: DOCUMENT
        *  Margins: PRINTER_MARGINS.
        * */
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PRINT_FIT_TO_PAGE);
        assertTrue(mPrintJob.isValid());
    }

    @Test
    public void testPrintJob_Image_JPG_Valid() {
        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .jpg
        *  FitMode: PRINT_FIT_TO_PAGE
        *  JobType: IMAGE
        *  Margins: PRINTER_MARGINS.
        * */
        when(mPrintJob.verifyFileIntegrity()).thenReturn(true);
        when(mPrintJob.isValid()).thenCallRealMethod();
        when(mPrintJob.getMimeType()).thenReturn(PrintingConstants.JobType.IMAGE);
        when(mPrintJob.getFilename()).thenReturn(".jpg");
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PRINT_FIT_TO_PAGE);
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.PRINTER_MARGINS);
        assertTrue(mPrintJob.isValid());

        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .jpg
        *  FitMode: PRINT_FIT_TO_PAGE
        *  JobType: IMAGE
        *  Margins: NO_MARGINS.
        * */
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.NO_MARGINS);
        assertTrue(mPrintJob.isValid());

        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .jpg
        *  FitMode: PRINT_FIT_TO_PAGE
        *  JobType: IMAGE
        *  Margins: NO_MARGINS.
        * */
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PRINT_FILL_PAGE);
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.PRINTER_MARGINS);
        assertTrue(mPrintJob.isValid());

        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .jpg
        *  FitMode: PRINT_FIT_TO_PAGE
        *  JobType: IMAGE
        *  Margins: NO_MARGINS.
        * */
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.NO_MARGINS);
        assertTrue(mPrintJob.isValid());
    }

    @Test
    public void testPrintJob_Image_JPEG_Valid() {
        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .jpeg
        *  FitMode: PRINT_FIT_TO_PAGE
        *  JobType: IMAGE
        *  Margins: PRINTER_MARGINS.
        * */
        when(mPrintJob.verifyFileIntegrity()).thenReturn(true);
        when(mPrintJob.isValid()).thenCallRealMethod();
        when(mPrintJob.getMimeType()).thenReturn(PrintingConstants.JobType.IMAGE);
        when(mPrintJob.getFilename()).thenReturn(".jpeg");
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PRINT_FIT_TO_PAGE);
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.PRINTER_MARGINS);
        assertTrue(mPrintJob.isValid());

        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .jpeg
        *  FitMode: PRINT_FIT_TO_PAGE
        *  JobType: IMAGE
        *  Margins: NO_MARGINS.
        * */
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.NO_MARGINS);
        assertTrue(mPrintJob.isValid());

        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .jpeg
        *  FitMode: PRINT_FIT_TO_PAGE
        *  JobType: IMAGE
        *  Margins: NO_MARGINS.
        * */
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PRINT_FILL_PAGE);
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.PRINTER_MARGINS);
        assertTrue(mPrintJob.isValid());

        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .jpeg
        *  FitMode: PRINT_FIT_TO_PAGE
        *  JobType: IMAGE
        *  Margins: NO_MARGINS.
        * */
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.NO_MARGINS);
        assertTrue(mPrintJob.isValid());
    }

    @Test
    public void testPrintJob_Image_PNG_Valid() {
        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .jpeg
        *  FitMode: PRINT_FIT_TO_PAGE
        *  JobType: IMAGE
        *  Margins: PRINTER_MARGINS.
        * */
        when(mPrintJob.verifyFileIntegrity()).thenReturn(true);
        when(mPrintJob.isValid()).thenCallRealMethod();
        when(mPrintJob.getMimeType()).thenReturn(PrintingConstants.JobType.IMAGE);
        when(mPrintJob.getFilename()).thenReturn(".png");
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PRINT_FIT_TO_PAGE);
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.PRINTER_MARGINS);
        assertTrue(mPrintJob.isValid());

        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .png
        *  FitMode: PRINT_FIT_TO_PAGE
        *  JobType: IMAGE
        *  Margins: NO_MARGINS.
        * */
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.NO_MARGINS);
        assertTrue(mPrintJob.isValid());

        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .png
        *  FitMode: PRINT_FIT_TO_PAGE
        *  JobType: IMAGE
        *  Margins: NO_MARGINS.
        * */
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PRINT_FILL_PAGE);
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.PRINTER_MARGINS);
        assertTrue(mPrintJob.isValid());

        /*
        *  URI: Mocking VerifyFileIntegrity() to return TRUE.
        *  Filename: .png
        *  FitMode: PRINT_FIT_TO_PAGE
        *  JobType: IMAGE
        *  Margins: NO_MARGINS.
        * */
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.NO_MARGINS);
        assertTrue(mPrintJob.isValid());
    }


    @Test
    public void testPrintJob_Image_WrongType() {
        when(mPrintJob.verifyFileIntegrity()).thenReturn(true);
        when(mPrintJob.getFilename()).thenReturn(".jpg");
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PRINT_FILL_PAGE);
        when(mPrintJob.getMimeType()).thenReturn(PrintingConstants.JobType.DOCUMENT);
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.PRINTER_MARGINS);
        when(mPrintJob.isValid()).thenCallRealMethod();
        assertFalse(mPrintJob.isValid());

        when(mPrintJob.getFilename()).thenReturn(".jpeg");
        assertFalse(mPrintJob.isValid());

        when(mPrintJob.getFilename()).thenReturn(".png");
        assertFalse(mPrintJob.isValid());
    }

    @Test
    public void testPrintJob_Image_WrongFitMode() {
        when(mPrintJob.verifyFileIntegrity()).thenReturn(true);
        when(mPrintJob.isValid()).thenCallRealMethod();
        when(mPrintJob.getFilename()).thenReturn(".jpg");
        when(mPrintJob.getMimeType()).thenReturn(PrintingConstants.JobType.IMAGE);
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.PRINTER_MARGINS);

        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PASS_PDF_AS_IS);
        assertFalse(mPrintJob.isValid());

        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PRINT_CLIP_CONTENT);
        assertFalse(mPrintJob.isValid());

        when(mPrintJob.getFilename()).thenReturn(".jpeg");
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PASS_PDF_AS_IS);
        assertFalse(mPrintJob.isValid());

        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PRINT_CLIP_CONTENT);
        assertFalse(mPrintJob.isValid());


        when(mPrintJob.getFilename()).thenReturn(".png");
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PASS_PDF_AS_IS);
        assertFalse(mPrintJob.isValid());

        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PRINT_CLIP_CONTENT);
        assertFalse(mPrintJob.isValid());
    }

    @Test
    public void testPrintJob_Document_WrongMargins() {
        when(mPrintJob.verifyFileIntegrity()).thenReturn(true);
        when(mPrintJob.getFilename()).thenReturn(".pdf");
        when(mPrintJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PASS_PDF_AS_IS);
        when(mPrintJob.getMimeType()).thenReturn(PrintingConstants.JobType.DOCUMENT);
        when(mPrintJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.NO_MARGINS);
        when(mPrintJob.isValid()).thenCallRealMethod();

        assertFalse(mPrintJob.isValid());
    }
}
