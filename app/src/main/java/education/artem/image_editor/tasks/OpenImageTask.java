package education.artem.image_editor.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;

import education.artem.image_editor.BitmapHandle;
import education.artem.image_editor.R;

public class OpenImageTask extends AsyncTask<Uri, Void, Void> {


    protected WeakReference<ProgressDialog> progressDialogRef;
    protected WeakReference<Context> contextRef;
    protected WeakReference<ImageView> imageRef;

    public OpenImageTask(Context context, ImageView mImageView) {
        this.contextRef = new WeakReference<>(context);
        this.imageRef = new WeakReference<>(mImageView);
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    contextRef.get().getContentResolver().openFileDescriptor(uri, "r");
            assert parcelFileDescriptor != null;
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            options.inSampleSize = calculateInSampleSize(options, 1000,
                    1000);
            options.inJustDecodeBounds = false;
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            createErrorAlert(R.string.openImageError + ": " + e.getClass().getSimpleName() + e.getMessage());
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                if (e.getMessage() != null) {
                    Log.e(getClass().getSimpleName(), e.getClass().getSimpleName() + e.getMessage());
                    createErrorAlert(e.getClass().getSimpleName() + ": " + e.getMessage());
                }

            }
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth, int reqHeight) {
        // Реальные размеры изображения
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Вычисляем наибольший inSampleSize, который будет кратным двум
            // и оставит полученные размеры больше, чем требуемые
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public void createErrorAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(contextRef.get());
        builder.setTitle(R.string.error)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> dialog.cancel())
                .setIcon(R.drawable.ic_baseline_error_24);

        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    protected Void doInBackground(Uri... uris) {
        Uri selectedUri = uris[0];
        Bitmap bitmap = getBitmapFromUri(selectedUri);
        BitmapHandle.setBitmapSource(bitmap);
        BitmapHandle.setBitmapHandled(bitmap);
        return null;
    }

    @Override
    protected void onPreExecute() {
        progressDialogRef = new WeakReference<>(ProgressDialog.show(contextRef.get(),
                "ProgressDialog",
                "Opening image..."));
    }

    @Override
    protected void onPostExecute(Void result) {
        progressDialogRef.get().dismiss();
        imageRef.get().setImageBitmap(BitmapHandle.getBitmapSource());
    }
}
