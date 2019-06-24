package education.artem.contrastchangeandroid;

import android.graphics.Color;
import android.graphics.Bitmap;


public class ImageHandle {

    public static Histogram buildImageHistogram(Bitmap image) {
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

    public static Bitmap equalizeHistogram(Bitmap image) {
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
        }
        return newImage;
    }
}
