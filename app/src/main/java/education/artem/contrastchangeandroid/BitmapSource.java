package education.artem.contrastchangeandroid;

import android.graphics.Bitmap;

public class BitmapSource {

    private static final BitmapSource ourInstance = new BitmapSource();

    public static BitmapSource getInstance() {
        return ourInstance;
    }

    private BitmapSource(){

    }

    private static Bitmap bitmapSource;

    public static void setBitmapSource(Bitmap bitmap) {
        bitmapSource = bitmap;
    }

    public static Bitmap getBitmapSource(){
        return bitmapSource;
    }


}
