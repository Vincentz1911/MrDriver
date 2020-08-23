package com.vincentz.driver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.ListItem;

import java.util.List;

class SpotifyImageAdapter extends ArrayAdapter<ListItem> {

    private List<ListItem> list;
    private SpotifyAppRemote mSpotifyAppRemote;

    SpotifyImageAdapter(@NonNull Context context, List<ListItem> list, SpotifyAppRemote mSpotifyAppRemote) {
        super(context, 0, list);
        this.list = list;
        this.mSpotifyAppRemote = mSpotifyAppRemote;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.adapter_image, parent, false);

        ListItem currentItem = list.get(position);
        ImageView imageView = listItem.findViewById(R.id.image);

        mSpotifyAppRemote.getImagesApi()
                .getImage(currentItem.imageUri, Image.Dimension.X_SMALL)
                .setResultCallback(imageView::setImageBitmap);

        return listItem;
    }
}