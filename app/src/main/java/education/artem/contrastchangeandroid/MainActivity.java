package education.artem.contrastchangeandroid;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

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
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
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

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        mImageView = findViewById(R.id.imageView);
        execTimeTextView = findViewById(R.id.execTimeTextView);
        progressBar = findViewById(R.id.progressBar);
        statusView = findViewById(R.id.statusView);
        bottomNavBar = findViewById(R.id.bottomNavBar);
        bottomNavBar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        loadFragment(new ContrastFragment());
        readImage();
    }

    public void createInformationAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getResources().getString(R.string.info))
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }


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

    public void changeImage(View view) {

        AsyncTask<OperationName, Integer, Bitmap> task = null;

        switch (view.getId()) {
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
        if (BitmapHandle.getBitmapSource() != null) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.open_file_item:
                performFileSearch();
                break;
            case R.id.saveImage:
                saveImage();
                break;
            case R.id.closeItem:
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getImageRealPath(ContentResolver contentResolver, Uri uri, String whereClause) {
        String ret = "";

        // Query the uri with condition.
        Cursor cursor = contentResolver.query(uri, null, whereClause, null, null);

        if (cursor != null) {
            boolean moveToFirst = cursor.moveToFirst();
            if (moveToFirst) {

                // Get columns name by uri type.
                String columnName = MediaStore.Images.Media.DATA;

                if (uri == MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Images.Media.DATA;
                } else if (uri == MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Audio.Media.DATA;
                } else if (uri == MediaStore.Video.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Video.Media.DATA;
                }

                // Get column index.
                int imageColumnIndex = cursor.getColumnIndex(columnName);

                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex);
            }
        }

        return ret;
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


    private void listDirectory(String path) {
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);
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
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "image001.JPG");
        if (file.exists()) {
            bitmapSource = BitmapFactory.decodeFile(file.getAbsolutePath());
            BitmapHandle.setBitmapSource(bitmapSource);
            BitmapHandle.setFileSource(file);
            BitmapHandle.setBitmapHandled(bitmapSource);
            mImageView.setImageBitmap(bitmapSource);
        }
    }

    private void saveImage() {
        Bitmap editedBitmap = BitmapHandle.getBitmapHandled();
        File sourceFile = BitmapHandle.getFileSource();
        String sourceExt = getImageExtension(sourceFile);
        String fileName = getFileName(sourceFile);

        File newFile = new File(Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + fileName + "_modified." + sourceExt);
        Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;

        if (sourceExt != null) {
            switch (sourceExt) {
                case "jpg":
                case "jpeg":
                    format = Bitmap.CompressFormat.JPEG;
                    break;
                case "png":
                    format = Bitmap.CompressFormat.PNG;
                    break;
                case "webp":
                    format = Bitmap.CompressFormat.WEBP;
                    break;
                default:
                    format = Bitmap.CompressFormat.PNG;
            }
        }

        FileOutputStream fos = null;
        try {
            if (newFile.createNewFile()) {
                fos = new FileOutputStream(newFile);
                editedBitmap.compress(format, 100, fos);
                Toast.makeText(this, "File " + newFile.getPath() + " successfully saved!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Could not create file " + newFile.getPath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private String getImageExtension(File image) {
        String imageFileName = image.getName();
        int index = imageFileName.lastIndexOf(".");
        if (index > Math.max(imageFileName.lastIndexOf("/"), imageFileName.lastIndexOf("\\"))) {
            return imageFileName.substring(index + 1);
        }
        return null;
    }

    private String getFileName(File file) {
        String imageFileName = file.getName();
        int index = imageFileName.lastIndexOf(".");
        if (index > Math.max(imageFileName.lastIndexOf("/"), imageFileName.lastIndexOf("\\"))) {
            return imageFileName.substring(0, index);
        }
        return null;
    }

    private class OpenImageTask extends AsyncTask<Uri, Void, Void> {


        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(Uri... uris) {
            Uri selectedUri = uris[0];
            Bitmap bitmap = getBitmapFromUri(selectedUri);
            BitmapHandle.setBitmapSource(bitmap);
            String fileName = getImageRealPath(getContentResolver(), selectedUri, null);
            BitmapHandle.setFileSource(new File(fileName));
            BitmapHandle.setBitmapHandled(bitmap);
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
            mImageView.setImageBitmap(BitmapHandle.getBitmapSource());
        }
    }
}
