package com.google.mlkit.vision.demo.java;
import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.VideoView;



import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.R;
import com.google.mlkit.vision.demo.VisionImageProcessor;
import com.google.mlkit.vision.demo.preference.PreferenceUtils;

import com.google.mlkit.vision.demo.java.posedetector.PoseDetectorProcessor;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.demo.java.VisionProcessorBase;
import com.google.mlkit.vision.demo.java.videoframe.AV_VideoActivity;

import static java.lang.Thread.sleep;

public class VideoFrame extends Activity {
    /**
     * Called when the activity is first created.
     */
    Button button2, button3;
    Context VideoFrame;
    VideoView videoView2;
    boolean isTouch = false;
    int totalTime;
    int currentTime;

    private int imageMaxWidth;
    private int imageMaxHeight;
    private AV_VideoActivity videocapture;

    private SaveVideo SaveV;
    private Bitmap bitmap ;
    private ArrayList<Bitmap> bitmapArrayList;
    private MediaPlayer mediaPlayer;
    private VisionImageProcessor imageProcessor;
    private GraphicOverlay graphicOverlay;
    File file;
    private ImageView preview;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d("Test_SC", String.valueOf("Test_AB"));
        setContentView(R.layout.video_frame);
        //videoView2 = findViewById(R.id.videoView2);
        button2 = (Button) findViewById(R.id.button2) ;
        preview = findViewById(R.id.preview);
        button2.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 101);
            }
        });


    }
    @Override
    public void onActivityResult(int requestcode, int resultcode, Intent data){
        super.onActivityResult(requestcode, resultcode, data);
        if(requestcode == 101 ) {
            graphicOverlay = findViewById(R.id.graphic_overlay);
            graphicOverlay.clear();

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            Uri videouri = data.getData();
            bitmapArrayList = new ArrayList<Bitmap>();
            VideoFrame = getApplicationContext();
            retriever.setDataSource(VideoFrame , videouri);
            mediaPlayer = MediaPlayer.create(getBaseContext(), videouri);
            int millisecond = mediaPlayer.getDuration();

            String filepath = "/storage/emulated/0/DCIM/Camera/20210709_144407.mp4";


            //Mediaimg.imageFromBitmap(bitmap);
            /***
            for(int i=7000; i<7999; i+=1000) {
                Bitmap A_8888Bitmap;
                bitmap = retriever.getFrameAtTime(1000 * i, MediaMetadataRetriever.OPTION_CLOSEST);
                A_8888Bitmap = ARGBBitmap(bitmap);
                //preview.setImageBitmap(A_8888Bitmap);
                createImageProcessor();
                DetectInImage(A_8888Bitmap);
            }
            ***/
            for(int i=10; i<1000; i+=10) {
                try {
                    Bitmap A_8888Bitmap;
                    bitmap = videocapture.getFrameAtTimeByFrameCapture(filepath, 10 * i, 1080, 1920);
                    A_8888Bitmap = ARGBBitmap(bitmap);
                    //preview.setImageBitmap(A_8888Bitmap);
                    createImageProcessor();
                    DetectInImage(A_8888Bitmap);
                }catch (Exception e){e.printStackTrace();}
            }

            //SaveV.testEncodeVideoToMp4();

            retriever.release();
        }
    }
    private void DetectInImage(Bitmap setimg) {
        try {
            // Clear the overlay first
            graphicOverlay.clear();
            preview.setImageBitmap(setimg);

            if (imageProcessor != null) {
                graphicOverlay.setImageSourceInfo(
                        setimg.getWidth(), setimg.getHeight(), /* isFlipped= */ false);
                imageProcessor.processBitmap(setimg, graphicOverlay);
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

