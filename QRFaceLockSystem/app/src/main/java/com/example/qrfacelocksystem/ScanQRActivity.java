package com.example.qrfacelocksystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.util.List;

public class ScanQRActivity extends AppCompatActivity {

    CameraView cameraView;
    boolean isDetected = true;
    private ActionBar actionBar;
    private CameraSource.Builder mCameraSource;

    FirebaseVisionBarcodeDetector detector;
    FirebaseVisionBarcodeDetectorOptions options;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_q_r);

        setActionBar("Scan QR Code");

        setupCamera();

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

    private void scanQRcamera() {
        Dexter.withContext(this)
                .withPermission(String.valueOf(new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO}))
                .withListener(new PermissionListener() {
                                  @Override
                                  public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                        setupCamera();
                                  }

                                  @Override
                                  public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                      notification("You must accept permission");
                                  }

                                  @Override
                                  public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                                  }
                              }).check();

//        // To initialise the detector
//        FirebaseVisionBarcodeDetectorOptions options =
//                new FirebaseVisionBarcodeDetectorOptions.Builder()
//                        .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
//                        .build();
//
//        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
//                .getVisionBarcodeDetector(options);
    }

    private void setupCamera(){
        isDetected = !isDetected;

        cameraView = (CameraView) findViewById(R.id.camera_view);
        cameraView.setLifecycleOwner(this);
        cameraView.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull Frame frame) {
                processImage(getVisionImageFromFrame(frame));
            }
        });

        options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                .build();

        detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
    }

    private void processImage(FirebaseVisionImage image){
        if(!isDetected){
            detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                            processResult(firebaseVisionBarcodes);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            notification(""+ e.getMessage());
                        }
                    });

        }
    }

    private void processResult (List<FirebaseVisionBarcode> firebaseVisionBarcodes){
        if(firebaseVisionBarcodes.size() > 0)
        {
            isDetected = true;
            for (FirebaseVisionBarcode item: firebaseVisionBarcodes)
            {
                int value_type = item.getValueType();
                switch(value_type)
                {
                    case FirebaseVisionBarcode.TYPE_TEXT:
                    {
                        createDoor(item.getRawValue());
                    }
                    break;
                    default:
                }
            }
        }
    }

    private void createDoor(String rawValue){
        Intent intent = new Intent(ScanQRActivity.this, NewSetupActivity.class);
        intent.putExtra("DoorCode",rawValue);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private FirebaseVisionImage getVisionImageFromFrame(Frame frame){
            byte[] data = frame.getData();
            FirebaseVisionImageMetadata metaData = new FirebaseVisionImageMetadata.Builder()
                    .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                    .setHeight(frame.getSize().getHeight())
                    .setWidth(frame.getSize().getWidth())
                    .build();
        return FirebaseVisionImage.fromByteArray(data,metaData);
    }



    private void notification(String message) {
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();
    }

}