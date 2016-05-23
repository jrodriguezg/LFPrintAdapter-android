package com.jmrodrigg.printing;

import com.jmrodrigg.printing.controller.FileList;

import org.junit.Before;

import android.test.ActivityInstrumentationTestCase2;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Author: jrodriguezg
 * Date: 23/05/16.
 */
public class FilePickerTest extends ActivityInstrumentationTestCase2<FilePickerActivity> {
    FilePickerActivity mFilePickerActivity;

    public FilePickerTest() {
        super(FilePickerActivity.class);
    }

    @Before
    public void setUp() throws Exception {

        mFilePickerActivity = getActivity();
    }

    public void testListIsValid() {
        for (File f:mFilePickerActivity.mFileListController) {
            if (f.isDirectory()) continue;

            assertThat(FileList.isSupportedFileExt(f), is(true));
        }
    }

}
