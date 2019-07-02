package education.artem.contrastchangeandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ProcessTask extends AsyncTask<OperationName, Integer, Bitmap> {

    private long start;
    private ImageView mImageView;
    private TextView statusView;
    private ProgressBar progressBar;
    private TextView execTimeTextView;
    private Context context;

    public ProcessTask(Context currContext, ImageView imageView, TextView status, ProgressBar progress, TextView exec){
        this.mImageView = imageView;
        this.statusView = status;
        this.progressBar = progress;
        this.execTimeTextView = exec;
        this.context = currContext;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.start = System.currentTimeMillis();
        statusView.setText(context.getResources().getString(R.string.processing_image));
    }

    @Override
    protected Bitmap doInBackground(OperationName... operationNames) {
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
        statusView.setText("Image successfully handled");
        execTimeTextView.setText(context.getResources().getString(R.string.execution_time) + ": " + formatter.format(timeConsumed) + " мин");
        mImageView.setImageBitmap(result);
    }
}
