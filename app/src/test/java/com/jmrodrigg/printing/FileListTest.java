package com.jmrodrigg.printing;

import com.jmrodrigg.printing.controller.FileList;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Author: jrodriguezg
 * Date: 26/05/16.
 */
public class FileListTest {

    @Test
    public void testFileList_Image_Valid() {
        File aFile = Mockito.mock(File.class);
        when(aFile.getAbsolutePath()).thenReturn("validFile.png");
        assertTrue(FileList.isSupportedFileExt(aFile));

        when(aFile.getAbsolutePath()).thenReturn("validFile.jpeg");
        assertTrue(FileList.isSupportedFileExt(aFile));

        when(aFile.getAbsolutePath()).thenReturn("validFile.jpg");
        assertTrue(FileList.isSupportedFileExt(aFile));
    }

    @Test
    public void testFileList_Document_Valid() {
        File aFile = Mockito.mock(File.class);
        when(aFile.getAbsolutePath()).thenReturn("validFile.pdf");
        assertTrue(FileList.isSupportedFileExt(aFile));
    }

    @Test
    public void testFileList_Wrong() {
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

    @Test
    public void testFileList_FolderTitle() {
        FileList aFileList = Mockito.mock(FileList.class,Mockito.CALLS_REAL_METHODS);

        final String absPath = "/storage/emulated/0/";
        final String childPath = "folder";
        final String childChildPath = "folder";

        File rootFolder = new File(absPath);

        when(aFileList.getRootFolder()).thenReturn(rootFolder);
        when(aFileList.getCurrentFolder()).thenReturn(rootFolder);
        assertEquals(aFileList.generateFolderTitle(),"/");

        File childFolder = new File(absPath + childPath);
        when(aFileList.getCurrentFolder()).thenReturn(childFolder);
        assertEquals(aFileList.generateFolderTitle(),"/" + childPath);

        File childChildFolder = new File(absPath + childPath + childChildPath);
        when(aFileList.getCurrentFolder()).thenReturn(childChildFolder);
        assertEquals(aFileList.generateFolderTitle(),"/" + childPath + childChildPath);
    }
}
