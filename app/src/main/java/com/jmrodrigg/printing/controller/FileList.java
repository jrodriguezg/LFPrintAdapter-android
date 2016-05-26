package com.jmrodrigg.printing.controller;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 * Author: jrodriguezg
 * Date: 23/05/16.
 */
public class FileList extends ArrayList<File> {

    private File mRootFolder;
    private File mCurrentFolder;

    /**
     * Constructor.
     */
    public FileList() {
        mRootFolder = Environment.getExternalStorageDirectory();
        mCurrentFolder = getRootFolder();
    }

    public File getRootFolder() {
        return mRootFolder;
    }

    public File getCurrentFolder() {
        return mCurrentFolder;
    }

    public void setCurrentFolder(File mCurrentFolder) {
        this.mCurrentFolder = mCurrentFolder;
    }

    /**
     * Loads the list of items (folders and supported files) contained in the current folder.
     * @return the current path in form of a String.
     */
    public String fillList() {
        this.clear();

        for(File aFile:this.getCurrentFolder().listFiles()) {
            if (isSupportedFileExt(aFile)) this.add(aFile);
        }

        String title = generateFolderTitle();
        return title.isEmpty() ? "/" : title;
    }

    public void loadChildFolder(int pos) {
        setCurrentFolder(this.get(pos));
    }

    public boolean isRootFolder() {
        return this.getCurrentFolder().equals(this.getRootFolder());
    }

    public void loadParentFolder() {
        setCurrentFolder(this.getCurrentFolder().getParentFile());
    }

    /**
     * Recursive function that composes the current path.
     * @return the path composed in form of a String.
     */
    private String generateFolderTitle(File folder) {
        if (folder.equals(this.getRootFolder())) return "";
        else return generateFolderTitle(folder.getParentFile()) + "/" + folder.getName();
    }

    public String generateFolderTitle() {
        return isRootFolder() ? "/" : generateFolderTitle(this.getCurrentFolder());
    }

    /**
     * Check if a specific file extension is supported.
     * @return true if the file extension is supported.
     */
    public static boolean isSupportedFileExt(final File aFile) {
        if (aFile.isDirectory())
            return true;
        else {
            String filename = aFile.getAbsolutePath();

            if (filename.toUpperCase().endsWith(".PDF")
                || filename.toUpperCase().endsWith(".JPEG") || filename.toUpperCase().endsWith(".JPEG")
                || filename.toUpperCase().endsWith(".PNG"))
                return true;
        }

        return false;
    }

}
