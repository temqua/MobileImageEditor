package education.artem.contrastchangeandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ContrastChangeTask extends AsyncTask<OperationName, Integer, Bitmap> {

    private long start;
    private MainActivity activity;

    public ContrastChangeTask(MainActivity activity){
        this.activity = activity;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.start = System.currentTimeMillis();
        statusView.setText("Ведётся обработка изображения");
    }

    @Override
    protected Bitmap doInBackground(OperationName... params) {
        try {
            OperationName currentOperation = params[0];
            switch (currentOperation) {
                case EQUALIZE_CONTRAST:
                    return equalizeHistogram(BitmapSource.getBitmapSource());
                case FILTERING:
                    return equalizeHistogram(BitmapSource.getBitmapSource());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values){
        progressBar.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        long finish = System.currentTimeMillis();
        long timeConsumedMillis = finish - start;
        double timeConsumed = (double)timeConsumedMillis / 60000;
        NumberFormat formatter = new DecimalFormat("#0.000");
        statusView.setText("Изображение обработано. ");
        execTimeTextView.setText(getResources().getString(R.string.execution_time) + ": " + formatter.format(timeConsumed) + " мин");
        mImageView.setImageBitmap(result);
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
}
