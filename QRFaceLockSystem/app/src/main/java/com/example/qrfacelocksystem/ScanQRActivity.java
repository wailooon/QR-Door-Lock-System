package com.example.qrfacelocksystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

import java.util.ArrayList;
import java.util.List;

public class ScanQRActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference mDataRef;

    private FirebaseAuth mAuth;
    private FirebaseUser users;

    private ArrayList<String> _usersDetails; // Initialize all this stuff

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    CameraView cameraView;
    boolean isDetected = true;
    boolean isSame;
    private ActionBar actionBar;
    private CameraSource.Builder mCameraSource;
    private String door_lock_shared, doorIdFirebase;

    FirebaseVisionBarcodeDetector detector;
    FirebaseVisionBarcodeDetectorOptions options;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_q_r);

        mAuth = FirebaseAuth.getInstance();
        users = mAuth.getCurrentUser();

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
            for (final FirebaseVisionBarcode item: firebaseVisionBarcodes)
            {
                int value_type = item.getValueType();
                switch(value_type)
                {
                    case FirebaseVisionBarcode.TYPE_TEXT:
                    {
                        if(users == null){
                            mDataRef = FirebaseDatabase.getInstance().getReference("/IsUsedQRCode");

                            mDataRef.orderByChild("doorId").equalTo(item.getRawValue()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        notification("Please try other QR code, because this QR code has been used!");
                                        Intent intent = new Intent(ScanQRActivity.this, SignInActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        createDoor(item.getRawValue().toString());
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        }else{
                            mDataRef = FirebaseDatabase.getInstance().getReference("/IsUsedQRCode");

                            mDataRef.orderByChild("doorId").equalTo(item.getRawValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        notification("Please try other QR code, because this QR code has been used!");
                                        Intent intent = new Intent(ScanQRActivity.this, HomeActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(ScanQRActivity.this, AddNewDeviceActivity.class);
                                        intent.putExtra("NewDoorCode", item.getRawValue().toString());
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }

//                        mDataRef = FirebaseDatabase.getInstance().getReference("Users Details");
//
//                        mDataRef.orderByChild("doorId").equalTo(item.getRawValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                if(users == null){
//                                    if(dataSnapshot.exists()){
//                                        notification("Please try other QR code, because this QR code has been used!");
//                                        Intent intent = new Intent(ScanQRActivity.this, SignInActivity.class);
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        startActivity(intent);
//                                        finish();
//                                    }else{
//                                        createDoor(item.getRawValue().toString());
//                                    }
//
//                                }else {
//                                    mDataRef = FirebaseDatabase.getInstance().getReference("/Users Details/" + users.getUid());
//
//                                    mDataRef.orderByChild("doorId").equalTo(item.getRawValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                            if (dataSnapshot.exists()) {
//                                                notification("Please try other QR code, because this QR code has been used!");
//                                                Intent intent = new Intent(ScanQRActivity.this, HomeActivity.class);
//                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//                                                startActivity(intent);
//                                            } else {
//                                                Intent intent = new Intent(ScanQRActivity.this, AddNewDeviceActivity.class);
//                                                intent.putExtra("NewDoorCode", item.getRawValue().toString());
//                                                startActivity(intent);
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError error) {
//
//                                        }
//                                    });
//
//                                }
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });

                    }
                    break;
                    default:
                }
            }
        }
    }

    private int getUserDetailsPos(String userDetails) {
        return _usersDetails.indexOf(userDetails);
    }

    private void createDoor(String rawValue){
            Intent intent = new Intent(ScanQRActivity.this, NewSetupActivity.class);
            intent.putExtra("DoorCode",rawValue);
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