package com.vincentz.driver;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

public class SpotifyFragment extends Fragment {

    private Activity activity;
    private static final String CLIENT_ID = "4f791920ae734cd5b5bc91257acc3993";
    private static final String REDIRECT_URI = "com.vincentz.driver://callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    private final ErrorCallback mErrorCallback = this::logError;
    private String TAG = "Spotify";
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private TrackProgressBar mTrackProgressBar;
    private ImageView mCoverArtImageView;
    Subscription<PlayerState> mPlayerStateSubscription;
    Subscription<PlayerContext> mPlayerContextSubscription;

    private AppCompatTextView txt_artist, txt_song, txt_album;
    private AppCompatImageButton mPlayPauseButton, mSkipNextButton, mSkipPrevButton;
    private AppCompatImageView mPlayPauseButtonSmall;
    private AppCompatSeekBar mSeekBar;
    private LinearLayout lay_controls;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View root = li.inflate(R.layout.fragment_spotify, vg, false);
        activity = getActivity();
        initUI(root);
        initOnClick();

        //SubscribedToPlayerState();
        // Inflate the layout for this fragment

        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;

        root.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {
            final View[] viewsToHide = {txt_artist, txt_album, lay_controls};
            if (height/2 > view.getHeight()) {
                for (View v : viewsToHide) v.setVisibility(View.INVISIBLE);
            } else for (View v : viewsToHide) v.setVisibility(View.VISIBLE);

            Log.d(TAG, "onLayoutChange: ");
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI).showAuthView(true).build();

