package education.artem.image_editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ProcessTask extends AsyncTask<OperationName, Integer, Bitmap> {

    protected long start;
    protected WeakReference<ImageView> mImageView;
    protected WeakReference<TextView> statusViewRef;
    protected WeakReference<ProgressBar> progressBarRef;
    protected WeakReference<TextView> execTimeTextViewRef;
    protected WeakReference<Context> contextRef;
    protected WeakReference<TextView> cancelViewRef;
    protected Exception e;

    public ProcessTask(Context currContext, ImageView imageView, TextView status, ProgressBar progress, TextView exec, TextView cancelView) {
        this.mImageView = new WeakReference<>(imageView);
        this.statusViewRef = new WeakReference<>(status);
        this.progressBarRef = new WeakReference<>(progress);
        this.execTimeTextViewRef = new WeakReference<>(exec);
        this.cancelViewRef = new WeakReference<>(cancelView);
        this.contextRef = new WeakReference<>(currContext);
    }

    public Context getContext() {
        return contextRef.get();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.start = System.currentTimeMillis();
        cancelViewRef.get().setVisibility(View.VISIBLE);
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
        reset();
        BitmapHandle.setBitmapHandled(result);
        mImageView.get().setImageBitmap(result);
    }

    protected void reset() {
        progressBarRef.get().setProgress(0);
        cancelViewRef.get().setVisibility(View.INVISIBLE);
    }


    protected void createInformationAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.contextRef.get());
        builder.setTitle(this.contextRef.get().getResources().getString(R.string.info))
                .setMessage(message)
                .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_baseline_info_24);
        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void createErrorAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.contextRef.get());
        builder.setTitle(this.contextRef.get().getResources().getString(R.string.error))
                .setMessage(message)
                .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_baseline_error_24);
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        reset();
        statusViewRef.get().setText(R.string.handle_cancelled);
        if (e != null) {
            createErrorAlert(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
//        Toast.makeText(this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
