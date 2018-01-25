package com.lgd.rtspplayerdemo2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lgd.sdtylib.GiraffePlayer;
import com.lgd.sdtylib.Option;
import com.lgd.sdtylib.VideoInfo;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mVideoUrl;
    /**
     * startvideo
     */
    private Button mVideoStart;
    /**
     * LocalCamera
     */
    private Button mCameraProview;
    /**
     * SuperCamera
     */
    private Button mSupercamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void init(String murl) {
        VideoInfo videoInfo = new VideoInfo(Uri.parse(murl))
                .setTitle("test video")
                .setAspectRatio(VideoInfo.AR_4_3_FIT_PARENT)
                .addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1L))
                .addOption(Option.create(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "multiple_requests", 1L))
                .setShowTopBar(true);
        GiraffePlayer.play(getBaseContext(), videoInfo);
        overridePendingTransition(0, 0);
    }

    private void initView() {
        mVideoUrl = (EditText) findViewById(R.id.video_url);
        mVideoUrl.setOnClickListener(this);
        mVideoStart = (Button) findViewById(R.id.video_start);
        mVideoStart.setOnClickListener(this);
        mVideoUrl.setText("");
        mVideoUrl.setText((String) SharedPreferencesUtils.getParam(getBaseContext(), "Incorrect", "rtsp://"));
        mCameraProview = (Button) findViewById(R.id.camera_proview);
        mCameraProview.setOnClickListener(this);
        mSupercamera = (Button) findViewById(R.id.supercamera);
        mSupercamera.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;

            case R.id.video_start:
                String mUrl = mVideoUrl.getText().toString().trim();
                if (TextUtils.isEmpty(mUrl)) {
                    Toast.makeText(this, "Incorrect input, please try again", Toast.LENGTH_SHORT).show();
                    return;
                }
                init(mUrl);
                SharedPreferencesUtils.setParam(getBaseContext(), "Incorrect", mUrl);
                break;
            case R.id.video_url:
                break;
            case R.id.camera_proview:
                startActivity(new Intent(getBaseContext(), LocalCamera.class));
                break;
            case R.id.supercamera:
                startActivity(new Intent(getBaseContext(), SuperCamera.class));
                break;
        }
    }
}
