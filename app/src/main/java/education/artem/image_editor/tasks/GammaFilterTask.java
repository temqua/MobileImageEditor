package education.artem.image_editor.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import education.artem.image_editor.BitmapHandle;
import education.artem.image_editor.CurrentOperation;
import education.artem.image_editor.OperationName;
import education.artem.image_editor.ProcessTask;
//import education.artem.image_editor.jhlabs.filters.GammaFilter;
//import education.artem.image_editor.jhlabs.filters.util.AndroidUtils;

public class GammaFilterTask extends ProcessTask {


    public GammaFilterTask(Context currContext, ImageView imageView, TextView status, ProgressBar progress, TextView exec, TextView cancelView) {
        super(currContext, imageView, status, progress, exec, cancelView);
    }

    @Override
    protected Bitmap doInBackground(OperationName... params) {
        try {
            double gamma = 1d;
            String gammaStr = CurrentOperation.getOperationParams().get("gamma");
            if (gammaStr != null) {
                gamma = Double.parseDouble(gammaStr);
            }
            return gammaCorr(BitmapHandle.getBitmapHandled(), gamma);
        } catch (Exception e) {
            this.e = e;
            if (e.getMessage() != null) {
                Log.e(getClass().getName(), e.getMessage());
            }
            cancel(true);
        }
        return null;
    }

    public Bitmap gammaCorr(Bitmap image, double gamma) {
//        Bitmap editedBitmap = image.copy(image.getConfig(), true);
//        GammaFilter filter = new GammaFilter();
//        float g = (float) gamma;
//        filter.setGamma(g);
//        int[] bitmap = AndroidUtils.bitmapToIntArray(image);
//        int[] editedInt = filter.filter(bitmap, image.getWidth(), image.getHeight());
//        editedBitmap.copyPixelsFromBuffer(IntBuffer.wrap(editedInt));
        return image;
    }


    public Bitmap gammaCorrection(Bitmap image, double gamma) {
        Bitmap newImage = image.copy(image.getConfig(), true);
        int height = image.getHeight();
        int width = image.getWidth();
        double c = 1d;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (isCancelled()) {
                    return null;
                }
                if (!(i == 0 || j == 0 || i == width - 1 || j == height - 1)) {
                    int color = image.getPixel(i - 1, j - 1);
                    int indexR = Color.red(color);
                    int indexG = Color.green(color);
                    int indexB = Color.blue(color);
                    double red = c * Math.pow(indexR / 255, gamma) * 255;
                    double green = c * Math.pow(indexG / 255, gamma) * 255;
                    double blue = c * Math.pow(indexB / 255, gamma) * 255;
                    indexR = (int) red;
                    indexR = Math.min(indexR, 255);
                    indexR = Math.max(indexR, 0);
                    indexG = (int) green;
                    indexG = Math.min(indexG, 255);
                    indexG = Math.max(indexG, 0);
                    indexB = (int) blue;
                    indexB = Math.min(indexB, 255);
                    indexB = Math.max(indexB, 0);
                    newImage.setPixel(i, j, Color.rgb(indexR, indexG, indexB));
                }
            }
            double progress = (double) i / width * 100;
            publishProgress((int) progress);
        }
        return newImage;
    }

    private Bitmap GammaCorrection(Bitmap sourceBitmap, double gamma) {
        int height = sourceBitmap.getHeight();
        int width = sourceBitmap.getWidth();
        double c = 1d;
        ByteBuffer pixelBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
        ByteBuffer resultBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
        int stride = sourceBitmap.getRowBytes();
        sourceBitmap.copyPixelsToBuffer(pixelBuffer);
        Bitmap resultBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);

        int current;
        final int cChannels = 3;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                current = y * stride + x * 4;
                for (int i = 0; i < cChannels; i++) {
                    double range = (double) pixelBuffer.array()[current + i] / 255;
                    double correction = c * Math.pow(range, gamma);
                    resultBuffer.array()[current + i] = (byte) (correction * 255);
                }
                resultBuffer.array()[current + 3] = (byte) 255;
            }
            double progress = (double) y / height * 100;

            publishProgress((int) progress);
        }
        resultBitmap.copyPixelsFromBuffer(resultBuffer);
        return resultBitmap;
    }
}
