package com.jmrodrigg.printing;

import com.jmrodrigg.printing.controller.FileList;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Author: jrodriguezg
 * Date: 26/05/16.
 */
public class FilePickerUnitTest {

    @Test
    public void testFile_Image_Valid() {
        File aFile = Mockito.mock(File.class);
        when(aFile.getAbsolutePath()).thenReturn("validFile.png");
        assertTrue(FileList.isSupportedFileExt(aFile));

        when(aFile.getAbsolutePath()).thenReturn("validFile.jpeg");
        assertTrue(FileList.isSupportedFileExt(aFile));

        when(aFile.getAbsolutePath()).thenReturn("validFile.jpg");
        assertTrue(FileList.isSupportedFileExt(aFile));
    }

    @Test
    public void testFile_Document_Valid() {
        File aFile = Mockito.mock(File.class);
        when(aFile.getAbsolutePath()).thenReturn("validFile.pdf");
        assertTrue(FileList.isSupportedFileExt(aFile));
    }

    @Test
    public void testFile_Wrong() {
        File aFile = Mockito.mock(File.class);
        when(aFile.getAbsolutePath()).thenReturn("wrongFile.psd");
        assertFalse(FileList.isSupportedFileExt(aFile));

        when(aFile.getAbsolutePath()).thenReturn("wrongFile.ppt");
        assertFalse(FileList.isSupportedFileExt(aFile));

        when(aFile.getAbsolutePath()).thenReturn("wrongFile.tiff");
        assertFalse(FileList.isSupportedFileExt(aFile));

        when(aFile.getAbsolutePath()).thenReturn("wrongFile.txt");
        assertFalse(FileList.isSupportedFileExt(aFile));
    }
}
