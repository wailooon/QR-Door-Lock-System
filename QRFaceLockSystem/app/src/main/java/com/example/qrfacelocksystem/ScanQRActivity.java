package com.example.qrfacelocksystem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.google.android.gms.vision.CameraSource;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

public class ScanQRActivity extends AppCompatActivity {

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_q_r);

        setActionBar("Scan QR Code");

//        // To initialise the detector
//
//        FirebaseVisionBarcodeDetectorOptions options =
//                new FirebaseVisionBarcodeDetectorOptions.Builder()
//                        .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
//                        .build();
//
//        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
//                .getVisionBarcodeDetector(options);
//
//
//        // To connect the camera resource with the detector
//
//        mCameraSource = new CameraSource(this, barcodeOverlay);
//        mCameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
//
//        barcodeScanningProcessor = new BarcodeScanningProcessor(detector);
//        barcodeScanningProcessor.setBarcodeResultListener(getBarcodeResultListener());
//
//        mCameraSource.setMachineLearningFrameProcessor(barcodeScanningProcessor);
//
//        // // To create the FirebaseVisionImage
//
//
//        FirebaseVisionImageMetadata metadata =
//                new FirebaseVisionImageMetadata.Builder()
//                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
//                        .setWidth(frameMetadata.getWidth())
//                        .setHeight(frameMetadata.getHeight())
//                        .setRotation(frameMetadata.getRotation())
//                        .build();
//
//        Bitmap bitmap = BitmapUtils.getBitmap(data, frameMetadata);
//
//        FirebaseVisionImage firebaseVisionImage=FirebaseVisionImage.fromByteBuffer(data, metadata);

    }


    private void setActionBar(String heading) {
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorActionBar)));
        actionBar.setTitle(heading);
        actionBar.show();

    }

}