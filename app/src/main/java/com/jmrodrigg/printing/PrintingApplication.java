package com.jmrodrigg.printing;

import android.app.Application;

/**
 * Author: jrodriguezg
 * Date: 11/05/2015.
 */
public class PrintingApplication extends Application {

    // PDF Renderer:
    public Renderer renderer;
    public String filepath;

    // Printing settings:
    public enum PrintMode { PRINT_FIT_TO_PAGE, PRINT_CLIP_CONTENT, PASS_PDF_AS_IS }
    public enum MarginsMode { NO_MARGINS, PRINTER_MARGINS }
    public enum JobType { DOCUMENT, IMAGE }

    public PrintMode print_mode;
    public MarginsMode margins_mode;
    public JobType objectType;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        print_mode = PrintMode.PRINT_FIT_TO_PAGE;
        margins_mode = MarginsMode.PRINTER_MARGINS;
        objectType = JobType.DOCUMENT;
    }
}
