# LFPrintAdapter-android
Custom Implementation of Android's PrintDocumentAdapter ready to be used in any application.

## Purpose
The [LFPrintAdapter](/com/hp/lfprintadapter/LFPrintAdapter.java) class is an implementation of Android's [PrintDocumentAdapter](https://developer.android.com/reference/android/print/PrintDocumentAdapter.html) that abstracts you from implementing the [onLayout()](https://developer.android.com/reference/android/print/PrintDocumentAdapter.html#onLayout(android.print.PrintAttributes, android.print.PrintAttributes, android.os.CancellationSignal, android.print.PrintDocumentAdapter.LayoutResultCallback, android.os.Bundle)) and [onWrite()](https://developer.android.com/reference/android/print/PrintDocumentAdapter.html#onWrite(android.print.PageRange[], android.os.ParcelFileDescriptor, android.os.CancellationSignal, android.print.PrintDocumentAdapter.WriteResultCallback)) methods. 

This implementation is supported on a Parcelable [PrintJob](/com/hp/lfprintadapter/model/PrintJob.java) class that is intended to encapsulate all the attributes needed to compose the print job during the [LFPrintAdapter](/com/hp/lfprintadapter/LFPrintAdapter.java) lifecycle. You may extend it to accomodate its attributes to your app's purpose.

## Usage
This sourcecode can be added to any Android project targeting API 21 or higher.

In order to use the provided [LFPrintAdapter](/com/hp/lfprintadapter/LFPrintAdapter.java), you must specify it when calling print method:

```java
try {
    PrintManager printManager = (PrintManager) getSystemService(
            Context.PRINT_SERVICE);
    printManager.print("my_document_name", new LFPrintAdapter(this, mPrintJob), null);
} catch (IOException ex) {
    Log.e(PrintingConstants.LOG_TAG,"IOException while initializing the PrintAdapter.");
}
```

