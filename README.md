# LFPrintAdapter-android
Custom Implementation of Android's [PrintDocumentAdapter][2] ready to be used in any application.
Includes a sample app that allows the user to browse their local storage, select and print a PDF, JPEG or PNG file.

## Purpose
The [LFPrintAdapter class][1] is an implementation of Android's [PrintDocumentAdapter][2] methods that abstracts you from implementing the _onLayout()_ and _onWrite()_ methods. 

This implementation is supported on a Parcelable [PrintJob][3] class that is intended to encapsulate all the attributes needed to compose the print job during the [LFPrintAdapter][1] lifecycle. You may extend it to accommodate its attributes to your app's purpose.

## Content
This repository is a Gradle project formed by two modules:

- The [lfprintadapter library][4], that contains a custom implementation of Android's PrintDocumentAdapter and a class that improves the experience when printing on roll printers when printing images.
- An [Android application][5] that acts as an example of how to use the lfprintadapter library. 

The [lfprintadapter library][4] contains two flavours:
+ The _dumpFile_ flavour will make a copy of the print job (in form of PDF file) on the local device storage. The file will be copied under the /printing/ folder. 
+ The _noDumpFile_ flavour prevents the writing of the print job on local device storage.

## Usage
The [lfprintadapter library][4] can be added to any Android project targeting API 21 or higher. You may import it into your Gradle project as a library dependency.
In order to use the provided [LFPrintAdapter class][1], you must specify it when calling print method in your app:

```java
try {
    PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
    printManager.print("my_document_name", new LFPrintAdapter(this, mPrintJob), null);
} catch (IOException ex) {
    // TODO Catch the exception properly.
}
```

On the other hand, in order to integrate the [LFRollHelper class][9], you must create an object, set a scale mode (FIT or FILL) and call the _printBitmap()_ method as detailed below: 

```java
try {
    LFRollHelper pHelper = new LFRollHelper(this.getBaseContext());
    pHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
    
    File f = new File(mPrintJob.getUri());
    pHelper.printBitmap("image", Uri.fromFile(f), null);
} catch (Exception ex) {
    // TODO Catch the exception properly.
}
```

## More info
- Have a look at the [Starter Guide][6] for a brief summary of Android's print API and how to start implementing print functionality in your app. 
- For a more detailed documentation on Android's print API, visit the [Android Developers' portal][7] and the [package summary][8].

[1]: /lfprintadapter/src/main/java/com/jmrodrigg/lfprintadapter/LFPrintAdapter.java
[2]: https://developer.android.com/reference/android/print/PrintDocumentAdapter.html
[3]: /com/jmrodrigg/lfprintadapter/model/PrintJob.java
[4]: /lfprintadapter
[5]: /app
[6]: /Starter_Guide.pdf
[7]: https://developer.android.com/training/printing/index.html
[8]: https://developer.android.com/reference/android/print/package-summary.html
[9]: /lfprintadapter/src/main/java/com/jmrodrigg/lfprintadapter/LFRollHelper.java