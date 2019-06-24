package education.artem.contrastchangeandroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import education.artem.contrastchangeandroid.fragments.ContourFragment;
import education.artem.contrastchangeandroid.fragments.ContrastFragment;
import education.artem.contrastchangeandroid.fragments.FilterFragment;
import education.artem.contrastchangeandroid.fragments.OpenImageDialogFragment;

public class MainActivity extends AppCompatActivity {

    ImageView mImageView;
    Button changeImage;
    TextView execTimeTextView;
    Bitmap bitmapSource;
    ProgressBar progressBar;
    TextView statusView;
    OperationName currentOperation;
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
        readImage();
    }

    public void changeImage(){
        long start = System.currentTimeMillis();
        if (bitmapSource != null) {
            ChangeImageTask changeImageTask = new ChangeImageTask();
//            Bitmap newImage = changeImageTask.execute(bitmapSource);
            Bitmap newImage = ImageHandle.equalizeHistogram(bitmapSource);
            long finish = System.currentTimeMillis();
            long timeConsumedMillis = finish - start;
            double timeConsumed = (double)timeConsumedMillis / 60000;
            NumberFormat formatter = new DecimalFormat("#0.000");
            statusView.setText(R.string.execution_time + ": " + formatter.format(timeConsumed) + " мин");
            mImageView.setImageBitmap(newImage);
        } else {
            OpenImageDialogFragment myDialogFragment = new OpenImageDialogFragment();
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            myDialogFragment.show(transaction, "dialog");
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
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
        }
    };

    public void selectFragment(View view) {
        Fragment fragment;

        if(view == findViewById(R.id.contrast_item)) {
            fragment = new ContrastFragment();
        }else {
            fragment = new ContourFragment();
        }
        loadFragment(fragment);

    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.processingFragment, fragment);
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

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    this.getContentResolver().openFileDescriptor(uri, "r");
            assert parcelFileDescriptor != null;
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
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

    class ChangeImageTask extends AsyncTask<Bitmap, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            statusView.setText("Ведёт обработка изображения");
        }

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            try {
                changeImage();

            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            statusView.setText("Изображение обработано");
        }
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
//        Log.i(TAG, "Received an \"Activity Result\"");
        // BEGIN_INCLUDE (parse_open_document_response)
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
                bitmapSource = getBitmapFromUri(uri);
                mImageView.setImageBitmap(bitmapSource);
            }
            // END_INCLUDE (parse_open_document_response)
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
