package com.vincentz.driver;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.vincentz.driver.Tools.*;

public class SpotifyFragment extends Fragment {

    private static final String CLIENT_ID = "4f791920ae734cd5b5bc91257acc3993";
    private static final String REDIRECT_URI = "com.vincentz.driver://callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private Track track;
    private final ErrorCallback mErrorCallback = this::logError;
    private String TAG = "Spotify";
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private TrackProgressBar mTrackProgressBar;
    private ImageView mCoverArtImageView;
    Subscription<PlayerState> mPlayerStateSubscription;
    private Subscription<PlayerContext> mPlayerContextSubscription;

    private AppCompatTextView txt_artist, txt_song, txt_album;
    private AppCompatImageButton mPlayPauseButton, mSkipNextButton, mSkipPrevButton;
    private AppCompatImageView btn_playPauseSmall, btn_album, btn_artist, btn_playlist, btn_category;
    private AppCompatSeekBar mSeekBar;
    private LinearLayout lay_controls;
    private GridView gridView;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View root = li.inflate(R.layout.fragment_spotify, vg, false);
        initUI(root);
        initOnClick();

        //SubscribedToPlayerState();
        // Inflate the layout for this fragment

        DisplayMetrics displaymetrics = new DisplayMetrics();
        ACT.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;

