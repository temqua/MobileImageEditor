package education.artem.contrastchangeandroid.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.nio.ByteBuffer;

import education.artem.contrastchangeandroid.BitmapHandle;
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
                    return equalizeHistogram(BitmapHandle.getBitmapSource());
                case LINEAR_CONTRAST:
                    return adjustContrast(BitmapHandle.getBitmapSource(), 0.5);
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

    public Bitmap adjustContrast(Bitmap image, double threshold) {
        Bitmap newImage = image.copy(image.getConfig(), true);
        int height = image.getHeight();
        int width = image.getWidth();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (!(i == 0 || j == 0 || i == width - 1 || j == height - 1)) {
                    int R = 0;
                    int G = 0;
                    int B = 0;
                    int color = image.getPixel(i - 1, j - 1);
                    int indexR = Color.red(color);
                    int indexG = Color.green(color);
                    int indexB = Color.blue(color);
                    double red = (((double)indexR/255 - 0.5) * threshold + 0.5) * 255;
                    double green = (((double)indexG/255 - 0.5) * threshold + 0.5) * 255;
                    double blue = (((double)indexB/255 - 0.5) * threshold + 0.5) * 255;

                    indexR = (int) red;
                    indexR = indexR > 255 ? 255 : indexR;
                    indexR = indexR < 0 ? 0 : indexR;
                    indexG = (int) green;
                    indexG = indexG > 255 ? 255 : indexG;
                    indexG = indexG < 0 ? 0 : indexG;
                    indexB = (int) blue;
                    indexB = indexB > 255 ? 255 : indexB;
                    indexB = indexB < 0 ? 0 : indexB;
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


        double blue;
        double green;
        double red;
        Integer blueInt;
        Integer greenInt;
        Integer redInt;

        for (int k = 0; k + 4 < pixelBuffer.array().length; k += 4)
        {
            blue = ((((pixelBuffer.array()[k]/255) - 0.5) *
                    contrastLevel) + 0.5);


            green = ((((pixelBuffer.array()[k + 1]/255) - 0.5) *
                    contrastLevel) + 0.5);


            red = ((((pixelBuffer.array()[k + 2]/255) - 0.5) *
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


            blueInt = (int) blue;
            greenInt = (int) green;
            redInt = (int) red;
            pixelBuffer.array()[k] = blueInt.byteValue();
            pixelBuffer.array()[k + 1] = greenInt.byteValue();
            pixelBuffer.array()[k + 2] = redInt.byteValue();
        }

        newImage.copyPixelsFromBuffer(resultBuffer);
        return newImage;
    }

}
