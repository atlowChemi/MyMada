package atlow.chemi.mymada;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import atlow.chemi.scanner.BarcodeScanningProcessor;
import atlow.chemi.scanner.BarcodeScanningProcessor.BarcodeResultListener;
import atlow.chemi.scanner.CameraSource;
import atlow.chemi.scanner.CameraSourcePreview;
import atlow.chemi.scanner.FrameMetadata;
import atlow.chemi.scanner.GraphicOverlay;

public class BarcodeScannerActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        CompoundButton.OnCheckedChangeListener  {
    private static final String TAG = "LivePreviewActivity";
    private static final int PERMISSION_REQUESTS = 1;
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private String[] permissionsRequired = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.barcode_scanner);
        this.setFinishOnTouchOutside(false);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.x = -20;
        params.height = height/2;
        params.width = (height/2)-100;
        params.y = -10;

        this.getWindow().setAttributes(params);
        preview = findViewById(R.id.firePreview);
        graphicOverlay = findViewById(R.id.fireFaceOverlay);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }
        ToggleButton facingSwitch = findViewById(R.id.facingSwitch);
        facingSwitch.setOnCheckedChangeListener(this);
        // Hide the toggle button if there is only 1 camera
        if (Camera.getNumberOfCameras() == 1) {
            facingSwitch.setVisibility(View.GONE);
        }
        if (allPermissionsGranted()) {
            Log.e(TAG, "CREATING CAMERA");
            createCameraSource();
        } else {
            CreateCameraPermissionDialog();
        }
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        startCameraSource();
    }

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }
        try {
            Log.e(TAG, "Using Barcode Detector Processor");
            FirebaseVisionBarcodeDetectorOptions options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS).build();
            FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
            BarcodeScanningProcessor barcodeScanningProcessor = new BarcodeScanningProcessor(detector);
            barcodeScanningProcessor.setBarcodeResultListener(getBarcodeResultListener());
            cameraSource.setMachineLearningFrameProcessor(barcodeScanningProcessor);
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + e.getMessage());
            Toast.makeText(getApplicationContext(),"Can not create image processor: " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
    }
    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }
    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
    private boolean allPermissionsGranted() {
        for (String permission : permissionsRequired) {
            if (isPermissionNotGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }
    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        SharedPreferences permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
        for (String permission : permissionsRequired) {
            if (isPermissionNotGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(BarcodeScannerActivity.this, permissionsRequired[0])) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeScannerActivity.this, R.style.AlertDialogStyle);
                builder.setTitle(getApplicationContext().getResources().getString(R.string.permTitle));
                builder.setMessage(getApplicationContext().getResources().getString(R.string.permDesCamera));
                builder.setPositiveButton(getApplicationContext().getResources().getString(R.string.permYes), (dialog, which) -> {
                    dialog.cancel();
                    ActivityCompat.requestPermissions(BarcodeScannerActivity.this, permissionsRequired, PERMISSION_REQUESTS);
                });
                builder.setNegativeButton(getApplicationContext().getResources().getString(R.string.ret), (dialog, which) -> dialog.cancel());
                builder.show();
            }
            else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeScannerActivity.this, R.style.AlertDialogStyle);
                builder.setTitle(getApplicationContext().getResources().getString(R.string.permTitle));
                builder.setMessage(getApplicationContext().getResources().getString(R.string.permDesCamera));
                builder.setPositiveButton(getApplicationContext().getResources().getString(R.string.permYes), (dialog, which) -> {
                    dialog.cancel();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, PERMISSION_REQUESTS);
                });
                builder.setNegativeButton(getApplicationContext().getResources().getString(R.string.ret), (dialog, which) -> dialog.cancel());
                builder.show();
            }
            else {
                //just request the permission
                ActivityCompat.requestPermissions(BarcodeScannerActivity.this, permissionsRequired, PERMISSION_REQUESTS);
            }
        }
    }
    private void CreateCameraPermissionDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            builder.setMessage(getResources().getString(R.string.permDesCamera)).setCancelable(false)
                    .setTitle(getString(R.string.permTitle))
                    .setPositiveButton(getResources().getString(R.string.permYes), (dialog, id) -> getRuntimePermissions())
                    .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
            final AlertDialog alert = builder.create();
            alert.setOnShowListener(dialogInterface -> {
                TextView textView = alert.findViewById(android.R.id.message);
                if (textView != null)
                    textView.setTypeface(myTypeface);
                textView = alert.findViewById(android.R.id.title);
                if (textView != null)
                    textView.setTypeface(myTypeface);
            });
            alert.show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private static boolean isPermissionNotGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return false;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return true;
    }
    public BarcodeResultListener getBarcodeResultListener() {
        return new BarcodeResultListener() {
            @Override
            public void onSuccess(@Nullable Bitmap originalCameraImage, @NonNull List<FirebaseVisionBarcode> barcodes, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {
                Log.d(TAG, "onSuccess: " + barcodes.size());
                if (barcodes.size() > 0) {
                    for (FirebaseVisionBarcode barCode : barcodes) {
                        Log.d(TAG, "onSuccess: " + barCode.getRawValue());
                        Log.d(TAG, "onSuccess: " + barCode.getFormat());
                        Log.d(TAG, "onSuccess: " + barCode.getValueType());
                        String barCodeDisplayValue = barCode.getDisplayValue();
                        if (barCodeDisplayValue != null) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("barcodeR",barCodeDisplayValue);
                            setResult(Activity.RESULT_OK,returnIntent);
                            finish();
                            Log.d(TAG, "barcode value is: " + barCodeDisplayValue);
                        }
                    }
                    preview.release();
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "error reading the code: " + e.getMessage());
            }
        };
    }
    private void di(String err)   {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setTitle(getResources().getString(R.string.err)).setMessage(err).setCancelable(false).setNegativeButton(getResources().getString(R.string.ok), (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }
}
