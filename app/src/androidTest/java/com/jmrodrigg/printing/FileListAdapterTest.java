package com.jmrodrigg.printing;

import com.jmrodrigg.printing.controller.FileListAdapter;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.test.AndroidTestCase;
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
public class FileListAdapterTest extends AndroidTestCase {

    FileListAdapter mFileListAdapter;

    ArrayList<File> mFiles;
    @Mock
    File aFile;

    @Before
    public void setUp() throws Exception{
        super.setUp();

        Context context = InstrumentationRegistry.getTargetContext();
        setContext(context);
        assertNotNull(context);

        aFile = Mockito.mock(File.class);
        Mockito.when(aFile.isDirectory()).thenReturn(false);

        mFiles = new ArrayList<>();
        mFiles.add(aFile);

        mFileListAdapter = new FileListAdapter(getContext(), R.layout.file_item, mFiles);
    }

    public void testListItem_Image_Valid() {
        Mockito.when(aFile.getName()).thenReturn("validFile.jpeg");
        Mockito.when(aFile.isDirectory()).thenReturn(false);

        View view = mFileListAdapter.getView(0,null,null);

        assertThat(view, instanceOf(view.getClass()));

        ImageView imgIcon = (ImageView) view.findViewById(R.id.ImgViewFiletype);
        assertTrue(imgIcon.getDrawable().getConstantState().equals
                (getContext().getDrawable(R.drawable.type_jpg).getConstantState()));

        Mockito.when(aFile.getName()).thenReturn("validFile.jpg");
        Mockito.when(aFile.isDirectory()).thenReturn(false);

        view = mFileListAdapter.getView(0,null,null);

        assertThat(view, instanceOf(view.getClass()));

        imgIcon = (ImageView) view.findViewById(R.id.ImgViewFiletype);
        assertTrue(imgIcon.getDrawable().getConstantState().equals
                (getContext().getDrawable(R.drawable.type_jpg).getConstantState()));


        Mockito.when(aFile.getName()).thenReturn("validFile.png");
        view = mFileListAdapter.getView(0,null,null);

        assertThat(view, instanceOf(view.getClass()));

        imgIcon = (ImageView) view.findViewById(R.id.ImgViewFiletype);
        assertTrue(imgIcon.getDrawable().getConstantState().equals
                (getContext().getDrawable(R.drawable.type_png).getConstantState()));

    }

    public void testListItem_Document_Valid() {
        Mockito.when(aFile.getName()).thenReturn("validFile.pdf");

        View view = mFileListAdapter.getView(0,null,null);
        assertThat(view, instanceOf(view.getClass()));

        ImageView imgIcon = (ImageView) view.findViewById(R.id.ImgViewFiletype);
        assertTrue(imgIcon.getDrawable().getConstantState().equals
                (getContext().getDrawable(R.drawable.type_pdf).getConstantState()));
    }

    public void testListItem_Folder_Valid() {
        Mockito.when(aFile.isDirectory()).thenReturn(true);

        View view = mFileListAdapter.getView(0,null,null);
        assertThat(view, instanceOf(view.getClass()));

        ImageView imgIcon = (ImageView) view.findViewById(R.id.ImgViewFiletype);
        assertTrue(imgIcon.getDrawable().getConstantState().equals
                (getContext().getDrawable(R.drawable.type_folder).getConstantState()));
    }

    public void testListItem_Unknown_Valid() {
        Mockito.when(aFile.getName()).thenReturn("unknown.txt");
        Mockito.when(aFile.isDirectory()).thenReturn(false);

        View view = mFileListAdapter.getView(0,null,null);
        assertThat(view, instanceOf(view.getClass()));

        ImageView imgIcon = (ImageView) view.findViewById(R.id.ImgViewFiletype);

        assertTrue(imgIcon.getDrawable().getConstantState().equals
                (getContext().getDrawable(R.drawable.type_generic).getConstantState()));
    }
}
