package com.vincentz.driver;

import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.vincentz.driver.Tools.*;

public class SpotifyFragment extends Fragment {

    private String TAG = "Spotify";
    private static final String CLIENT_ID = "4f791920ae734cd5b5bc91257acc3993";
    private SpotifyAppRemote mSpotifyAppRemote;
    private Track track;
    private TrackProgressBar mTrackProgressBar;
    private AppCompatImageView mCoverArtImageView;
    private final ErrorCallback mErrorCallback = this::logError;

    private ListView playlistView;
    private AppCompatTextView txt_artist;
    private AppCompatImageView PlayButton, NextButton, PrevButton, ShuffleButton, RepeatButton, LikeButton;
    private AppCompatImageView AlbumButton, ArtistButton, PlaylistButton;
    private AppCompatSeekBar mSeekBar;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View root = li.inflate(R.layout.fragment_spotify, vg, false);
        initUI(root);
        initOnClick();

        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri("com.vincentz.driver://callback").showAuthView(true).build();

        SpotifyAppRemote.connect(getContext(), connectionParams, new Connector.ConnectionListener() {
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                msg(getActivity(),"Connected to Spotify");
                connected();
            }

            public void onFailure(Throwable throwable) {
                //Log.e(TAG, throwable.getMessage(), throwable);
                msg(getActivity(),"Failed to Connect to Spotify");
            }
        });
        return root;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//
