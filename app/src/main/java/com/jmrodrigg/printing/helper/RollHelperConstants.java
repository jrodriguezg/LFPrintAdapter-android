package com.jmrodrigg.printing.helper;

import android.graphics.Bitmap;

/**
 * Author: jrodriguezg
 * Date: 13/06/2016.
 */
public interface RollHelperConstants {
    Bitmap.Config COLOR_CONFIG  = Bitmap.Config.RGB_565;
    int SUBSAMPLING_VALUE       = 2;
    boolean IN_DITHER           = false;
    boolean QUALITY_OVER_SPEED  = false;

    int TILE_SIZE               = 1024;
}
