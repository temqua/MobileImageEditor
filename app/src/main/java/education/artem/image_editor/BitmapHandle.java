package education.artem.image_editor;

import android.graphics.Bitmap;

public class BitmapHandle {

    private static final BitmapHandle ourInstance = new BitmapHandle();
    private static Bitmap bitmapHandled;
    private static String fileName;
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

    public static String getFileName() {
        return fileName;
    }

    public static void setFileName(String fileName) {
        BitmapHandle.fileName = fileName;
    }

    public static Bitmap getBitmapHandled() {
        return bitmapHandled;
    }

    public static void setBitmapHandled(Bitmap bitmapHandled) {
        BitmapHandle.bitmapHandled = bitmapHandled;
    }
}