//        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
//                .setRedirectUri(REDIRECT_URI).showAuthView(true).build();
//
//        SpotifyAppRemote.connect(getContext(), connectionParams, new Connector.ConnectionListener() {
//            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
//                mSpotifyAppRemote = spotifyAppRemote;
//                msg(getActivity(),"Connected to Spotify");
//                connected();
//            }
//
//            public void onFailure(Throwable throwable) {
//                //Log.e(TAG, throwable.getMessage(), throwable);
//                msg(getActivity(),"Failed to Connect to Spotify");
//            }
//        });
//    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void initUI(View view) {
        PlayButton = view.findViewById(R.id.play_pause_button);
        PrevButton = view.findViewById(R.id.skip_prev_button);
        NextButton = view.findViewById(R.id.skip_next_button);
        ShuffleButton = view.findViewById(R.id.btn_shuffle);
        RepeatButton = view.findViewById(R.id.btn_repeat);
        AlbumButton = view.findViewById(R.id.img_album);
        ArtistButton = view.findViewById(R.id.img_artist);
        PlaylistButton = view.findViewById(R.id.img_category);
        LikeButton = view.findViewById(R.id.btn_like);

        playlistView = view.findViewById(R.id.playlistView);
        mCoverArtImageView = view.findViewById(R.id.image);
        txt_artist = view.findViewById(R.id.txt_artist);
        mSeekBar = view.findViewById(R.id.seek_to);
        mSeekBar.setEnabled(false);
        mTrackProgressBar = new TrackProgressBar(mSeekBar);
    }

    private void initOnClick() {
        PlayButton.setOnClickListener(v -> mSpotifyAppRemote.getPlayerApi()
                .getPlayerState().setResultCallback(playerState -> {
                    if (playerState.isPaused) {
                        mSpotifyAppRemote.getPlayerApi().resume().setResultCallback(empty ->
                                msg(getActivity(),getString(R.string.command_feedback, "play")))
                                .setErrorCallback(mErrorCallback);

                    } else {
                        mSpotifyAppRemote.getPlayerApi().pause().setResultCallback(empty ->
                                msg(getActivity(),getString(R.string.command_feedback, "pause")))
                                .setErrorCallback(mErrorCallback);
                    }
                }));

        LikeButton.setOnClickListener(v -> mSpotifyAppRemote.getUserApi()
                .getLibraryState(track.uri).setResultCallback(libraryState -> {
                    if (!libraryState.isAdded) mSpotifyAppRemote
                            .getUserApi()
                            .addToLibrary(track.uri)
                            .setResultCallback(empty -> logMessage(getString(R.string.command_feedback, "Liked")))
                            .setErrorCallback(this::logError);

                    else mSpotifyAppRemote
                            .getUserApi()
                            .removeFromLibrary(track.uri)
                            .setResultCallback(empty -> getString(R.string.command_feedback, "Unliked"))
                            .setErrorCallback(this::logError);
                }));

        AlbumButton.setOnClickListener(view -> {
            mSpotifyAppRemote.getPlayerApi().play(track.album.uri);
            msg(getActivity(),"Playing Album: " + track.album.name);
        });

        ArtistButton.setOnClickListener(view -> {
            mSpotifyAppRemote.getPlayerApi().play(track.artist.uri);
            msg(getActivity(),"Playing Artist: " + track.artist.name);
        });

        PlaylistButton.setOnClickListener(view -> {
            if (playlistView.getVisibility() == View.GONE)
                playlistView.setVisibility(View.VISIBLE);
            else playlistView.setVisibility(View.GONE);
        });

        NextButton.setOnClickListener(v -> mSpotifyAppRemote.getPlayerApi()
                .skipNext()
                .setResultCallback(data -> msg(getActivity(),getString(R.string.command_feedback, "skip next")))
                .setErrorCallback(mErrorCallback));

        PrevButton.setOnClickListener(v -> mSpotifyAppRemote.getPlayerApi()
                .skipPrevious()
                .setResultCallback(empty -> msg(getActivity(),getString(R.string.command_feedback, "skip previous")))
                .setErrorCallback(mErrorCallback));
        ShuffleButton.setOnClickListener(view -> mSpotifyAppRemote.getPlayerApi()
                .toggleShuffle()
                .setResultCallback(empty -> logMessage(getString(R.string.command_feedback, "toggle shuffle")))
                .setErrorCallback(mErrorCallback));

        RepeatButton.setOnClickListener(view -> mSpotifyAppRemote.getPlayerApi()
                .toggleRepeat()
                .setResultCallback(empty -> logMessage(getString(R.string.command_feedback, "toggle repeat")))
                .setErrorCallback(mErrorCallback));
    }

    private void connected() {
        mSpotifyAppRemote.getPlayerApi().resume();
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            track = playerState.track;

            if (track != null) {
                // Invalidate play / pause
                if (playerState.isPaused) {
                    PlayButton.setImageResource(R.drawable.sic_play_48dp);
                    mSeekBar.setThumb(getResources().getDrawable(R.drawable.sic_pause_button, getActivity().getTheme()));
                } else {
                    PlayButton.setImageResource(R.drawable.sic_pause_48dp);
                    mSeekBar.setThumb(getResources().getDrawable(R.drawable.sic_play_button, getActivity().getTheme()));
                }

                //Sets ICON for shuffling
                if (playerState.playbackOptions.isShuffling)
                    ShuffleButton.setImageResource(R.drawable.sic_baseline_shuffle_24);
                else
                    ShuffleButton.setImageResource(R.drawable.sic_baseline_trending_flat_24);

                //Sets ICON for noRepeat/repeat/repeat1
                if (playerState.playbackOptions.repeatMode == 0)
                    RepeatButton.setImageResource(R.drawable.sic_baseline_vertical_align_bottom_24);
                else if (playerState.playbackOptions.repeatMode == 1)
                    RepeatButton.setImageResource(R.drawable.sic_baseline_repeat_one_24);
                else if (playerState.playbackOptions.repeatMode == 2)
                    RepeatButton.setImageResource(R.drawable.sic_baseline_repeat_24);

                // Get image from track
                mSpotifyAppRemote.getImagesApi()
                        .getImage(track.imageUri, Image.Dimension.LARGE)
                        .setResultCallback(bitmap -> mCoverArtImageView.setImageBitmap(bitmap));

                //Creates song info text with smaller "by" and "from" textsize
                SpannableString ss1 = new SpannableString(track.name + " by "
                        + track.artist.name + " from " + track.album.name);
                ss1.setSpan(new RelativeSizeSpan(0.6f), track.name.length(),
                        track.name.length() + 4, 0);
                ss1.setSpan(new RelativeSizeSpan(0.6f), track.name.length()
                        + 4 + track.artist.name.length(), track.name.length()
                        + 10 + track.artist.name.length(), 0);
                txt_artist.setText(ss1);
                txt_artist.setMovementMethod(new ScrollingMovementMethod());
                Log.d(TAG, track.name + " by " + track.artist.name + " from " + track.album.name);

                // Update progressbar & Invalidate seekbar length and position
                if (playerState.playbackSpeed > 0) mTrackProgressBar.unpause();
                else mTrackProgressBar.pause();

                mSeekBar.setMax((int) track.duration);
                mTrackProgressBar.setDuration(track.duration);
                mTrackProgressBar.update(playerState.playbackPosition);
                mSeekBar.setEnabled(true);

                //CHECKS IF SONG IS LIKED AND SETS ICON
                mSpotifyAppRemote.getUserApi().getLibraryState(track.uri).setResultCallback(libraryState -> {
                    if (libraryState.isAdded) LikeButton.setImageResource(R.drawable.sic_baseline_favorite_24);
                    else LikeButton.setImageResource(R.drawable.sic_baseline_favorite_border_24);
                });
            }
        });

        //GET RECOMMENDED ITEMS. SHOW IN LISTVIEW WHEN BUTTON IS CLICKED
        mSpotifyAppRemote
                .getContentApi()
                .getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
                .setResultCallback(this::recommendedContentCallBack)
                .setErrorCallback(mErrorCallback);
    }

    private void recommendedContentCallBack(ListItems listItems) {
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
            SpotifyImageAdapter arrayAdapter = new SpotifyImageAdapter(getContext(), combined, mSpotifyAppRemote);
            playlistView.setAdapter(arrayAdapter);

            playlistView.setOnItemClickListener((adapterView, view, i, l) -> {
                mSpotifyAppRemote.getPlayerApi().play(combined.get(i).uri);
                msg(getActivity(),"Playing: " + combined.get(i).title);
                playlistView.setVisibility(View.GONE);
            });
        }
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
