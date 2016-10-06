package com.jmrodrigg.printing;

import com.jmrodrigg.printing.controller.FileList;
import com.jmrodrigg.printing.controller.FileListAdapter;
import com.jmrodrigg.printing.samples.PrintCustomContent;

import com.jmrodrigg.lfprintadapter.model.PrintJob;
import com.jmrodrigg.lfprintadapter.model.PrintingConstants;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

/**
 * Author: jrodriguezg
 * Date: 02/05/2016.
 */
public class FilePickerActivity extends ListActivity {

    FileList mFileListController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add back button on actionbar:
        if (getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);

        mFileListController = new FileList();

        // Check permissions to READ External storage:
        if(Build.VERSION.SDK_INT >= 23) {
            if ((ContextCompat.checkSelfPermission(FilePickerActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(FilePickerActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PrintingConstants.PERMISSION_EXT_STORAGE_READ);
            } else
                fillList();
        } else
            fillList();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PrintingConstants.PERMISSION_EXT_STORAGE_READ:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    fillList();
                else
                    Toast.makeText(FilePickerActivity.this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        File selection = mFileListController.get(position);

        if (selection.isDirectory()) {
            mFileListController.loadChildFolder(position);
            fillList();
        } else {
            String fileName = selection.getAbsolutePath();
            if (FileList.isSupportedFileExt(selection)) {

                PrintingConstants.JobType mimeType = fileName.toUpperCase().endsWith(".PDF") ? PrintingConstants.JobType.DOCUMENT : PrintingConstants.JobType.IMAGE;

                Intent intent = new Intent(FilePickerActivity.this, Viewer.class);
                PrintJob job = new PrintJob();
                job.setUri(fileName);
                job.setFilename(selection.getName());
                job.setMimeType(mimeType);
                intent.putExtra(PrintingConstants.PRINT_JOB_CLASS,job);
                startActivityForResult(intent, 1);

            } else
                Toast.makeText(FilePickerActivity.this, getString(R.string.unsupported_mime_type), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filepicker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case android.R.id.home:
                if (!mFileListController.isRootFolder()) {
                    mFileListController.loadParentFolder();
                    fillList();
                    return true;
                }
                break;
            case R.id.action_sample:
                intent = new Intent(this,PrintCustomContent.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mFileListController.isRootFolder()) {
            finish();
        } else {
            mFileListController.loadParentFolder();
            fillList();
        }
    }

    private void fillList() {
        this.setTitle(mFileListController.fillList());
        this.setListAdapter(new FileListAdapter(getBaseContext(), R.layout.file_item, mFileListController));
    }

}
