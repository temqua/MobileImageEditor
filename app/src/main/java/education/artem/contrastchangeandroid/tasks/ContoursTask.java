package education.artem.contrastchangeandroid.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.nio.Buffer;
import java.nio.ByteBuffer;

import education.artem.contrastchangeandroid.BitmapSource;
import education.artem.contrastchangeandroid.Matrix;
import education.artem.contrastchangeandroid.OperationName;
import education.artem.contrastchangeandroid.ProcessTask;

public class ContoursTask extends ProcessTask {
    public ContoursTask(Context currContext, ImageView imageView, TextView status, ProgressBar progress, TextView exec) {
        super(currContext, imageView, status, progress, exec);
    }

    @Override
    protected Bitmap doInBackground(OperationName... params) {
        return ConvolutionFilter(BitmapSource.getBitmapSource(), Matrix.Prewitt3x3Horizontal, Matrix.Prewitt3x3Vertical, 1, 0, true);
    }

    public Bitmap ConvolutionFilter(Bitmap sourceBitmap,
                                           double[][] xFilterMatrix,
                                           double[][] yFilterMatrix,
                                           double factor,
                                           int bias,
                                           boolean grayscale)
    {
//        BitmapData sourceData = sourceBitmap.LockBits(new Rectangle(0, 0,
//                        sourceBitmap.Width, sourceBitmap.Height),
//                ImageLockMode.ReadOnly,
//                PixelFormat.Format32bppArgb);
        ByteBuffer pixelBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
        ByteBuffer resultBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
//        Buffer pixelBuffer = new byte[sourceBitmap.getWidth() * sourceBitmap.getHeight()];
//        byte[] resultBuffer = new byte[sourceBitmap.getWidth() * sourceBitmap.getHeight()];

//        Marshal.Copy(sourceData.Scan0, pixelBuffer, 0, pixelBuffer.Length);
//        sourceBitmap.UnlockBits(sourceData);
        sourceBitmap.copyPixelsToBuffer(pixelBuffer);
        if (grayscale == true)
        {
            float rgb = 0;

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

        double blueX = 0.0;
        double greenX = 0.0;
        double redX = 0.0;

        double blueY = 0.0;
        double greenY = 0.0;
        double redY = 0.0;

        double blueTotal = 0.0;
        double greenTotal = 0.0;
        double redTotal = 0.0;

        int filterOffset = 1;
        int calcOffset = 0;

        int byteOffset = 0;

        for (int offsetY = filterOffset; offsetY <
                sourceBitmap.getHeight() - filterOffset; offsetY++)
        {
            for (int offsetX = filterOffset; offsetX <
                    sourceBitmap.getWidth() - filterOffset; offsetX++)
            {
                blueX = greenX = redX = 0;
                blueY = greenY = redY = 0;

                blueTotal = greenTotal = redTotal = 0.0;

                byteOffset = offsetY *
                        sourceBitmap.getWidth() +
                        offsetX * 4;

                for (int filterY = -filterOffset;
                     filterY <= filterOffset; filterY++)
                {
                    for (int filterX = -filterOffset;
                         filterX <= filterOffset; filterX++)
                    {
                        calcOffset = byteOffset +
                                (filterX * 4) +
                                (filterY *  sourceBitmap.getWidth());

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
        }

        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap);

//        BitmapData resultData = resultBitmap.LockBits(new Rectangle(0, 0,
//                        resultBitmap.Width, resultBitmap.Height),
//                ImageLockMode.WriteOnly,
//                PixelFormat.Format32bppArgb);

//        Marshal.Copy(resultBuffer, 0, resultData.Scan0, resultBuffer.Length);
//        resultBitmap.UnlockBits(resultData);
        resultBitmap.copyPixelsFromBuffer(resultBuffer);
        return resultBitmap;
    }
}
