package com.jmrodrigg.printing;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.print.PrintManager;
import android.support.v4.print.PrintHelper;
import android.view.Menu;
import android.view.View;
import android.widget.RadioButton;

/**
 * Author: jrodriguezg
 * Date: 11/05/2015.
 */
public class PrintingSettingsActivity extends Activity {

    /** Overriden methods **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printing_settings);

        if(((PrintingApplication)getApplication()).objectType == PrintingConstants.JobType.IMAGE){
            RadioButton btn = (RadioButton) findViewById(R.id.radioAsIs);
            btn.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_printing_settings, menu);
        return false;
    }

    /** Events **/

    public void onPrintModeClicked(View v) {
        boolean checked = ((RadioButton) v).isChecked();

        switch(v.getId()) {
            // Send exactly the original document. No processing:
            case R.id.radioAsIs:
                if(checked) ((PrintingApplication)getApplication()).print_mode = PrintingConstants.PrintMode.PASS_PDF_AS_IS;
                break;
            // Clip the original document to the paper size selected:
            case R.id.radioClip:
                if(checked) ((PrintingApplication)getApplication()).print_mode = PrintingConstants.PrintMode.PRINT_CLIP_CONTENT;
                break;
            // Fit the original document to the paper size selected:
            default:
            case R.id.radioFit:
                if(checked) ((PrintingApplication)getApplication()).print_mode = PrintingConstants.PrintMode.PRINT_FIT_TO_PAGE;
                break;

        }
    }

    public void onMarginsModeClicked(View v) {
        boolean checked = ((RadioButton) v).isChecked();

        switch(v.getId()) {
            case R.id.radioNoMargins:
                if(checked) ((PrintingApplication)getApplication()).margins_mode = PrintingConstants.MarginsMode.NO_MARGINS;
                break;
            case R.id.radioPrinterMargins:
                if(checked) ((PrintingApplication)getApplication()).margins_mode = PrintingConstants.MarginsMode.PRINTER_MARGINS;
                break;
        }
    }

    public void doPrint(View v) {
        switch (((PrintingApplication)getApplication()).objectType) {
            case DOCUMENT:
                PrintManager printManager = (PrintManager) getSystemService(
                        Context.PRINT_SERVICE);
                printManager.print("document",new PrintAdapter(this),null);
                break;

            case IMAGE:
                try{
                    PrintHelper pHelper = new PrintHelper(this);

                    if(((PrintingApplication)getApplication()).print_mode == PrintingConstants.PrintMode.PRINT_CLIP_CONTENT) {
                        pHelper.setScaleMode(PrintHelper.SCALE_MODE_FILL);
                    } else if(((PrintingApplication)getApplication()).print_mode == PrintingConstants.PrintMode.PRINT_FIT_TO_PAGE) {
                        pHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                    } else {
                        throw new Exception("Print Mode not supported");
                    }

                    Bitmap bmp = BitmapFactory.decodeFile(((PrintingApplication) getApplication()).filepath);
                    pHelper.printBitmap("image", bmp);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
        }

    }
}
