package com.jmrodrigg.printing;

import android.app.Application;

/**
 * Author: jrodriguezg
 * Date: 11/05/2015.
 */
public class PrintingApplication extends Application {

    // PDF Renderer:
    public Renderer renderer;

    // Printing settings:
    public enum PrintMode { PRINT_FIT_TO_PAGE, PRINT_CLIP_CONTENT }
    public enum MarginsMode { NO_MARGINS, PRINTER_MARGINS }

    public PrintMode print_mode;
    public MarginsMode margins_mode;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        print_mode = PrintMode.PRINT_FIT_TO_PAGE;
        margins_mode = MarginsMode.PRINTER_MARGINS;
    }
}
