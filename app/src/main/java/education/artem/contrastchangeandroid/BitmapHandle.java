package education.artem.contrastchangeandroid;

import android.graphics.Bitmap;

import java.io.File;

public class BitmapHandle {

    private static final BitmapHandle ourInstance = new BitmapHandle();
    private static Bitmap bitmapHandled;
    private static File fileSource;

    private static Bitmap bitmapSource;

    private BitmapHandle() {

    }

    public static BitmapHandle getInstance() {
        return ourInstance;
    }

    public static void setBitmapSource(Bitmap bitmap) {
        bitmapSource = bitmap;
    }

    public static Bitmap getBitmapSource(){
        return bitmapSource;
    }

    public static File getFileSource() {
        return fileSource;
    }

    public static void setFileSource(File fileSource) {
        BitmapHandle.fileSource = fileSource;
    }

    public static Bitmap getBitmapHandled() {
        return bitmapHandled;
    }

    public static void setBitmapHandled(Bitmap bitmapHandled) {
        BitmapHandle.bitmapHandled = bitmapHandled;
    }
}
