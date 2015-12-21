package com.perchlive.broadcast.sdk.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.IOException;

import com.perchlive.broadcast.sdk.R;
import com.perchlive.broadcast.sdk.av.AVRecorder;
import com.perchlive.broadcast.sdk.av.SessionConfig;
import com.perchlive.broadcast.sdk.view.GLCameraView;
import timber.log.Timber;

/**
 * A fragment used to record audio / video. Before showing your application
 * must have been granted camera & record audio permission.
 */
public class AVRecorderFragment extends Fragment implements View.OnClickListener {

    ImageButton  cameraFlipperButton;
    Button       recordButton;
    GLCameraView cameraPreivew;

    protected AVRecorder avRecorder;

    public AVRecorderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_avrecoder, container, false);
        recordButton = (Button) root.findViewById(R.id.recordButton);
        cameraFlipperButton = (ImageButton) root.findViewById(R.id.cameraFlipper);
        cameraPreivew = (GLCameraView) root.findViewById(R.id.cameraPreview);

        recordButton.setOnClickListener(this);
        cameraFlipperButton.setOnClickListener(this);

        try {
            avRecorder = new AVRecorder(new SessionConfig(getActivity().getFilesDir()));
            avRecorder.setPreviewDisplay((GLCameraView) root.findViewById(R.id.cameraPreview));
        } catch (IOException e) {
            Timber.e(e, "Failed to init AVRecorder");
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (avRecorder != null) {
            avRecorder.release();
        }
    }

    @Override
    public void onClick(View v) {
        if (avRecorder == null) return;

        if (v == cameraFlipperButton) {

            avRecorder.requestOtherCamera();

        } else if (v == recordButton) {

            if (avRecorder.isRecording()) {
                avRecorder.stopRecording();
                recordButton.setBackgroundResource(R.drawable.red_dot);
            } else {
                avRecorder.startRecording();
                recordButton.setBackgroundResource(R.drawable.red_square);
            }
        }
    }
}
