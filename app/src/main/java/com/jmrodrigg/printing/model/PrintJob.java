package com.jmrodrigg.printing.model;

import com.jmrodrigg.printing.PrintingConstants;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Author: jrodriguezg
 * Date: 03/05/2016.
 */
public class PrintJob implements Parcelable {
    private String mUri;
    private String mFilename;
    private PrintingConstants.FitMode mFitMode;
    private PrintingConstants.MarginsMode mMarginsMode;
    private PrintingConstants.JobType mMimeType;

    public PrintJob() {
        mFitMode = PrintingConstants.FitMode.PRINT_FIT_TO_PAGE;
        mMarginsMode = PrintingConstants.MarginsMode.PRINTER_MARGINS;
        mMimeType = PrintingConstants.JobType.DOCUMENT;
    }

    protected PrintJob(Parcel in) {
        mUri = in.readString();
        mFilename = in.readString();
        mMimeType = (PrintingConstants.JobType) in.readSerializable();
        mFitMode = (PrintingConstants.FitMode) in.readSerializable();
        mMarginsMode = (PrintingConstants.MarginsMode) in.readSerializable();
    }

    public static final Creator<PrintJob> CREATOR = new Creator<PrintJob>() {
        @Override
        public PrintJob createFromParcel(Parcel in) {
            return new PrintJob(in);
        }

        @Override
        public PrintJob[] newArray(int size) {
            return new PrintJob[size];
        }
    };

    public void setUri(String uri) {
        mUri = uri;
    }

    public String getUri() {
        return mUri;
    }

    public boolean exists() {
        File aFile = new File(mUri);
        return aFile.exists();
    }

    public String getFilename() {
        return mFilename;
    }

    public void setFilename(String filename) {
        mFilename = filename;
    }

    public void setMimeType(PrintingConstants.JobType mimeType) {
        mMimeType = mimeType;
    }

    public PrintingConstants.JobType getMimeType() {
        return mMimeType;
    }

    public void setFitMode(PrintingConstants.FitMode fitMode) {
        mFitMode = fitMode;
    }

    public void setMarginsMode(PrintingConstants.MarginsMode margins) {
        mMarginsMode = margins;
    }

    public PrintingConstants.FitMode getFitMode() {
        return mFitMode;
    }

    public PrintingConstants.MarginsMode getMarginsMode() {
        return mMarginsMode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUri);
        dest.writeString(mFilename);
        dest.writeSerializable(mMimeType);
        dest.writeSerializable(mFitMode);
        dest.writeSerializable(mMarginsMode);
    }
}
