package com.jmrodrigg.printing;

import android.Manifest;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FilePickerActivity extends ListActivity {

    File rootFolder = Environment.getExternalStorageDirectory();
    File currentFolder;

    ArrayList<File> childrenList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentFolder = rootFolder;

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

        File selection = childrenList.get(position);

        if (selection.isDirectory()) {
            currentFolder = selection;
            fillList();
        } else {
            String fileName = selection.getAbsolutePath();
            if (fileName.toUpperCase().endsWith(".PDF")
                    || fileName.toUpperCase().endsWith(".JPG")
                    || fileName.toUpperCase().endsWith(".JPEG")
                    || fileName.toUpperCase().endsWith(".PNG")) {

                PrintingConstants.JobType type = fileName.toUpperCase().endsWith(".PDF") ? PrintingConstants.JobType.DOCUMENT : PrintingConstants.JobType.IMAGE;

                Intent intent = new Intent(FilePickerActivity.this, Viewer.class);
                intent.putExtra(PrintingConstants.FILE_URI, fileName);
                intent.putExtra(PrintingConstants.FILE_MIMETYPE, type);

                startActivityForResult(intent, 1);
            } else
                Toast.makeText(FilePickerActivity.this, getString(R.string.unsupported_mime_type), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (rootFolder.equals(currentFolder)) {
            finish();
        } else {
            currentFolder = currentFolder.getParentFile();
            fillList();
        }
    }

    private void fillList() {
        String title = generateFolderTitle(currentFolder);
        this.setTitle(title.isEmpty() ? "/" : title);

        childrenList = new ArrayList<>();
        Collections.addAll(childrenList, currentFolder.listFiles());

        this.setListAdapter(new FileListAdapter(getBaseContext(), R.layout.file_item, childrenList));
    }

    private String generateFolderTitle(File folder) {
        if (folder.equals(rootFolder)) return "";
        else return generateFolderTitle(folder.getParentFile()) + "/" + folder.getName();
    }

    class FileListAdapter extends ArrayAdapter<File> {

        Context mContext;
        int mResourceId;
        List<File> mObjects;

        public FileListAdapter(Context context, int resource, List<File> objects) {
            super(context, resource, objects);

            mContext = context;
            mResourceId = resource;
            mObjects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imgIcon;
            TextView txtName, txtSize, txtDate;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(mResourceId, null);
            }

            File file = mObjects.get(position);
            if (file != null) {
                imgIcon = (ImageView) convertView.findViewById(R.id.ImgViewFiletype);
                txtName = (TextView) convertView.findViewById(R.id.txtFileName);
                txtSize = (TextView) convertView.findViewById(R.id.txtSize);
                txtDate = (TextView) convertView.findViewById(R.id.txtDate);

                int resource = R.drawable.type_generic;

                String fileName = file.getName();
                long size = file.length();
                long lastModified = file.lastModified();

                if (file.isDirectory()) {
                    resource = R.drawable.type_folder;
                    txtSize.setText("");
                } else {
                    // Determine mime type:
                    if (fileName.toUpperCase().endsWith(".PDF")) {
                        // PDF:
                        resource = R.drawable.type_pdf;
                    } else if (fileName.toUpperCase().endsWith(".JPG") || file.getName().toUpperCase().endsWith(".JPEG")) {
                        // JPG:
                        resource = R.drawable.type_jpg;
                    } else if (fileName.toUpperCase().endsWith(".PNG")) {
                        // PNG:
                        resource = R.drawable.type_png;
                    }

                    // If file, set size:
                    if (size <= 1024)
                        txtSize.setText(getResources().getString(R.string.file_size_bytes, size));
                    else if (size <= (1024 * 1024))
                        txtSize.setText(getResources().getString(R.string.file_size_Kbytes, String.format(Locale.getDefault(), "%.2f", (float) size / 1024)));
                    else
                        txtSize.setText(getResources().getString(R.string.file_size_Mbytes, String.format(Locale.getDefault(), "%.2f", (float) size / (1024 * 1024))));
                }

                // Set Icon:
                imgIcon.setImageResource(resource);
                // Set name:
                txtName.setText(fileName);
                // Set Last Modified date:
                SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy - hh:mm", Locale.getDefault());
                txtDate.setText(sd.format(new Date(lastModified)));

            }

            return convertView;
        }
    }

}
