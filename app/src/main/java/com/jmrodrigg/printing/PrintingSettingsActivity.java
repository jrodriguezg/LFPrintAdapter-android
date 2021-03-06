package com.jmrodrigg.printing;

import com.jmrodrigg.lfprintadapter.LFPrintAdapter;
import com.jmrodrigg.lfprintadapter.model.PrintJob;
import com.jmrodrigg.lfprintadapter.model.PrintingConstants;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintManager;
import android.support.v4.print.PrintHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

import java.io.File;
import java.io.IOException;

import static com.jmrodrigg.printing.model.Constants.PRINT_JOB_CLASS;

/**
 * Author: jrodriguezg
 * Date: 11/05/2015.
 */
public class PrintingSettingsActivity extends Activity {

    PrintJob mPrintJob;

    /** Overriden methods **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printing_settings);

        this.setTitle(getString(R.string.title_activity_printing_settings));

        mPrintJob = getIntent().getParcelableExtra(PRINT_JOB_CLASS);

        // Init components:
        initComponents();
    }

    private void initComponents() {
        RadioButton rBtnPMFill, rBtnPMFit, rBtnPMClip, rBtnPMAsIs;

        rBtnPMFit = (RadioButton) findViewById(R.id.radioFit);
        rBtnPMFill = (RadioButton) findViewById(R.id.radioFill);
        rBtnPMClip = (RadioButton) findViewById(R.id.radioClip);
        rBtnPMAsIs = (RadioButton) findViewById(R.id.radioAsIs);

        if(mPrintJob.getMimeType() == PrintingConstants.JobType.IMAGE) {
            rBtnPMAsIs.setVisibility(View.GONE);
            rBtnPMClip.setVisibility(View.GONE);
        }

        switch (mPrintJob.getFitMode()) {
            case PRINT_FIT_TO_PAGE:
                rBtnPMFit.setChecked(true);
                break;
            case PRINT_FILL_PAGE:
                rBtnPMFill.setChecked(true);
                break;
            case PRINT_CLIP_CONTENT:
                rBtnPMClip.setChecked(true);
                break;
            case PASS_PDF_AS_IS:
                rBtnPMAsIs.setChecked(true);
                break;
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
                if(checked) mPrintJob.setFitMode(PrintingConstants.FitMode.PASS_PDF_AS_IS);
                break;
            // Clip the original document to the paper size selected:
            case R.id.radioClip:
                if(checked) mPrintJob.setFitMode(PrintingConstants.FitMode.PRINT_CLIP_CONTENT);
                break;
            case R.id.radioFill:
                if(checked) mPrintJob.setFitMode(PrintingConstants.FitMode.PRINT_FILL_PAGE);
                break;
            // Fit the original document to the paper size selected:
            default:
            case R.id.radioFit:
                if(checked) mPrintJob.setFitMode(PrintingConstants.FitMode.PRINT_FIT_TO_PAGE);
                break;

        }
    }

    public void doPrint(View v) {
        switch (mPrintJob.getMimeType()) {
            case DOCUMENT:
                try {
                    PrintManager printManager = (PrintManager) getSystemService(
                            Context.PRINT_SERVICE);
                    printManager.print("document", new LFPrintAdapter(this, mPrintJob), null);
                } catch (IOException ex) {
                    Log.e(PrintingConstants.LOG_TAG,"IOException while initializing the PrintAdapter.");
                }
                break;

            case IMAGE:
                try{
                    com.jmrodrigg.lfprintadapter.LFRollHelper pHelper = new com.jmrodrigg.lfprintadapter.LFRollHelper(this.getBaseContext());

                    if (mPrintJob.getFitMode().equals(PrintingConstants.FitMode.PRINT_FILL_PAGE)) {
                        pHelper.setScaleMode(PrintHelper.SCALE_MODE_FILL);
                    } else if (mPrintJob.getFitMode().equals(PrintingConstants.FitMode.PRINT_FIT_TO_PAGE)) {
                        pHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                    } else
                        throw new Exception("Print Mode not supported");

                    File f = new File(mPrintJob.getUri());
                    pHelper.printBitmap("image", Uri.fromFile(f), null);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    private void finishActivity() {
        Intent intent = new Intent();
        intent.putExtra(PRINT_JOB_CLASS,mPrintJob);
        setResult(RESULT_OK,intent);
        finish();
    }
}
