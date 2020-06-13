package education.artem.image_editor.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import education.artem.image_editor.BitmapHandle;
import education.artem.image_editor.OperationName;
import education.artem.image_editor.ProcessTask;
import education.artem.image_editor.filters.BilateralFilter;

public class BilateralFilterTask extends ProcessTask {
    public BilateralFilterTask(Context currContext, ImageView imageView, TextView status, ProgressBar progress, TextView exec) {
        super(currContext, imageView, status, progress, exec);
    }

    @Override
    protected Bitmap doInBackground(OperationName... params) {
        return bilateralFilter(BitmapHandle.getBitmapSource(), 6, 3);
    }

    public Bitmap bilateralFilter(Bitmap sourceBitmap, float distanceSigma, float intensitySigma) {
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
