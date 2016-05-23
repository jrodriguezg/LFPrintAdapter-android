package com.jmrodrigg.printing;

import com.jmrodrigg.printing.model.PrintJob;

import org.mockito.Mockito;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

/**
 * Author: jrodriguezg
 * Date: 23/05/16.
 */
public class PrintJobTest extends ActivityInstrumentationTestCase2<PrintingSettingsActivity> {
    PrintingSettingsActivity mPrintingSettingsActivity;

    PrintJob mMockDocJob;

    public PrintJobTest() {
        super(PrintingSettingsActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());

        Intent intent = new Intent();

        // Mock a Document PrintJob:
        mMockDocJob = Mockito.mock(PrintJob.class);
        when(mMockDocJob.getFilename()).thenReturn("aFile.pdf");
        when(mMockDocJob.exists()).thenReturn(Boolean.TRUE);
        when(mMockDocJob.getFitMode()).thenReturn(PrintingConstants.FitMode.PASS_PDF_AS_IS);
        when(mMockDocJob.getMarginsMode()).thenReturn(PrintingConstants.MarginsMode.NO_MARGINS);
        when(mMockDocJob.getMimeType()).thenReturn(PrintingConstants.JobType.DOCUMENT);

        intent.setExtrasClassLoader(ClassLoader.getSystemClassLoader());
        intent.putExtra(PrintingConstants.PRINT_JOB_CLASS,mMockDocJob);
        setActivityIntent(intent);
        mPrintingSettingsActivity = getActivity();
    }

    public void testJobType() {
        PrintingConstants.JobType jobType = mPrintingSettingsActivity.mPrintJob.getMimeType();
        String filename = mPrintingSettingsActivity.mPrintJob.getFilename();

        if (jobType.equals(PrintingConstants.JobType.DOCUMENT))
            assertThat(filename.toUpperCase().endsWith(".PDF"), is(true));
        else if (jobType.equals(PrintingConstants.JobType.IMAGE)) {
            boolean valid_extension = (filename.toUpperCase().endsWith(".JPG") || filename.toUpperCase().endsWith(".JPEG") || filename.toUpperCase().endsWith(".PNG"));
            assertThat(valid_extension, is(true));
        }
    }

    public void testValidUri() {
        File file = new File(mPrintingSettingsActivity.mPrintJob.getUri());
        assertThat(file.exists(), is(true));
    }

}
