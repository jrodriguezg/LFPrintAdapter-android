package com.hp.lfprintadapter.model;

/**
 * Author: jrodriguezg
 * Date: 03/05/2016.
 */
public class PrintingConstants {

    // Printing settings:
    public enum FitMode { PRINT_FIT_TO_PAGE,  PRINT_CLIP_CONTENT, PASS_PDF_AS_IS }
    public enum JobType { DOCUMENT }

    // In order to be able to save a copy of the print job in the device, permissions must be granted:
    public static final boolean DUMP_FILE = true;
}
