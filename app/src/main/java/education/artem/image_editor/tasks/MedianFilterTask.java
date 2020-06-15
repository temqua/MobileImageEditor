package education.artem.image_editor.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import education.artem.image_editor.BitmapHandle;
import education.artem.image_editor.CurrentOperation;
import education.artem.image_editor.Matrix;
import education.artem.image_editor.OperationName;
import education.artem.image_editor.ProcessTask;

public class MedianFilterTask extends ProcessTask {

    final int BITS = 256;

    public MedianFilterTask(Context currContext, ImageView imageView, TextView status, ProgressBar progress, TextView exec, TextView cancelView) {
        super(currContext, imageView, status, progress, exec, cancelView);
    }


    @Override
    protected Bitmap doInBackground(OperationName... params) {
        try {
            OperationName operationName = params[0];
            switch (operationName) {
                case MEDIAN_FILTER:
                    Map<String, String> filterParams = CurrentOperation.getOperationParams();
                    int matrixSize = 3;
                    if (filterParams.size() > 0) {
                        String matrixSizeValue = filterParams.get("matrixSize");
                        matrixSize = matrixSizeValue != null ? Integer.parseInt(matrixSizeValue) : 3;
                    }
                    return medianFilter(BitmapHandle.getBitmapSource(), matrixSize, 0, false);
                case BLUR:
                    return convolutionFilter(BitmapHandle.getBitmapSource(), Matrix.BLUR, 9, 0);
                case GAUSSIAN_BLUR:
                    return convolutionFilter(BitmapHandle.getBitmapSource(), Matrix.GAUSSIAN_BLUR_5x5, 256, 0);
                case SHARPEN:
                    return convolutionFilter(BitmapHandle.getBitmapSource(), Matrix.SHARPEN, 1, 0);
                case EMBOSS:
                    return convolutionFilter(BitmapHandle.getBitmapSource(), Matrix.EMBOSS, 1, 0);
                case IDENTITY:
                    return convolutionFilter(BitmapHandle.getBitmapSource(), Matrix.IDENTITY, 1, 0);
            }
            return medianFilter(BitmapHandle.getBitmapSource(), 3, 0, false);
        } catch (Exception e) {
            this.e = e;
            if (e.getMessage() != null) {
                Log.e(getClass().getName(), e.getMessage());
            }
            cancel(true);
        }
        return null;
    }

    private Bitmap medianFilter(Bitmap sourceBitmap,
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
                if (isCancelled()) {
                    return null;
                }

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


    private Bitmap convolutionFilter(Bitmap sourceBitmap, double[][] kernel, double divisor, double offset) {
        int imageWidth = sourceBitmap.getWidth();
        int imageHeight = sourceBitmap.getHeight();
        Bitmap resultBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        // Apply transformation (without borders)
        int tamMidKernel = (kernel.length - 1) / 2;
        int[][] pixels = new int[kernel.length][kernel.length];
        int newR, newG, newB, sumR, sumG, sumB;
        double a;
        for (int y = 0; y < imageHeight - (kernel.length - 1); y++) {
            for (int x = 0; x < imageWidth - (kernel.length - 1); x++) {
                // Get pixel matrix
                for (int i = 0; i < kernel.length; ++i) {
                    for (int j = 0; j < kernel.length; ++j) {
                        pixels[i][j] = sourceBitmap.getPixel(x + i, y + j);
                    }
                }
                // Get sum of RGB multiplied by kernel
                sumR = sumG = sumB = 0;
                for (int i = 0; i < kernel.length; ++i) {
                    for (int j = 0; j < kernel.length; ++j) {
                        // (from [0,1) to [0,BITS)) * kernel
                        int indexR = Color.red(pixels[i][j]);
                        int indexG = Color.green(pixels[i][j]);
                        int indexB = Color.blue(pixels[i][j]);
                        sumR += indexR * kernel[i][j];
                        sumG += indexG * kernel[i][j];
                        sumB += indexB * kernel[i][j];
                    }
                }
                // Get final RGB*
                newR = (int) (sumR / divisor + offset);
                newR = checkInRange(newR);
                newG = (int) (sumG / divisor + offset);
                newG = checkInRange(newG);
                newB = (int) (sumB / divisor + offset);
                newB = checkInRange(newB);
                resultBitmap.setPixel(x, y, Color.rgb(newR, newG, newB));
            }
            double progress = (double) y / imageHeight * 100;
            publishProgress((int) progress);
        }

        // Copy borders from original image
        for (int i = 0; i < sourceBitmap.getWidth(); i++) {
            for (int j = 0; j < tamMidKernel; j++) {
                // Top border
                resultBitmap.setPixel(i, j, sourceBitmap.getPixel(i, j));
                // Botton border
                resultBitmap.setPixel(i, sourceBitmap.getHeight() - 1 - j,
                        sourceBitmap.getPixel(i, sourceBitmap.getHeight() - 1 - j));
            }
        }
        for (int i = 0; i < sourceBitmap.getHeight(); i++) {
            for (int j = 0; j < tamMidKernel; j++) {
                // Right border
                resultBitmap.setPixel(sourceBitmap.getWidth() - 1 - j, i,
                        sourceBitmap.getPixel(sourceBitmap.getWidth() - 1 - j, i));

                // Left border
                resultBitmap.setPixel(j, i, resultBitmap.getPixel(j, i));
            }
        }


        return resultBitmap;
    }

    private int checkInRange(int pixel) {
        if (pixel < 0) {
            return 0;
        } else if (pixel >= BITS) {
            return BITS - 1;
        } else {
            return pixel;
        }
    }
}