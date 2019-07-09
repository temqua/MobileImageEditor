package education.artem.contrastchangeandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import education.artem.contrastchangeandroid.fragments.ContourFragment;
import education.artem.contrastchangeandroid.fragments.ContrastFragment;
import education.artem.contrastchangeandroid.fragments.FilterFragment;
import education.artem.contrastchangeandroid.fragments.OpenImageDialogFragment;
import education.artem.contrastchangeandroid.tasks.ContoursTask;
import education.artem.contrastchangeandroid.tasks.ContrastChangeTask;
import education.artem.contrastchangeandroid.tasks.MedianFilterTask;

public class MainActivity extends AppCompatActivity {

    ImageView mImageView;
    TextView execTimeTextView;
    Bitmap bitmapSource;
    ProgressBar progressBar;
    TextView statusView;
    BottomNavigationView bottomNavBar;
    private static final int READ_REQUEST_CODE = 1337;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView =  findViewById(R.id.imageView);
        execTimeTextView = findViewById(R.id.execTimeTextView);
        progressBar =  findViewById(R.id.progressBar);
        statusView = findViewById(R.id.statusView);
        bottomNavBar =  findViewById(R.id.bottomNavBar);
        bottomNavBar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        loadFragment(new ContrastFragment());
        readImage();
    }

    public void createInformationAlert(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getResources().getString(R.string.info))
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createSelectDialog(ArrayAdapter<String> data) {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setTitle("Select Algorithm:");

        builderSingle.setNegativeButton("cancel", (dialog, which) -> dialog.dismiss());
        builderSingle.setAdapter(data, (dialog, which) -> {
            String strName = data.getItem(which);
            Toast.makeText(getApplicationContext(), strName, Toast.LENGTH_LONG).show();
        });
        builderSingle.show();
    }

    public void changeImage(View view){

        AsyncTask<OperationName, Integer, Bitmap> task = null;

        switch (view.getId()){
            case R.id.contours_analyze:
                task = new ContoursTask(MainActivity.this, mImageView, statusView, progressBar, execTimeTextView);
                break;
            case R.id.contrast_change:
                task = new ContrastChangeTask(MainActivity.this, mImageView, statusView, progressBar, execTimeTextView);
                break;
            case R.id.filtration:
                task = new MedianFilterTask(MainActivity.this, mImageView, statusView, progressBar, execTimeTextView);
                break;
        }
        if (BitmapSource.getBitmapSource() != null) {
            if (task != null) {
                task.execute(CurrentOperation.getCurrentOperation());
            }
        } else {
            OpenImageDialogFragment myDialogFragment = new OpenImageDialogFragment();
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            myDialogFragment.show(transaction, "dialog");
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.contours_item:
                        loadFragment(new ContourFragment());
                        return true;
                    case R.id.contrast_item:
                        loadFragment(new ContrastFragment());
                        return true;
                    case R.id.filter_item:
                        loadFragment(new FilterFragment());
                        return true;
                }
                return false;
            };


    private void loadFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }



    public void performFileSearch() {

        // BEGIN_INCLUDE (use_open_document_intent)
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a file (as opposed to a list
        // of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers, it would be
        // "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
        // END_INCLUDE (use_open_document_intent)
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.open_file_item){
            performFileSearch();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                OpenImageTask task = new OpenImageTask();
                task.execute(uri);
            }
            // END_INCLUDE (parse_open_document_response)
        }
    }



    private class OpenImageTask extends AsyncTask<Uri, Void, Void> {


        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(Uri... uris) {
            BitmapSource.setBitmapSource(getBitmapFromUri(uris[0]));
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "ProgressDialog",
                    "Opening image...");
        }

        @Override
        protected void onPostExecute(Void result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
            mImageView.setImageBitmap(BitmapSource.getBitmapSource());
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

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
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
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void listDirectory(String path){
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (File file : files) {
            Log.d("Files", "FileName:" + file.getName());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    private void readImage() {

        File file = new File(Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"image001.JPG");
        if (file.exists()){
            bitmapSource = BitmapFactory.decodeFile(file.getAbsolutePath());
            mImageView.setImageBitmap(bitmapSource);
        }
    }
}
