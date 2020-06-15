package education.artem.image_editor.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.nio.ByteBuffer;

import education.artem.image_editor.BitmapHandle;
import education.artem.image_editor.Matrix;
import education.artem.image_editor.OperationName;
import education.artem.image_editor.ProcessTask;

public class ContoursTask extends ProcessTask {


    public ContoursTask(Context currContext, ImageView imageView, TextView status, ProgressBar progress, TextView exec, TextView cancelView) {
        super(currContext, imageView, status, progress, exec, cancelView);
    }

    @Override
    protected Bitmap doInBackground(OperationName... params) {

        try {
            OperationName operationName = params[0];

            switch (operationName) {
                case CONTOURS_SOBEL:
                    return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.Sobel3x3Horizontal, Matrix.Sobel3x3Vertical, 1, 0, false);
                case CONTOURS_SOBEL_GRAYSCALE:
                    return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.Sobel3x3Horizontal, Matrix.Sobel3x3Vertical, 1, 0, true);
                case CONTOURS_PREWITT:
                    return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.Prewitt3x3Horizontal, Matrix.Prewitt3x3Vertical, 1, 0, false);
                case CONTOURS_PREWITT_GRAYSCALE:
                    return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.Prewitt3x3Horizontal, Matrix.Prewitt3x3Vertical, 1, 0, true);
                case CONTOURS_LAPLASIAN_3X3:
                    return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.Laplacian3x3, 1, 0, false);
                case CONTOURS_LAPLASIAN_3X3_GRAYSCALE:
                    return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.Laplacian3x3, 1, 0, true);
                case CONTOURS_LAPLASIAN_5X5:
                    return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.Laplacian5x5, 1, 0, false);
                case CONTOURS_LAPLASIAN_5X5_GRAYSCALE:
                    return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.Laplacian5x5, 1, 0, true);
            }
            return ConvolutionFilter(BitmapHandle.getBitmapSource(), Matrix.Prewitt3x3Horizontal, Matrix.Prewitt3x3Vertical, 1, 0, false);
        } catch (Exception e) {
            this.e = e;
            if (e.getMessage() != null) {
                Log.e(getClass().getName(), e.getMessage());
            }
            cancel(true);
        }
        return null;
    }

    private Bitmap ConvolutionFilter(Bitmap sourceBitmap,
                                     double[][] filterMatrix,
                                     double factor,
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
                    width - filterOffset; offsetX++) {
                if (isCancelled()) {
                    return null;
                }

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

    private Bitmap ConvolutionFilter(Bitmap sourceBitmap,
                                     double[][] xFilterMatrix,
                                     double[][] yFilterMatrix,
                                     double factor,
                                     int bias,
                                     boolean grayscale) {
        ByteBuffer pixelBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
        ByteBuffer resultBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
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

        double blueX;
        double greenX;
        double redX;

        double blueY;
        double greenY;
        double redY;

        double blueTotal;
        double greenTotal;
        double redTotal;

        int filterOffset = 1;
        int calcOffset;

        int byteOffset;
        double progress;
        int top = sourceBitmap.getHeight() - filterOffset;
        for (int offsetY = filterOffset; offsetY <
                sourceBitmap.getHeight() - filterOffset; offsetY++)
        {
            for (int offsetX = filterOffset; offsetX <
                    sourceBitmap.getWidth() - filterOffset; offsetX++)
            {
                blueX = greenX = redX = 0;
                blueY = greenY = redY = 0;

                byteOffset = offsetY *
                        sourceBitmap.getRowBytes() +
                        offsetX * 4;

                for (int filterY = -filterOffset;
                     filterY <= filterOffset; filterY++)
                {
                    for (int filterX = -filterOffset;
                         filterX <= filterOffset; filterX++)
                    {
                        calcOffset = byteOffset +
                                (filterX * 4) +
                                (filterY *  sourceBitmap.getRowBytes());

                        blueX += (double)(pixelBuffer.array()[calcOffset]) * xFilterMatrix[filterY + filterOffset][filterX + filterOffset];

                        greenX += (double)(pixelBuffer.array()[calcOffset + 1]) *
                                xFilterMatrix[filterY + filterOffset][filterX + filterOffset];

                        redX += (double)(pixelBuffer.array()[calcOffset + 2]) *
                                xFilterMatrix[filterY + filterOffset][filterX + filterOffset];

                        blueY += (double)(pixelBuffer.array()[calcOffset]) *
                                yFilterMatrix[filterY + filterOffset][filterX + filterOffset];

                        greenY += (double)(pixelBuffer.array()[calcOffset + 1]) *
                                yFilterMatrix[filterY + filterOffset][filterX + filterOffset];

                        redY += (double)(pixelBuffer.array()[calcOffset + 2]) *
                                yFilterMatrix[filterY + filterOffset][filterX + filterOffset];
                    }
                }

                blueTotal = Math.sqrt((blueX * blueX) + (blueY * blueY));
                greenTotal = Math.sqrt((greenX * greenX) + (greenY * greenY));
                redTotal = Math.sqrt((redX * redX) + (redY * redY));

                if (blueTotal > 255)
                { blueTotal = 255; }
                else if (blueTotal < 0)
                { blueTotal = 0; }

                if (greenTotal > 255)
                { greenTotal = 255; }
                else if (greenTotal < 0)
                { greenTotal = 0; }

                if (redTotal > 255)
                { redTotal = 255; }
                else if (redTotal < 0)
                { redTotal = 0; }

                resultBuffer.array()[byteOffset] = (byte)(blueTotal);
                resultBuffer.array()[byteOffset + 1] = (byte)(greenTotal);
                resultBuffer.array()[byteOffset + 2] = (byte)(redTotal);
                resultBuffer.array()[byteOffset + 3] = (byte)255;
            }

            progress = (double) offsetY / top * 100;
            publishProgress((int) progress);
        }

        Bitmap resultBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);

        resultBitmap.copyPixelsFromBuffer(resultBuffer);
        return resultBitmap;
    }
}
