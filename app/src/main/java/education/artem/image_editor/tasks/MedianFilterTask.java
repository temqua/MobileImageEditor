package education.artem.image_editor.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import education.artem.image_editor.BitmapHandle;
import education.artem.image_editor.Matrix;
import education.artem.image_editor.OperationName;
import education.artem.image_editor.ProcessTask;
import education.artem.image_editor.filters.BilateralFilter;

public class MedianFilterTask extends ProcessTask {

    final int BITS = 256;
    private double gamma;


    public MedianFilterTask(Context currContext, ImageView imageView, TextView status, ProgressBar progress, TextView exec, double gamma) {
        super(currContext, imageView, status, progress, exec);
        this.gamma = gamma;
    }

    @Override
    protected Bitmap doInBackground(OperationName... params) {
        switch (params[0]) {
            case FILTER_3x3:
                return medianFilter(BitmapHandle.getBitmapSource(), 3, 0, false);
            case FILTER_5x5:
                return medianFilter(BitmapHandle.getBitmapSource(), 5, 0, false);
            case FILTER_7x7:
                return medianFilter(BitmapHandle.getBitmapSource(), 7, 0, false);
            case FILTER_9x9:
                return medianFilter(BitmapHandle.getBitmapSource(), 9, 0, false);
            case FILTER_11x11:
                return medianFilter(BitmapHandle.getBitmapSource(), 11, 0, false);
            case GAMMA_CORRECTION:
                return gammaCorrection(BitmapHandle.getBitmapSource(), gamma);
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
            case BILATERAL:
                return BilateralFilter(BitmapHandle.getBitmapSource(), 6, 3);
        }
        return medianFilter(BitmapHandle.getBitmapSource(), 3, 0, false);
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

    public Bitmap gammaCorrection(Bitmap image, double gamma) {
        Bitmap newImage = image.copy(image.getConfig(), true);
        int height = image.getHeight();
        int width = image.getWidth();
        double c = 1d;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
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
                resultBuffer.array()[current + 3] = (byte) 255;
            }
            double progress = (double) y / height * 100;

            publishProgress((int) progress);
        }
        resultBitmap.copyPixelsFromBuffer(resultBuffer);
        return resultBitmap;
    }

    public Bitmap BilateralFilter(Bitmap sourceBitmap, float distanceSigma, float intensitySigma) {
        BilateralFilter bilateralFilter = new BilateralFilter(distanceSigma, intensitySigma);
        int kernelSize = bilateralFilter.getKernelSize();
        float[][] gaussianKernelMatrix = bilateralFilter.getGaussianKernelMatrix();
        float[] intensityVector = bilateralFilter.getIntensityVector();
        int imageWidth = sourceBitmap.getWidth();
        int imageHeight = sourceBitmap.getHeight();
        Bitmap resultBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                float numeratorSumR = 0;
                float numeratorSumG = 0;
                float numeratorSumB = 0;
                float denominatorSum = 0;

                // It needs to calculate number of pixel that fits to kernel.
                int halfKernelSize = (int) Math.floor(kernelSize / 2);
                int kernelCenterIntensity = sourceBitmap.getPixel(x, y);

                // Go around the kernel if it is allowed by border of image.
                for (int i = x - halfKernelSize; i < x + halfKernelSize; i++) {
                    for (int j = y - halfKernelSize; j < y + halfKernelSize; j++) {
                        if (i >= 0 && j >= 0 && i < imageWidth && j < imageHeight) {
                            float kernelPositionWeight;
                            int kernelPositionIntensity = sourceBitmap.getPixel(i, j);

                            // Translate kernel image coordinates into local gaussianKernelMatrix coordinates and
                            // calculate weight of current kernel position.
                            kernelPositionWeight = gaussianKernelMatrix[x - i + halfKernelSize][y - j + halfKernelSize] *
                                    intensityVector[bilateralFilter.getIntensityDifference(kernelCenterIntensity, kernelPositionIntensity)];

                            // Process each color component separately.
                            // It is necessary to make filter work with color images.
                            numeratorSumR += kernelPositionWeight * ((kernelPositionIntensity >> 16) & 0xFF);
                            numeratorSumG += kernelPositionWeight * ((kernelPositionIntensity >> 8) & 0xFF);
                            numeratorSumB += kernelPositionWeight * (kernelPositionIntensity & 0xFF);
                            denominatorSum += kernelPositionWeight;
                        }
                    }
                }

                // Normalization by division and combination bit color value from separate components
                // into compound 32-bits value, delete an alpha channel. Then set new value.
                int color = 0xFF000000 | (((int) (numeratorSumR / denominatorSum) & 0xFF) << 16) |
                        (((int) (numeratorSumG / denominatorSum) & 0xFF) << 8) |
                        ((int) (numeratorSumB / denominatorSum) & 0xFF);

                resultBitmap.setPixel(x, y, color);
            }
            double progress = (double) x / imageWidth * 100;
            publishProgress((int) progress);
        }
        return resultBitmap;
    }
}