package com.vincentz.driver;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import java.io.IOException;

//import static com.vincentz.driver.Tools.ACT;
import static com.vincentz.driver.Tools.msg;

public class CameraFragment extends Fragment implements View.OnClickListener, SurfaceHolder.Callback {

    private String TAG = "Camera";
    private MediaRecorder recorder;
    private SurfaceHolder holder;
    private boolean recording = false;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View root = li.inflate(R.layout.fragment_camera, vg, false);

        initRecorder();

        SurfaceView cameraView = root.findViewById(R.id.sv_camera);
        holder = cameraView.getHolder();
        holder.addCallback(this);
//        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);

        return root;
    }

    private void initRecorder() {
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        } catch (Exception e) {
            Log.d(TAG, "initRecorder: " + e.getMessage());
            msg(getActivity(),"No audio or video device found");

        }


        CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);
        recorder.setOutputFile(Environment.getExternalStorageDirectory().getPath()
                + "/Videocapture.mp4");
        recorder.setMaxDuration(1000 * 60); // 60 seconds
        recorder.setMaxFileSize(1024 * 1024 * 20); // Approximately 20 MB
    }

    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            //ACT.finish();
        }
    }

    public void onClick(View v) {
        if (recording) {
            recorder.stop();
            recording = false;

            // Let's initRecorder so we can record again
            initRecorder();
            prepareRecorder();
        } else {
            recording = true;
            recorder.start();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        prepareRecorder();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        getActivity().finish();
    }
}
