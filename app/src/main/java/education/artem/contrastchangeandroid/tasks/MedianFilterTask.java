package education.artem.contrastchangeandroid.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import education.artem.contrastchangeandroid.BitmapSource;
import education.artem.contrastchangeandroid.OperationName;
import education.artem.contrastchangeandroid.ProcessTask;

public class MedianFilterTask extends ProcessTask {
    public MedianFilterTask(Context currContext, ImageView imageView, TextView status, ProgressBar progress, TextView exec) {
        super(currContext, imageView, status, progress, exec);
    }

    @Override
    protected Bitmap doInBackground(OperationName... params) {
        switch(params[0]) {
            case FILTER_3x3:
                return MedianFilter(BitmapSource.getBitmapSource(), 3, 0, false);
            case FILTER_5x5:
                return MedianFilter(BitmapSource.getBitmapSource(), 5, 0, false);
            case FILTER_7x7:
                return MedianFilter(BitmapSource.getBitmapSource(), 7, 0, false);
            case FILTER_9x9:
                return MedianFilter(BitmapSource.getBitmapSource(), 9, 0, false);
            case FILTER_11x11:
                return MedianFilter(BitmapSource.getBitmapSource(), 11, 0, false);
        }
        return MedianFilter(BitmapSource.getBitmapSource(), 3, 0, false);
    }

    public Bitmap MedianFilter(Bitmap sourceBitmap,
                                      int matrixSize,
                                      int bias,
                                      boolean grayscale) {

        ByteBuffer pixelBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
        ByteBuffer resultBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());

        sourceBitmap.copyPixelsToBuffer(pixelBuffer);

        if (grayscale) {
            float rgb = 0;

            for (int k = 0; k < pixelBuffer.array().length; k += 4) {
                rgb = pixelBuffer.array()[k] * 0.11f;
                rgb += pixelBuffer.array()[k + 1] * 0.59f;
                rgb += pixelBuffer.array()[k + 2] * 0.3f;

                pixelBuffer.array()[k] = (byte) rgb;
                pixelBuffer.array()[k + 1] = pixelBuffer.array()[k];
                pixelBuffer.array()[k + 2] = pixelBuffer.array()[k];
                pixelBuffer.array()[k + 3] = (byte) 255;
            }
        }


        int filterOffset = (matrixSize - 1) / 2;
        int calcOffset = 0;


        int byteOffset = 0;
        double progress = 0;
        int top = sourceBitmap.getHeight() - filterOffset;
        List<Integer> neighbourPixels = new ArrayList<Integer>();
        byte[] middlePixel;


        for (int offsetY = filterOffset; offsetY <
                sourceBitmap.getHeight() - filterOffset; offsetY++) {
            for (int offsetX = filterOffset; offsetX <
                    sourceBitmap.getWidth() - filterOffset; offsetX++) {
                byteOffset = offsetY *
                        sourceBitmap.getRowBytes() +
                        offsetX * 4;


                neighbourPixels.clear();


                for (int filterY = -filterOffset;
                     filterY <= filterOffset; filterY++) {
                    for (int filterX = -filterOffset;
                         filterX <= filterOffset; filterX++) {


                        calcOffset = byteOffset +
                                (filterX * 4) +
                                (filterY * sourceBitmap.getRowBytes());
                        neighbourPixels.add(pixelBuffer.getInt(calcOffset));

                    }
                }

                Collections.sort(neighbourPixels);
                middlePixel = ByteBuffer.allocate(4).putInt(neighbourPixels.get(filterOffset)).array();

                resultBuffer.array()[byteOffset] = middlePixel[0];
                resultBuffer.array()[byteOffset + 1] = middlePixel[1];
                resultBuffer.array()[byteOffset + 2] = middlePixel[2];
                resultBuffer.array()[byteOffset + 3] = middlePixel[3];
            }
            progress = (double) offsetY / top * 100;
            publishProgress((int) progress);
        }


        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap);

        resultBitmap.copyPixelsFromBuffer(resultBuffer);
        return resultBitmap;

    }
}