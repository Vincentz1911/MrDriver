package com.vincentz.driver;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class InfoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_info, vg, false);
        ImageButton big = view.findViewById(R.id.btn_big);
        ImageButton ltop = view.findViewById(R.id.btn_left_top);
        ImageButton lbtm = view.findViewById(R.id.btn_left_bottom);
        ImageButton rtop = view.findViewById(R.id.btn_right_top);
        ImageButton rbtm = view.findViewById(R.id.btn_right_bottom);



        FragmentManager fm = getActivity().getSupportFragmentManager();
        (view.findViewById(R.id.btn_left_top)).setOnClickListener(v ->
        {
            Fragment fr = fm.findFragmentById(R.id.fl_left_top);
            View vw = fr.getView();
            ViewGroup parent = (ViewGroup) vw.getParent();
            parent.removeView(vw);

            Fragment frbig = fm.findFragmentById(R.id.fl_big);
            View vwbig = frbig.getView();
            ViewGroup parentbig = (ViewGroup) vwbig.getParent();
            parentbig.removeView(vwbig);

            parentbig.addView(vw);
            parent.addView(vwbig);

            if (fr instanceof SpotifyFragment) big.setImageResource(R.drawable.ic_spotify_logo_200dp);
            if (fr instanceof MapFragment) big.setImageResource(R.drawable.ic_maps_200dp);
            if (frbig instanceof SpotifyFragment) ltop.setImageResource(R.drawable.ic_spotify_logo_200dp);
            if (frbig instanceof MapFragment) ltop.setImageResource(R.drawable.ic_maps_200dp);
        });


//                View vv = fragment.getView();
//        ViewGroup parent = (ViewGroup)vv.getParent();
//        parent.removeView(vv);
//        newparent.addView(vv, layoutParams);

//                fm.beginTransaction()
//                .replace(R.id.fl_left_top, new SelectorFragment(), "").commit());

        (view.findViewById(R.id.btn_big)).setOnClickListener(v -> fm.beginTransaction()
                .replace(R.id.fl_big, new SelectorFragment(), "").commit());

        (view.findViewById(R.id.btn_right_top)).setOnClickListener(v -> fm.beginTransaction()
                .replace(R.id.fl_right_top, new SelectorFragment(), "").commit());

        (view.findViewById(R.id.btn_right_bottom)).setOnClickListener(v -> fm.beginTransaction()
                .replace(R.id.fl_right_bottom, new SelectorFragment(), "").commit());

        return view;
    }

}
