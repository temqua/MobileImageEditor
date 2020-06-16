package education.artem.image_editor;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
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
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import education.artem.image_editor.fragments.BilateralPickerFragment;
import education.artem.image_editor.fragments.ContourFragment;
import education.artem.image_editor.fragments.ContrastFragment;
import education.artem.image_editor.fragments.FilterFragment;
import education.artem.image_editor.fragments.OpenImageDialogFragment;
import education.artem.image_editor.tasks.BilateralFilterTask;
import education.artem.image_editor.tasks.ContoursTask;
import education.artem.image_editor.tasks.ContrastChangeTask;
import education.artem.image_editor.tasks.GammaFilterTask;
import education.artem.image_editor.tasks.MedianFilterTask;
import education.artem.image_editor.tasks.OpenImageTask;


public class MainActivity extends AppCompatActivity {

    ImageView mImageView;
    TextView execTimeTextView;
    Bitmap bitmapSource;
    ProgressBar progressBar;
    TextView statusView;
    TextView cancelTaskTextView;
    double gamma;
    AsyncTask<OperationName, Integer, Bitmap> currentTask;
    BottomNavigationView bottomNavBar;
    private static final int READ_REQUEST_CODE = 1337;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private final String LOG_TAG = "ArtemImageEditor";


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

    public void initComponents() {
        mImageView = findViewById(R.id.imageView);
        execTimeTextView = findViewById(R.id.execTimeTextView);
        progressBar = findViewById(R.id.progressBar);
        statusView = findViewById(R.id.statusView);
        cancelTaskTextView = findViewById(R.id.cancelTaskView);
        bottomNavBar = findViewById(R.id.bottomNavBar);
        bottomNavBar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        cancelTaskTextView.setOnClickListener(view -> cancelCurrentTask());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        initComponents();
        loadFragment(new ContrastFragment());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Bitmap bitmapHandled = BitmapHandle.getBitmapHandled();
        if (mImageView != null && bitmapHandled != null) {
            mImageView.setImageBitmap(bitmapHandled);
        }
        if (progressBar != null) {
            int progress = savedInstanceState.getInt("handle_progress");
            progressBar.setProgress(progress);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (progressBar != null) {
            outState.putInt("handle_progress", progressBar.getProgress());
        }
    }

    public void createErrorAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.error)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> dialog.cancel())
                .setIcon(R.drawable.ic_baseline_error_24);

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void createInformationAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getResources().getString(R.string.info))
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> dialog.cancel())
                .setIcon(R.drawable.ic_baseline_info_24);
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
        boolean canExecute = true;

        if (BitmapHandle.getBitmapSource() == null) {
            OpenImageDialogFragment myDialogFragment = new OpenImageDialogFragment();
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            myDialogFragment.show(transaction, "open_image_dialog");
        } else {
            if (view.getId() == R.id.contours_analyze) {
                task = new ContoursTask(MainActivity.this, mImageView, statusView, progressBar, execTimeTextView, cancelTaskTextView);
            } else if (view.getId() == R.id.filtration) {
                switch (CurrentOperation.getCurrentOperationName()) {
                    case BILATERAL:
                        task = new BilateralFilterTask(MainActivity.this, mImageView, statusView, progressBar, execTimeTextView, cancelTaskTextView);
                        canExecute = false;
                        BilateralPickerFragment pickerFragment = new BilateralPickerFragment();
                        FragmentManager manager = getSupportFragmentManager();
                        FragmentTransaction transaction = manager.beginTransaction();
                        pickerFragment.show(transaction, "bilateral_picker");
                        break;
                    case GAMMA_CORRECTION:
                        gamma = 0.1;
                        task = new GammaFilterTask(MainActivity.this, mImageView, statusView, progressBar, execTimeTextView, cancelTaskTextView);
                        canExecute = false;
                        createNumberDialog("Gamma", "Choose gamma", new String[]{"0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0", "1.1", "1.2", "1.3"}, 0, 12, (dialog, which) -> {
                            CurrentOperation.getOperationParams().clear();
                            CurrentOperation.getOperationParams().put("gamma", String.valueOf(gamma));
                            dialog.dismiss();
                            executeCurrentTask();
                        }, (picker, oldVal, newVal) -> gamma = newVal);
                        break;
                    default:
                        CurrentOperation.getOperationParams().clear();
                        CurrentOperation.getOperationParams().put("gamma", "3");
                        task = new MedianFilterTask(MainActivity.this, mImageView, statusView, progressBar, execTimeTextView, cancelTaskTextView);
                        break;
                }
            } else if (view.getId() == R.id.contrast_change) {
                SeekBar contrastBar = findViewById(R.id.contrastBar);
                double threshold = contrastBar.getVisibility() == View.VISIBLE ? (double) contrastBar.getProgress() / 100 : 0;
                CurrentOperation.getOperationParams().clear();
                CurrentOperation.getOperationParams().put("threshold", String.valueOf(threshold));
                task = new ContrastChangeTask(MainActivity.this, mImageView, statusView, progressBar, execTimeTextView, cancelTaskTextView);
            }
            if (task != null) {
                this.currentTask = task;
            }
            if (canExecute) {
                executeCurrentTask();
            }
        }


    }

    public void executeCurrentTask() {
        currentTask.execute(CurrentOperation.getCurrentOperationName());
    }

    public void cancelCurrentTask() {
        this.currentTask.cancel(true);
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
                dumpImageMetaData(uri);
                OpenImageTask task = new OpenImageTask(MainActivity.this, mImageView);
                task.execute(uri);
            }
            // END_INCLUDE (parse_open_document_response)
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.lastOperationItem:
                lastOperation();
                break;
            case R.id.openFileItem:
                performFileSearch();
                break;
            case R.id.saveImageItem:
                saveImage();
                break;
            case R.id.closeItem:
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void lastOperation() {
        if (mImageView != null) {
            mImageView.setImageBitmap(BitmapHandle.getBitmapHandled());
        }
    }


    public void dumpImageMetaData(Uri uri) {

        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.

        try (Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null, null)) {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i(LOG_TAG, "Display Name: " + displayName);
                BitmapHandle.setFileName(displayName);
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
            BitmapHandle.setFileName(file.getName());
            BitmapHandle.setBitmapHandled(bitmapSource);
            mImageView.setImageBitmap(bitmapSource);
        }
    }

    private void saveImage() {
        if (BitmapHandle.getBitmapSource() == null) {
            OpenImageDialogFragment myDialogFragment = new OpenImageDialogFragment();
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            myDialogFragment.show(transaction, "open_image_dialog");
        } else {
            Bitmap editedBitmap = BitmapHandle.getBitmapHandled();
            String sourceFileName = BitmapHandle.getFileName();
            String sourceExt = getImageExtension(sourceFileName);
            String newFileName = getFileName(sourceFileName);
            File newFile = new File(Environment.
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + newFileName + "_modified." + sourceExt);
            Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;

            if (sourceExt != null) {
                switch (sourceExt) {
                    case "jpg":
                    case "jpeg":
                        format = Bitmap.CompressFormat.JPEG;
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
                if (e.getMessage() != null) {
                    Log.e(LOG_TAG, e.getMessage());
                }
                createInformationAlert("Could not save file. " + e.getMessage());
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        createInformationAlert("Could not save file. " + e.getMessage());
                        if (e.getMessage() != null) {
                            Log.e(LOG_TAG, e.getMessage());
                        }
                    }
                }
            }
        }

    }

    private String getImageExtension(String imageFileName) {
        int index = imageFileName.lastIndexOf(".");
        if (index > Math.max(imageFileName.lastIndexOf("/"), imageFileName.lastIndexOf("\\"))) {
            return imageFileName.substring(index + 1);
        }
        return null;
    }

    private String getFileName(String imageFileName) {
        int index = imageFileName.lastIndexOf(".");
        if (index > Math.max(imageFileName.lastIndexOf("/"), imageFileName.lastIndexOf("\\"))) {
            return imageFileName.substring(0, index);
        }
        return null;
    }

    protected void createNumberDialog(String title, String message, String[] displayedValues, int minValue, int maxValue, DialogInterface.OnClickListener positiveListener, NumberPicker.OnValueChangeListener pickerListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        NumberPicker numberPicker = new NumberPicker(MainActivity.this);
        numberPicker.setMinValue(minValue);
        numberPicker.setMaxValue(maxValue);
        if (displayedValues.length > 0) {
            numberPicker.setDisplayedValues(displayedValues);
        }
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setOnValueChangedListener(pickerListener);

        builder.setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .setPositiveButton(R.string.ok, positiveListener);
        builder.setView(numberPicker);
        AlertDialog alert = builder.create();
        alert.show();
    }

//    private class OpenImageTask extends AsyncTask<Uri, Void, Void> {
//
//
//        ProgressDialog progressDialog;
//
//        @Override
//        protected Void doInBackground(Uri... uris) {
//            Uri selectedUri = uris[0];
//            Bitmap bitmap = getBitmapFromUri(selectedUri);
//            BitmapHandle.setBitmapSource(bitmap);
//            BitmapHandle.setBitmapHandled(bitmap);
//            return null;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            progressDialog = ProgressDialog.show(MainActivity.this,
//                    "ProgressDialog",
//                    "Opening image...");
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            // execution of result of Long time consuming operation
//            progressDialog.dismiss();
//            mImageView.setImageBitmap(BitmapHandle.getBitmapSource());
//        }
//    }
}
