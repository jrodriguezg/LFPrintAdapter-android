package com.jmrodrigg.printing;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Author: jrodriguezg
 * Date: 30/05/16.
 */
public class FileListAdapterTest extends ActivityInstrumentationTestCase2<FilePickerActivity> {

    FilePickerActivity.FileListAdapter mFileListAdapter;
    @Mock
    ArrayList mFiles;
    @Mock
    File aFile;

    public FileListAdapterTest() {
        super(FilePickerActivity.class);
    }

    @Before
    public void setUp() {
        System.setProperty(
                "dexmaker.dexcache",
                getInstrumentation().getTargetContext().getCacheDir().getPath());

        mFiles = Mockito.mock(ArrayList.class);
        aFile = Mockito.mock(File.class);
        mFileListAdapter = getActivity().new FileListAdapter(getActivity().getBaseContext(),R.layout.file_item,mFiles);
    }

    public void testListItem_Image_Valid() {
        Mockito.when(aFile.getName()).thenReturn("validFile.jpeg");
        Mockito.when(aFile.isDirectory()).thenReturn(false);

        Mockito.when(mFiles.get(0)).thenReturn(aFile);

        View view = mFileListAdapter.getView(0,null,null);

        assertThat(view, instanceOf(view.getClass()));

        ImageView imgIcon = (ImageView) view.findViewById(R.id.ImgViewFiletype);
        assertTrue(imgIcon.getDrawable().getConstantState().equals
                (getActivity().getBaseContext().getResources().getDrawable(R.drawable.type_jpg).getConstantState()));


        Mockito.when(aFile.getName()).thenReturn("validFile.png");
        view = mFileListAdapter.getView(0,null,null);

        assertThat(view, instanceOf(view.getClass()));

        imgIcon = (ImageView) view.findViewById(R.id.ImgViewFiletype);
        assertTrue(imgIcon.getDrawable().getConstantState().equals
                (getActivity().getBaseContext().getResources().getDrawable(R.drawable.type_png).getConstantState()));

    }

    public void testListItem_Document_Valid() {
        Mockito.when(aFile.getName()).thenReturn("validFile.pdf");
        Mockito.when(aFile.isDirectory()).thenReturn(false);

        Mockito.when(mFiles.get(0)).thenReturn(aFile);

        View view = mFileListAdapter.getView(0,null,null);
        assertThat(view, instanceOf(view.getClass()));

        ImageView imgIcon = (ImageView) view.findViewById(R.id.ImgViewFiletype);
        assertTrue(imgIcon.getDrawable().getConstantState().equals
                (getActivity().getBaseContext().getResources().getDrawable(R.drawable.type_pdf).getConstantState()));
    }

    public void testListItem_Folder_Valid() {
        Mockito.when(aFile.isDirectory()).thenReturn(true);

        Mockito.when(mFiles.get(0)).thenReturn(aFile);

        View view = mFileListAdapter.getView(0,null,null);
        assertThat(view, instanceOf(view.getClass()));

        ImageView imgIcon = (ImageView) view.findViewById(R.id.ImgViewFiletype);
        assertTrue(imgIcon.getDrawable().getConstantState().equals
                (getActivity().getBaseContext().getResources().getDrawable(R.drawable.type_folder).getConstantState()));
    }
}
