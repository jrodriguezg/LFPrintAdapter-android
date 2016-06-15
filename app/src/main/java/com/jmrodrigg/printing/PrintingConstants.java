package com.jmrodrigg.printing;

/**
 * Author: jrodriguezg
 * Date: 03/05/2016.
 */
public class PrintingConstants {

    // Bundle parameters:
    public static final String PRINT_JOB_CLASS = "print-job-class";

    // Permissions requests:
    public static final int PERMISSION_EXT_STORAGE_READ = 1;

    // Intent actions:
    public static final int ACTION_PRINT = 1;

    // Printing settings:
    public enum FitMode { PRINT_FIT_TO_PAGE, PRINT_FILL_PAGE, PRINT_CLIP_CONTENT, PASS_PDF_AS_IS }
    public enum MarginsMode { NO_MARGINS, PRINTER_MARGINS }
    public enum JobType { DOCUMENT, IMAGE }

    public static final String LOG_TAG = "PrintSample";
}