        root.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {
            final View[] viewsToHide = {txt_artist, txt_album, lay_controls};
            if (view.getHeight() < (height + 100) / 2) {
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
                Tools.msg(ACT, "Connected to Spotify");
                connected();
            }

            public void onFailure(Throwable throwable) {
                Log.e(TAG, throwable.getMessage(), throwable);
                Tools.msg(ACT, "Failed to Connect to Spotify");
            }
        });
    }

    private void initUI(View view) {
        mPlayPauseButton = view.findViewById(R.id.play_pause_button);
        btn_playPauseSmall = view.findViewById(R.id.play_pause_button_small);
        mSkipPrevButton = view.findViewById(R.id.skip_prev_button);
        mSkipNextButton = view.findViewById(R.id.skip_next_button);

        btn_album = view.findViewById(R.id.img_album);
        btn_artist = view.findViewById(R.id.img_artist);
        btn_playlist = view.findViewById(R.id.img_playlist);
        btn_category = view.findViewById(R.id.img_category);

        mCoverArtImageView = view.findViewById(R.id.image);
        txt_album = view.findViewById(R.id.txt_album);
        txt_song = view.findViewById(R.id.txt_song);
        txt_artist = view.findViewById(R.id.txt_artist);
        lay_controls = view.findViewById(R.id.lay_controls);
        mSeekBar = view.findViewById(R.id.seek_to);
        mSeekBar.setEnabled(false);
        mTrackProgressBar = new TrackProgressBar(mSeekBar);
        gridView = view.findViewById(R.id.gridview);
    }

    private void initOnClick() {
        mPlayPauseButton.setOnClickListener(v -> playPause());
        btn_playPauseSmall.setOnClickListener(v -> playPause());
        btn_playPauseSmall.setOnLongClickListener(v -> {
            onSkipNextButtonClicked();
            return true;
        });

        btn_album.setOnClickListener(view -> {
            mSpotifyAppRemote.getPlayerApi().play(track.album.uri);
            msg(ACT, "Playing Album: " + track.album.name);
        });

        btn_artist.setOnClickListener(view -> {
            mSpotifyAppRemote.getPlayerApi().play(track.artist.uri);
            msg(ACT, "Playing Artist: " + track.artist.name);
        });

        btn_playlist.setOnClickListener(this::onSubscribedToPlayerContextButtonClicked);

        btn_category.setOnClickListener(view -> mSpotifyAppRemote
                .getContentApi()
                .getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
                .setResultCallback(this::recommendedContentCallBack)
                .setErrorCallback(mErrorCallback));

        btn_playlist.setOnLongClickListener(view -> {
            ClipboardManager clipboardManager = (ClipboardManager) ACT.getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData clipData = clipboardManager.getPrimaryClip();
            try {
                mSpotifyAppRemote.getPlayerApi().play(clipData.toString());
                msg(ACT, "Added Playlist");
            } catch (Exception e) {
                msg(ACT, "Error playing playlist");
            }

            return true;
        });

        mSkipNextButton.setOnClickListener(v -> onSkipNextButtonClicked());
    }

    private void playPlaylist() {
//        mSpotifyAppRemote
//                .getUserApi().getLibraryState()
//        mSpotifyAppRemote.getUserApi().subscribeToUserStatus().
//                setEventCallback(userStatus -> {
//                    int user = userStatus.code;
//                });
//
//        Types.RequestId i = mSpotifyAppRemote.getUserApi().subscribeToUserStatus().getRequestId()
//                Types.;
//        mSpotifyAppRemote.getUserApi().getClientId();
        //mSpotifyAppRemote.getPlayerApi().play();
        msg(ACT, "Playing Artist: " + track.artist.name);
    }

    private void onSkipNextButtonClicked() {
        mSpotifyAppRemote
                .getPlayerApi()
                .skipNext()
                .setResultCallback(data -> msg(getString(R.string.command_feedback, "skip next")))
                .setErrorCallback(mErrorCallback);
    }

    public void onSkipPreviousButtonClicked() {
        mSpotifyAppRemote
                .getPlayerApi()
                .skipPrevious()
                .setResultCallback(
                        empty -> msg(getString(R.string.command_feedback, "skip previous")))
                .setErrorCallback(mErrorCallback);
    }

    private void playPause() {
        mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
            if (playerState.isPaused) {
                mSpotifyAppRemote.getPlayerApi().resume().setResultCallback(empty ->
                        msg(getString(R.string.command_feedback, "play")))
                        .setErrorCallback(mErrorCallback);

            } else {
                mSpotifyAppRemote.getPlayerApi().pause().setResultCallback(empty ->
                        msg(getString(R.string.command_feedback, "pause")))
                        .setErrorCallback(mErrorCallback);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private final Subscription.EventCallback<PlayerContext> mPlayerContextEventCallback =
            new Subscription.EventCallback<PlayerContext>() {
                @Override
                public void onEvent(PlayerContext playerContext) {
                    //showCurrentPlayerContext(getView());
                    txt_song.setText(
                            String.format("%s\n%s", playerContext.title, playerContext.subtitle));
                    txt_song.setTag(playerContext);
                }
            };

//    public void RecommendedContent() {
//        mSpotifyAppRemote
//                .getContentApi()
//                .getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
//                .setResultCallback(listItems -> recommendedContentCallBack(listItems))
//                .setErrorCallback(mErrorCallback);
//    }

    private void recommendedContentCallBack(ListItems listItems) {
        gridView.setVisibility(View.VISIBLE);

        final CountDownLatch latch = new CountDownLatch(listItems.items.length);
        final List<ListItem> combined = new ArrayList<>();
        for (int j = 0; j < listItems.items.length; j++) {
            if (listItems.items[j].playable) {
                combined.add(listItems.items[j]);
                handleLatch(latch, combined);
            } else {
                mSpotifyAppRemote
                        .getContentApi()
                        .getChildrenOfItem(listItems.items[j], 3, 0)
                        .setResultCallback(
                                childListItems -> {
                                    combined.addAll(Arrays.asList(childListItems.items));
                                    handleLatch(latch, combined);
                                })
                        .setErrorCallback(mErrorCallback);
            }
        }

    }

    private void handleLatch(CountDownLatch latch, List<ListItem> combined) {
        latch.countDown();
        if (latch.getCount() == 0) {
            SpotifyImageAdapter arrayAdapter = new SpotifyImageAdapter(ACT, combined, mSpotifyAppRemote);
            gridView.setAdapter(arrayAdapter);

            gridView.setOnItemClickListener((adapterView, view, i, l) -> {
                mSpotifyAppRemote.getPlayerApi().play(combined.get(i).uri);
                msg(ACT, "Playing: " + combined.get(i).title);
                gridView.setVisibility(View.INVISIBLE);
            });
//            showDialog(
//                    getString(R.string.command_response, getString(R.string.browse_content)),
//                    gson.toJson(combined));
        }
    }

//    private void showCurrentPlayerContext(View view) {
//        if (view.getTag() != null) {
//            showDialog(gson.toJson(txt_song.getTag()));
//        }
//    }

    private void onSubscribedToPlayerContextButtonClicked(View view) {
        if (mPlayerContextSubscription != null && !mPlayerContextSubscription.isCanceled()) {
            mPlayerContextSubscription.cancel();
            mPlayerContextSubscription = null;
        }

//        mPlayerContextButton.setVisibility(View.VISIBLE);
//        mSubscribeToPlayerContextButton.setVisibility(View.INVISIBLE);
        //                                            mPlayerContextButton.setVisibility(View.INVISIBLE);
        //                                            mSubscribeToPlayerContextButton.setVisibility(View.VISIBLE);
        mPlayerContextSubscription =
                (Subscription<PlayerContext>)
                        mSpotifyAppRemote
                                .getPlayerApi()
                                .subscribeToPlayerContext()
                                .setEventCallback(mPlayerContextEventCallback)
                                .setErrorCallback(
                                        this::logError);
    }

    private void connected() {
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().resume();
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    track = playerState.track;

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
                            btn_playPauseSmall.setImageResource(R.drawable.ic_play_stroke_128dp);
                            mSeekBar.setThumb(getResources()
                                    .getDrawable(R.drawable.ic_pause_button, null));
                        } else {
                            mPlayPauseButton.setImageResource(R.drawable.ic_pause_48dp);
                            btn_playPauseSmall.setImageResource(R.drawable.ic_pause_stroke_128dp);
                            mSeekBar.setThumb(getResources()
                                    .getDrawable(R.drawable.ic_play_button, null));
                        }

                        // Get image from track
                        mSpotifyAppRemote.getImagesApi()
                                .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                                .setResultCallback(bitmap -> mCoverArtImageView.setImageBitmap(bitmap));

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

    private void logError(Throwable throwable) {
        Toast.makeText(getContext(), "R.string.err_generic_toast", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "", throwable);
    }

//    private void logMessage(String msg) {
//        logMessage(msg, Toast.LENGTH_SHORT);
//    }
//
//    private void logMessage(String msg, int duration) {
//        Toast.makeText(getContext(), msg, duration).show();
//        Log.d(TAG, msg);
//    }


    //region TRACKBAR
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

        TrackProgressBar(SeekBar seekBar) {
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
    //endregion
}
