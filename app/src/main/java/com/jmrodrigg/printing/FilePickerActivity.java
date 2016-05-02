package com.jmrodrigg.printing;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
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

        fillList();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        File selection = childrenList.get(position);

        if (selection.isDirectory()) {
            currentFolder = selection;
            fillList();
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
        this.setTitle(title.isEmpty()?"/":title);

        childrenList = new ArrayList<>();
        Collections.addAll(childrenList, currentFolder.listFiles());

        this.setListAdapter(new FileListAdapter(getBaseContext(),R.layout.file_item,childrenList));
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
            TextView txtName,txtSize,txtDate;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(mResourceId,null);
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
                    } else if (fileName.toUpperCase().endsWith(".ONG")) {
                        // PNG:
                        resource = R.drawable.type_png;
                    }

                    // If file, set size:
                    DecimalFormat df = new DecimalFormat();
                    if (size <= 1024) txtSize.setText(getResources().getString(R.string.file_size_bytes,size));
                    else if (size <= (1024*1024)) txtSize.setText(getResources().getString(R.string.file_size_Kbytes,String.format(Locale.getDefault(),"%.2f",(float)size/1024)));
                    else txtSize.setText(getResources().getString(R.string.file_size_Mbytes,String.format(Locale.getDefault(),"%.2f",(float)size/(1024*1024))));
                }

                // Set Icon:
                imgIcon.setImageResource(resource);
                // Set name:
                txtName.setText(fileName);
                // Set Last Modified date:
                SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy - hh:mm",Locale.getDefault());
                txtDate.setText(sd.format(new Date(lastModified)));

            }

            return convertView;
        }
    }

}