        SpotifyAppRemote.connect(getContext(), connectionParams, new Connector.ConnectionListener() {
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                Tools.msg(activity, "Connected to Spotify");
                connected();
            }

            public void onFailure(Throwable throwable) {
                Log.e(TAG, throwable.getMessage(), throwable);
                Tools.msg(activity, "Cannot Connect to Spotify");
            }
        });
    }

    private void initUI(View view) {
        mPlayPauseButton = view.findViewById(R.id.play_pause_button);
        mPlayPauseButtonSmall = view.findViewById(R.id.play_pause_button_small);
        mSkipPrevButton = view.findViewById(R.id.skip_prev_button);
        mSkipNextButton = view.findViewById(R.id.skip_next_button);

        mCoverArtImageView = view.findViewById(R.id.image);
        txt_album = view.findViewById(R.id.txt_album);
        txt_song = view.findViewById(R.id.txt_song);
        txt_artist = view.findViewById(R.id.txt_artist);
        lay_controls = view.findViewById(R.id.lay_controls);
        mSeekBar = view.findViewById(R.id.seek_to);
        mSeekBar.setEnabled(false);
//        mSeekBar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//        mSeekBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mTrackProgressBar = new TrackProgressBar(mSeekBar);

    }

    private void initOnClick() {
        mPlayPauseButton.setOnClickListener(v -> playPause());
        mPlayPauseButtonSmall.setOnClickListener(v -> playPause());
        mPlayPauseButtonSmall.setOnLongClickListener(v -> {
            onSkipNextButtonClicked();
            return true;
        });

        mSkipNextButton.setOnClickListener(v -> onSkipNextButtonClicked());
    }

    private void onSkipNextButtonClicked() {
        mSpotifyAppRemote
                .getPlayerApi()
                .skipNext()
                .setResultCallback(data -> logMessage(getString(R.string.command_feedback, "skip next")))
                .setErrorCallback(mErrorCallback);
    }

    public void onSkipPreviousButtonClicked(View view) {
        mSpotifyAppRemote
                .getPlayerApi()
                .skipPrevious()
                .setResultCallback(
                        empty -> logMessage(getString(R.string.command_feedback, "skip previous")))
                .setErrorCallback(mErrorCallback);
    }

    private void playPause() {
        mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
            if (playerState.isPaused) {
                mSpotifyAppRemote.getPlayerApi().resume().setResultCallback(empty ->
                        logMessage(getString(R.string.command_feedback, "play")))
                        .setErrorCallback(mErrorCallback);

            } else {
                mSpotifyAppRemote.getPlayerApi().pause().setResultCallback(empty ->
                        logMessage(getString(R.string.command_feedback, "pause")))
                        .setErrorCallback(mErrorCallback);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void connected() {
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().resume();
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;

                    if (track != null) {
                        // Update progressbar
                        if (playerState.playbackSpeed > 0) {
                            mTrackProgressBar.unpause();
                        } else {
                            mTrackProgressBar.pause();
                        }

                        // Invalidate play / pause
                        if (playerState.isPaused) {
                            mPlayPauseButton.setImageResource(R.drawable.ic_play_48dp);
                            mPlayPauseButtonSmall.setImageResource(R.drawable.ic_play_stroke_128dp);
                            mSeekBar.setThumb(getResources()
                                    .getDrawable(R.drawable.ic_pause_button, null));
                        } else {
                            mPlayPauseButton.setImageResource(R.drawable.ic_pause_48dp);
                            mPlayPauseButtonSmall.setImageResource(R.drawable.ic_pause_stroke_128dp);
                            mSeekBar.setThumb(getResources()
                                    .getDrawable(R.drawable.ic_play_button, null));

                        }

                        // Get image from track
                        mSpotifyAppRemote.getImagesApi()
                                .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                                .setResultCallback(bitmap -> {
                                    mCoverArtImageView.setImageBitmap(bitmap);
                                });

                        txt_artist.setText(track.artist.name);
                        txt_album.setText(track.album.name);
                        txt_song.setText(track.name);// + "(" + track.duration + ")"
                        Log.d(TAG, track.name + " by " + track.artist.name);

                        // Invalidate seekbar length and position
                        mSeekBar.setMax((int) playerState.track.duration);
                        mTrackProgressBar.setDuration(playerState.track.duration);
                        mTrackProgressBar.update(playerState.playbackPosition);

                        mSeekBar.setEnabled(true);
                    }
                });
    }

    private void playUri(String uri) {
        mSpotifyAppRemote
                .getPlayerApi()
                .play(uri)
                .setResultCallback(empty -> logMessage(getString(R.string.command_feedback, "play")))
                .setErrorCallback(mErrorCallback);
    }


    private void logError(Throwable throwable) {
        Toast.makeText(getContext(), "R.string.err_generic_toast", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "", throwable);
    }

    private void logMessage(String msg) {
        logMessage(msg, Toast.LENGTH_SHORT);
    }

    private void logMessage(String msg, int duration) {
        Toast.makeText(getContext(), msg, duration).show();
        Log.d(TAG, msg);
    }

    private void showDialog(String title, String message) {
        new AlertDialog.Builder(activity).setTitle(title).setMessage(message).create().show();
    }

    private class TrackProgressBar {
        private static final int LOOP_DURATION = 500;
        private final SeekBar mSeekBar;
        private final Handler mHandler;

        private final Runnable mSeekRunnable =
                new Runnable() {
                    @Override
                    public void run() {
                        int progress = mSeekBar.getProgress();
                        mSeekBar.setProgress(progress + LOOP_DURATION);
                        mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
                    }
                };

        private TrackProgressBar(SeekBar seekBar) {
            mSeekBar = seekBar;
            SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mSpotifyAppRemote
                            .getPlayerApi()
                            .seekTo(seekBar.getProgress())
                            .setErrorCallback(mErrorCallback);
                }
            };
            mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
            mHandler = new Handler();
        }

        private void setDuration(long duration) {
            mSeekBar.setMax((int) duration);
        }

        private void update(long progress) {
            mSeekBar.setProgress((int) progress);
        }

        private void pause() {
            mHandler.removeCallbacks(mSeekRunnable);
        }

        private void unpause() {
            mHandler.removeCallbacks(mSeekRunnable);
            mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
        }
    }
}
