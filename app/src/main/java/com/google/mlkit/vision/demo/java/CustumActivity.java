package com.google.mlkit.vision.demo.java;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.R;
import com.google.mlkit.vision.demo.VisionImageProcessor;
import com.google.mlkit.vision.demo.java.posedetector.MLKitVisionImage;
import com.google.mlkit.vision.demo.java.posedetector.PoseDetectorProcessor;
import com.google.mlkit.vision.demo.java.videoframe.AV_BitmapUtil;
import com.google.mlkit.vision.demo.java.videoframe.AV_FrameCapture;
import com.google.mlkit.vision.demo.preference.PreferenceUtils;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import com.google.mlkit.vision.demo.java.videoframe.AV_FrameCapture;

import static java.lang.Math.max;

public class CustumActivity extends LivePreviewActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener {

    private AV_FrameCapture mFrameCapture = null;
    boolean USE_MEDIA_META_DATA_RETRIEVER = false;

    private static final String POSE_DETECTION = "Pose Detection";
    private static final String TAG = "LivePreviewActivity";

    private Bitmap bitmap ;
    private ArrayList<Bitmap> bitmapArrayList;
    private MediaPlayer mediaPlayer;
    private VisionImageProcessor imageProcessor;
    private GraphicOverlay graphicOverlay;
    private VideoView videoView;
    private Button button, start_b;
    private ImageView preview;
    private Context CustumActivity;
    private AV_FrameCapture frameCapture;
    int i=1000;
    Intent myintent;
    Context VideoFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.custum_view);
        preview = findViewById(R.id.preview);


        button = (Button) findViewById(R.id.button) ;
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 101);
            }
        });
        start_b = (Button) findViewById(R.id.start_b) ;
        start_b.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                i+=1000;
                graphicOverlay = findViewById(R.id.graphic_overlay);
                graphicOverlay.clear();
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                myintent = getIntent();
                String filepath = "/storage/emulated/0/DCIM/Camera/20210709_144407.mp4";
                retriever.setDataSource(filepath);
                bitmapArrayList = new ArrayList<Bitmap>();
                bitmapArrayList.add(bitmap);
                bitmap = retriever.getFrameAtTime(i * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
                Bitmap A_8888Bitmap = ARGBBitmap(bitmap);
                createImageProcessor();
                DetectInImage(A_8888Bitmap);
                preview.setImageBitmap(A_8888Bitmap);
                retriever.release();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // 갤러리
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {

                frameCapture = new AV_FrameCapture();

                graphicOverlay = findViewById(R.id.graphic_overlay);
                graphicOverlay.clear();

                MediaController mc = new MediaController(this); // 비디오 컨트롤 가능하게(일시정지, 재시작 등)
                videoView.setMediaController(mc);

                PoseDetector poseDetector = null;

                Uri fileUri = data.getData();
                CustumActivity = getApplicationContext();
                InputImage image = null;
                try {
                    image = InputImage.fromFilePath(CustumActivity, fileUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String filepath = "/storage/emulated/0/DCIM/Camera/20210709_144407.mp4";
                captureFrame(filepath,100,1080,1920);
/***
                mediaPlayer = MediaPlayer.create(getBaseContext(), fileUri);
                int millisecond = mediaPlayer.getDuration();
                for (int i = 1000; i < millisecond; i += 1000) {
                    try {
                        bitmap = frameCapture.getFrameAtTime(i);
                        preview.setImageBitmap(bitmap);
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                        throw e;
                    }
                }
 ***/
                //videoView.setVideoPath(String.valueOf(fileUri));    // 선택한 비디오 경로 비디오뷰에 셋
                //videoView.start();  // 비디오뷰 시작

            }
        }
    }
    private void captureFrame(String VIDEO_FILE_PATH, long SNAPSHOT_DURATION_IN_MILLIS, int SNAPSHOT_WIDTH, int SNAPSHOT_HEIGHT) {

        // getFrameAtTimeByMMDR & getFrameAtTimeByFrameCapture function uses a micro sec 1millisecond = 1000 microseconds

        Bitmap bmp = USE_MEDIA_META_DATA_RETRIEVER ? getFrameAtTimeByMMDR(VIDEO_FILE_PATH, (SNAPSHOT_DURATION_IN_MILLIS * 1000))
                : getFrameAtTimeByFrameCapture(VIDEO_FILE_PATH, (SNAPSHOT_DURATION_IN_MILLIS * 1000), SNAPSHOT_WIDTH, SNAPSHOT_HEIGHT);

        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date());
        if (null != bmp) {
            AV_BitmapUtil.saveBitmap(bmp, String.format("/sdcard/read_%s.jpg", timeStamp));
        }

        if (mFrameCapture != null) {
            mFrameCapture.release();
        }
    }

    private Bitmap getFrameAtTimeByMMDR(String path, long time) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        Bitmap bmp = mmr.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST);
        mmr.release();
        return bmp;
    }

    private Bitmap getFrameAtTimeByFrameCapture(String path, long time, int snapshot_width, int snapshot_height) {
        mFrameCapture = new AV_FrameCapture();
        mFrameCapture.setDataSource(path);
        mFrameCapture.setTargetSize(snapshot_width, snapshot_height);
        mFrameCapture.init();
        return mFrameCapture.getFrameAtTime(time);
    }
    private void DetectInImage(Bitmap setimg) {
        try {
            Bitmap resizedBitmap;
                float scaleFactor = max((float) setimg.getWidth() / (float) 1536, (float) setimg.getHeight() / (float) 2048);
                resizedBitmap =Bitmap.createScaledBitmap(
                        setimg,
                        (int) (setimg.getWidth() / scaleFactor),
                        (int) (setimg.getHeight() / scaleFactor),
                        true);

            graphicOverlay.clear();
            preview.setImageBitmap(resizedBitmap);
            double width = resizedBitmap.getWidth()*1.5;
            double height = resizedBitmap.getHeight()*1.5;
            if (imageProcessor != null) {
                graphicOverlay.setImageSourceInfo(
                        (int)width, (int)height, /* isFlipped= */ false);
                imageProcessor.processBitmap(resizedBitmap, graphicOverlay);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void createImageProcessor() {

        PoseDetectorOptionsBase poseDetectorOptions =
                PreferenceUtils.getPoseDetectorOptionsForStillImage(this);
        boolean shouldShowInFrameLikelihood =
                PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodStillImage(this);
        boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this);
        boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this);
        boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this);
        imageProcessor =
                new PoseDetectorProcessor(
                        this,
                        poseDetectorOptions,
                        shouldShowInFrameLikelihood,
                        visualizeZ,
                        rescaleZ,
                        runClassification,
                        /* isStreamMode = */ false);


    }
    private Bitmap ARGBBitmap(Bitmap img) {
        return img.copy(Bitmap.Config.ARGB_8888,true);
    }


}