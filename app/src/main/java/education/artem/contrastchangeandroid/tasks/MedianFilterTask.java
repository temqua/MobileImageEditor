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

import education.artem.contrastchangeandroid.BitmapHandle;
import education.artem.contrastchangeandroid.Matrix;
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
                return MedianFilter(BitmapHandle.getBitmapSource(), 3, 0, false);
            case FILTER_5x5:
                return MedianFilter(BitmapHandle.getBitmapSource(), 5, 0, false);
            case FILTER_7x7:
                return MedianFilter(BitmapHandle.getBitmapSource(), 7, 0, false);
            case FILTER_9x9:
                return MedianFilter(BitmapHandle.getBitmapSource(), 9, 0, false);
            case FILTER_11x11:
                return MedianFilter(BitmapHandle.getBitmapSource(), 11, 0, false);
            case GAMMA_CORRECTION:
                return GammaCorrection(BitmapHandle.getBitmapSource());
            case BLUR:
                return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.BLUR, 9, 0, false);
            case GAUSSIAN_BLUR:
                return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.GAUSSIAN_BLUR, 256, 0, false);
            case SHARPEN:
                return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.SHARPEN, 1, 0, false);
            case EMBOSS:
                return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.EMBOSS, 1, 0, false);
            case IDENTITY:
                return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.IDENTITY, 1, 0, false);
        }
        return MedianFilter(BitmapHandle.getBitmapSource(), 3, 0, false);
    }

    public Bitmap MedianFilter(Bitmap sourceBitmap,
                                      int matrixSize,
                                      int bias,
                                      boolean grayscale) {

        ByteBuffer pixelBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
        ByteBuffer resultBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
        int height = sourceBitmap.getHeight();
        int width = sourceBitmap.getWidth();
        int stride = sourceBitmap.getRowBytes();
        sourceBitmap.copyPixelsToBuffer(pixelBuffer);

        if (grayscale) {
            float rgb;

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
        int calcOffset;


        int byteOffset;
        double progress;
        int top = height - filterOffset;
        List<Integer> neighbourPixels = new ArrayList<>();
        byte[] middlePixel;


        for (int offsetY = filterOffset; offsetY <
                height - filterOffset; offsetY++) {
            for (int offsetX = filterOffset; offsetX <
                    width - filterOffset; offsetX++) {
                byteOffset = offsetY *
                        stride +
                        offsetX * 4;


                neighbourPixels.clear();


                for (int filterY = -filterOffset;
                     filterY <= filterOffset; filterY++) {
                    for (int filterX = -filterOffset;
                         filterX <= filterOffset; filterX++) {


                        calcOffset = byteOffset +
                                (filterX * 4) +
                                (filterY * stride);
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

    public Bitmap ConvolutionFilter(Bitmap sourceBitmap,
                                    double[][] filterMatrix,
                                    double factor,
                                    int bias,
                                    boolean grayscale)
    {

        ByteBuffer pixelBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
        ByteBuffer resultBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
        int height = sourceBitmap.getHeight();
        int width = sourceBitmap.getWidth();
        int stride = sourceBitmap.getRowBytes();
        sourceBitmap.copyPixelsToBuffer(pixelBuffer);
        if (grayscale)
        {
            float rgb;

            for (int k = 0; k < pixelBuffer.array().length; k += 4)
            {
                rgb = pixelBuffer.array()[k] * 0.11f;
                rgb += pixelBuffer.array()[k + 1] * 0.59f;
                rgb += pixelBuffer.array()[k + 2] * 0.3f;

                pixelBuffer.array()[k] = (byte)rgb;
                pixelBuffer.array()[k + 1] = pixelBuffer.array()[k];
                pixelBuffer.array()[k + 2] = pixelBuffer.array()[k];
                pixelBuffer.array()[k + 3] = (byte)255;
            }
        }

        double blue;
        double green;
        double red;

        int filterWidth = filterMatrix[0].length;

        int filterOffset = (filterWidth-1) / 2;
        int calcOffset;

        int byteOffset;
        double progress;
        int top = height - filterOffset;

        for (int offsetY = filterOffset; offsetY <
                height - filterOffset; offsetY++)
        {
            for (int offsetX = filterOffset; offsetX <
                    width - filterOffset; offsetX++)
            {
                blue = 0;
                green = 0;
                red = 0;

                byteOffset = offsetY *
                        stride +
                        offsetX * 4;

                for (int filterY = -filterOffset;
                     filterY <= filterOffset; filterY++)
                {
                    for (int filterX = -filterOffset;
                         filterX <= filterOffset; filterX++)
                    {

                        calcOffset = byteOffset +
                                (filterX * 4) +
                                (filterY * stride);

                        blue += (double)(pixelBuffer.array()[calcOffset]) *
                                filterMatrix[filterY + filterOffset][filterX + filterOffset];

                        green += (double)(pixelBuffer.array()[calcOffset + 1]) *
                                filterMatrix[filterY + filterOffset][filterX + filterOffset];

                        red += (double)(pixelBuffer.array()[calcOffset + 2]) *
                                filterMatrix[filterY + filterOffset][filterX + filterOffset];
                    }
                }

                blue = factor * blue + bias;
                green = factor * green + bias;
                red = factor * red + bias;

                if (blue > 255)
                { blue = 255; }
                else if (blue < 0)
                { blue = 0; }

                if (green > 255)
                { green = 255; }
                else if (green < 0)
                { green = 0; }

                if (red > 255)
                { red = 255; }
                else if (red < 0)
                { red = 0; }

                resultBuffer.array()[byteOffset] = (byte)(blue);
                resultBuffer.array()[byteOffset + 1] = (byte)(green);
                resultBuffer.array()[byteOffset + 2] = (byte)(red);
                resultBuffer.array()[byteOffset + 3] = (byte)255;
            }
            progress = (double) offsetY / top * 100;
            publishProgress((int) progress);
        }


        Bitmap resultBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);

        resultBitmap.copyPixelsFromBuffer(resultBuffer);
        return resultBitmap;
    }

    public Bitmap GammaCorrection(Bitmap sourceBitmap) {
        int height = sourceBitmap.getHeight();
        int width = sourceBitmap.getWidth();
        double gamma = 0.95;
        double c = 1d;
        ByteBuffer pixelBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
        ByteBuffer resultBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
        int stride = sourceBitmap.getRowBytes();
        sourceBitmap.copyPixelsToBuffer(pixelBuffer);
        Bitmap resultBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);

        int current;
        int cChannels = 3;
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                current = y * stride + x * 4;
                for (int i = 0; i < cChannels; i++)
                {
                    double range = (double)pixelBuffer.array()[current + i] / 255;
                    double correction = c * Math.pow(range, gamma);
                    resultBuffer.array()[current + i] = (byte)(correction * 255);
                }
                resultBuffer.array()[current + 3] = (byte)255;
            }
            double progress = (double)y/width * 100;

            publishProgress((int)progress);
        }
        resultBitmap.copyPixelsFromBuffer(resultBuffer);
        return resultBitmap;
    }

    public Bitmap BilateralFilter(Bitmap sourceBitmap) {
        return sourceBitmap;
    }
}