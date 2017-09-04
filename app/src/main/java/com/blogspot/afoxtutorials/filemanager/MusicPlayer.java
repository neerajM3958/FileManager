package com.blogspot.afoxtutorials.filemanager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MusicPlayer extends AppCompatActivity implements View.OnClickListener {
    SeekBar mSeekBar;
    TextView mDurationView, mCurrentPositionView;
    long mDuration, mCurrentpostion;
    private File file;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private FloatingActionButton FabPlay, FabForward, FabPrevious;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private ImageView AlbumArtView;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeID = new Themer(this).themeRes();
        setTheme(themeID);
        setContentView(R.layout.activity_music_player);
        applyTheme();
        FabPlay = (FloatingActionButton) findViewById(R.id.music_payer_fab_play);
        FabPlay.setOnClickListener(this);
        FabPrevious = (FloatingActionButton) findViewById(R.id.music_player_fab_previous);
        FabPrevious.setOnClickListener(this);
        FabForward = (FloatingActionButton) findViewById(R.id.music_player_fab_forward);
        FabForward.setOnClickListener(this);
        mSeekBar = (SeekBar) findViewById(R.id.music_player_progressBar);
        mDurationView = (TextView) findViewById(R.id.music_payer_duration);
        mCurrentPositionView = (TextView) findViewById(R.id.music_payer_current_position);


        Intent intent = getIntent();
        Uri uri = intent.getData();
        file = new File(uri.getPath());


        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(file.getPath());
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Setting data for displaying
        mDuration = mMediaPlayer.getDuration();
        mDurationView.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) mDuration),
                TimeUnit.MILLISECONDS.toSeconds((long) mDuration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                mDuration)))
        );


        setAlbumArt();
        mSeekBar.setMax(mMediaPlayer.getDuration());
        progressTracker();


        //Gaining Audio Focus and setting on focus change listener
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                        focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    }
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    if (mMediaPlayer.getCurrentPosition() > 0) {
                        mMediaPlayer.start();
                    }
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    mMediaPlayer.pause();
                }
            }
        };


        int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, mAudioManager.STREAM_MUSIC, mAudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        if (result == mAudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            FabPlay.setImageResource(R.drawable.ic_pause_24dp);
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mMediaPlayer.seekTo(0);
                    mMediaPlayer.pause();
                    FabPlay.setImageResource(R.drawable.ic_play_24dp);
                }
            });
        }
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) mMediaPlayer.seekTo(mSeekBar.getProgress());
            }
        });

    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }

    @Override
    public void onClick(View v) {
        int dur = mMediaPlayer.getDuration();
        int curPos = mMediaPlayer.getCurrentPosition();
        int jump = 0;
        switch (v.getId()) {
            case R.id.music_payer_fab_play:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    FabPlay.setImageResource(R.drawable.ic_play_24dp);
                } else {
                    mMediaPlayer.start();
                    FabPlay.setImageResource(R.drawable.ic_pause_24dp);
                }
                break;
            case R.id.music_player_fab_previous:
                jump = curPos - (dur / 10) < 0 ? 0 : curPos - (dur / 10);
                mMediaPlayer.seekTo(jump);
                break;
            case R.id.music_player_fab_forward:
                jump = curPos + (dur / 10) > dur ? dur : curPos + (dur / 10);
                mMediaPlayer.seekTo(jump);
                break;
        }
    }

    public void setAlbumArt() {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(file.getPath());
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            AlbumArtView = (ImageView) findViewById(R.id.music_payer_album_art);
            AlbumArtView.setImageBitmap(bitmap);

        }
        mmr.release();
    }

    public void progressTracker() {
        final Handler mHandler = new Handler();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null) {
                    mCurrentpostion = mMediaPlayer.getCurrentPosition();
                    mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                    mCurrentPositionView.setText(String.format("%d:%d",
                            TimeUnit.MILLISECONDS.toMinutes((long) mCurrentpostion),
                            TimeUnit.MILLISECONDS.toSeconds((long) mCurrentpostion) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                            mCurrentpostion))));

                }
                mHandler.postDelayed(this, 1000);
            }
        });
    }

    private void applyTheme() {
        Themer themer = new Themer(this);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.music_payer_base_rlayout);
        relativeLayout.setBackgroundColor(themer.fetchBackgroundColor());
        RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.music_payer_root_view);
        rootLayout.setBackgroundColor(themer.fetchBackgroundColor());
        ImageView albumArt = (ImageView) findViewById(R.id.music_payer_album_art);
        DrawableCompat.setTint(albumArt.getDrawable(), themer.fetchAccentColor());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return true;
    }
}
