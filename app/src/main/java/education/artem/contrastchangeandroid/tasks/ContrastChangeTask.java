package education.artem.contrastchangeandroid.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import education.artem.contrastchangeandroid.BitmapSource;
import education.artem.contrastchangeandroid.Histogram;
import education.artem.contrastchangeandroid.OperationName;
import education.artem.contrastchangeandroid.ProcessTask;

public class ContrastChangeTask extends ProcessTask {


    public ContrastChangeTask(Context currContext, ImageView imageView, TextView status, ProgressBar progress, TextView exec){
        super(currContext, imageView, status, progress, exec);
    }


    @Override
    protected Bitmap doInBackground(OperationName... params) {
        try {
            OperationName currentOperation = params[0];
            switch (currentOperation) {
                case EQUALIZE_CONTRAST:
                    return equalizeHistogram(BitmapSource.getBitmapSource());
                case LINEAR_CONTRAST:
                    return linearContrast(BitmapSource.getBitmapSource(), 50);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Histogram buildImageHistogram(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int size = 256;
        Histogram hist = new Histogram(size);
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                if (!(i == 0 || j == 0 || i == width - 1 || j == height - 1)) {
                    int color = image.getPixel(i - 1, j - 1);
                    int R = Color.red(color);
                    hist.getRed()[R] += 1;
                    int B = Color.blue(color);
                    hist.getBlue()[B] += 1;
                    int G = Color.green(color);
                    hist.getGreen()[G] += 1;
                }
            }

        }

        return hist;
    }

    public  Bitmap equalizeHistogram(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int size = 256;
        Bitmap newImage = image.copy(image.getConfig(), true);
        Histogram hist = buildImageHistogram(image);
        for (int i = 0; i < size; i++)
        {
            hist.getRed()[i] /= (width * height);
            hist.getGreen()[i] /= (width * height);
            hist.getBlue()[i] /= (width * height);
        }
        for (int i = 1; i < size; i++)
        {
            hist.getRed()[i] = hist.getRed()[i - 1] + hist.getRed()[i];
            hist.getGreen()[i] = hist.getGreen()[i - 1] + hist.getGreen()[i];
            hist.getBlue()[i] = hist.getBlue()[i - 1] + hist.getBlue()[i];
        }

        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                if (!(i == 0 || j == 0 || i == width - 1 || j == height - 1)) {
                    int color = image.getPixel(i - 1, j - 1);
                    int indexR = Color.red(color);
                    int indexG = Color.green(color);
                    int indexB = Color.blue(color);
                    int red = (int)(hist.getRed()[indexR] * size);
                    int green = (int)(hist.getGreen()[indexG] * size);
                    int blue = (int)(hist.getBlue()[indexB] * size);
                    newImage.setPixel(i, j, Color.rgb(red, green, blue));
                }
                else {
                    int color = image.getPixel(i, j);
                    int indexR = Color.red(color);
                    int indexG = Color.green(color);
                    int indexB = Color.blue(color);
                    newImage.setPixel(i, j, Color.rgb(indexR, indexG, indexB));

                }
            }
            double progress = (double)i/width * 100;

            publishProgress((int)progress);
        }
        return newImage;
    }

    public Bitmap linearContrast(Bitmap image, int threshold){
        ByteBuffer pixelBuffer = ByteBuffer.allocate(image.getByteCount());
        ByteBuffer resultBuffer = ByteBuffer.allocate(image.getByteCount());
        image.copyPixelsToBuffer(pixelBuffer);
        Bitmap newImage = image.copy(image.getConfig(), true);
        double contrastLevel = Math.pow((100.0 + threshold) / 100.0, 2);


        double blue = 0;
        double green = 0;
        double red = 0;


        for (int k = 0; k + 4 < pixelBuffer.array().length; k += 4)
        {
            blue = ((((pixelBuffer.array()[k]) - 0.5) *
                    contrastLevel) + 0.5);


            green = ((((pixelBuffer.array()[k + 1]) - 0.5) *
                    contrastLevel) + 0.5);


            red = ((((pixelBuffer.array()[k + 2]) - 0.5) *
                    contrastLevel) + 0.5);


            if  (blue > 255)
            { blue = 255; }
            else if  (blue < 0)
            { blue = 0; }


            if (green > 255)
            { green = 255; }
            else if (green < 0)
            { green = 0; }


            if (red > 255)
            { red = 255; }
            else if (red < 0)
            { red = 0; }


            pixelBuffer.array()[k] = (byte)blue;
            pixelBuffer.array()[k + 1] = (byte)green;
            pixelBuffer.array()[k + 2] = (byte)red;
        }

        newImage.copyPixelsFromBuffer(resultBuffer);
        return newImage;
    }

}
