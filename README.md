# LFPrintAdapter-android
Custom Implementation of Android's [PrintDocumentAdapter][2] ready to be used in any application.
Includes a sample app that allows the user to browse their local storage, select and print a PDF, JPEG or PNG file.

## Purpose
The [LFPrintAdapter][1] class is an implementation of Android's [PrintDocumentAdapter][2] methods that abstracts you from implementing the [onLayout()] and [onWrite()] methods. 

This implementation is supported on a Parcelable [PrintJob][3] class that is intended to encapsulate all the attributes needed to compose the print job during the [LFPrintAdapter][1] lifecycle. You may extend it to accommodate its attributes to your app's purpose.

## Usage
This sourcecode can be added to any Android project targeting API 21 or higher.

In order to use the provided [LFPrintAdapter][1], you must specify it when calling print method:

```java
try {
    PrintManager printManager = (PrintManager) getSystemService(
            Context.PRINT_SERVICE);
    printManager.print("my_document_name", new LFPrintAdapter(this, mPrintJob), null);
} catch (IOException ex) {
    Log.e(PrintingConstants.LOG_TAG,"IOException while initializing the PrintAdapter.");
}
```

[1]: /lfprintadapter/src/main/java/com/hp/lfprintadapter/LFPrintAdapter.java
[2]: https://developer.android.com/reference/android/print/PrintDocumentAdapter.html
[3]: /com/hp/lfprintadapter/model/PrintJob.java