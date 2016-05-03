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

    public PrintingConstants.PrintMode print_mode;
    public PrintingConstants.MarginsMode margins_mode;
    public PrintingConstants.JobType objectType;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        print_mode = PrintingConstants.PrintMode.PRINT_FIT_TO_PAGE;
        margins_mode = PrintingConstants.MarginsMode.PRINTER_MARGINS;
        objectType = PrintingConstants.JobType.DOCUMENT;
    }
}
