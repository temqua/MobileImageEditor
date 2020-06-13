package education.artem.image_editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ProcessTask extends AsyncTask<OperationName, Integer, Bitmap> {

    private long start;
    private WeakReference<ImageView> mImageView;
    private WeakReference<TextView> statusViewRef;
    private WeakReference<ProgressBar> progressBarRef;
    private WeakReference<TextView> execTimeTextViewRef;
    private WeakReference<Context> contextRef;

    public ProcessTask(Context currContext, ImageView imageView, TextView status, ProgressBar progress, TextView exec) {
        this.mImageView = new WeakReference<>(imageView);
        this.statusViewRef = new WeakReference<>(status);
        this.progressBarRef = new WeakReference<>(progress);
        this.execTimeTextViewRef = new WeakReference<>(exec);
        this.contextRef = new WeakReference<>(currContext);
    }

    public Context getContext() {
        return contextRef.get();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.start = System.currentTimeMillis();

        statusViewRef.get().setText(contextRef.get().getResources().getString(R.string.processing_image));
    }

    @Override
    protected Bitmap doInBackground(OperationName... operationNames) {
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values){
        progressBarRef.get().setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        long finish = System.currentTimeMillis();
        long timeConsumedMillis = finish - start;
        double timeConsumed = (double) timeConsumedMillis / 60000;
        NumberFormat formatter = new DecimalFormat("#0.000");
        statusViewRef.get().setText(R.string.imageHandled);
        execTimeTextViewRef.get().setText(contextRef.get().getResources().getString(R.string.execution_time) + ": " + formatter.format(timeConsumed) + " мин");
        progressBarRef.get().setProgress(0);
        BitmapHandle.setBitmapHandled(result);
        mImageView.get().setImageBitmap(result);
    }


    protected void createInformationAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.contextRef.get());
        builder.setTitle(this.contextRef.get().getResources().getString(R.string.info))
                .setMessage(message)
                .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }
}
