package com.vincentz.driver;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class InfoFragment extends Fragment {

    private Activity activity;
    private FragmentManager fm;
    private ImageView centerbig;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View root = li.inflate(R.layout.fragment_info, vg, false);
        activity = getActivity();
        if (activity == null) return null;
        fm = getActivity().getSupportFragmentManager();
        // SharedPreferences pref = activity.getSharedPreferences("12", Context.MODE_PRIVATE);

        //region TIME AND DATE
        TextView time = root.findViewById(R.id.txt_time);
        TextView date = root.findViewById(R.id.txt_date);
        TextView week = root.findViewById(R.id.txt_week);
        Timer timer = new Timer("Timer");
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                activity.runOnUiThread(() -> {
                    Date dateNow = new Date();
                    time.setText(new SimpleDateFormat("HH:mm:ss",
                            Locale.getDefault()).format(dateNow));
                    date.setText(new SimpleDateFormat("EEEE d. MMMM",
                            Locale.getDefault()).format(dateNow));
                    week.setText(getString(R.string.week, new SimpleDateFormat("w - yyyy",
                            Locale.getDefault()).format(dateNow)));
                });
            }
        }, 1000, 1000);
        //endregion

        //region INIT ONCLICK
        centerbig = root.findViewById(R.id.btn_big);
        ImageView lefttop = root.findViewById(R.id.btn_left_top);
        ImageView leftbtm = root.findViewById(R.id.btn_left_bottom);
        ImageView righttop = root.findViewById(R.id.btn_right_top);
        ImageView rightbtm = root.findViewById(R.id.btn_right_bottom);

        centerbig.setOnLongClickListener(v -> removeFragment(R.id.fl_big_center, centerbig));
        lefttop.setOnClickListener(v -> switchViews(R.id.fl_left_top, lefttop));
        lefttop.setOnLongClickListener(v -> removeFragment(R.id.fl_left_top, lefttop));
        leftbtm.setOnClickListener(v -> switchViews(R.id.fl_left_bottom, leftbtm));
        leftbtm.setOnLongClickListener(v -> removeFragment(R.id.fl_left_bottom, leftbtm));
        righttop.setOnClickListener(v -> switchViews(R.id.fl_right_top, righttop));
        righttop.setOnLongClickListener(v -> removeFragment(R.id.fl_right_top, righttop));
        rightbtm.setOnClickListener(v -> switchViews(R.id.fl_right_bottom, rightbtm));
        rightbtm.setOnLongClickListener(v -> removeFragment(R.id.fl_right_bottom, rightbtm));
        //endregion

        return root;
    }

    private boolean removeFragment(int fragment, ImageView button) {
        fm.beginTransaction().replace(fragment, new SelectorFragment(), "").commit();
        button.setImageResource(R.drawable.ic_delete_200dp);
        return true;
    }

    private void switchViews(int idFrom, ImageView button) {
        Fragment fragFrom = fm.findFragmentById(idFrom);
        View vw = fragFrom.getView();
        ViewGroup parent = (ViewGroup) vw.getParent();
        parent.removeView(vw);

        Fragment fragCenter = fm.findFragmentById(R.id.fl_big_center);
        View centerView = fragCenter.getView();
        ViewGroup parentCenter = (ViewGroup) centerView.getParent();
        parentCenter.removeView(centerView);

        centerbig.setImageResource(switchImage(fragFrom.getClass().getSimpleName()));
        button.setImageResource(switchImage(fragCenter.getClass().getSimpleName()));

        parentCenter.addView(vw);
        parent.addView(centerView);
    }

    private int switchImage(String simpleName) {
        switch (simpleName) {
            case "SpotifyFragment":
                return R.drawable.ic_spotify_logo_200dp;
            case "MapFragment":
                return R.drawable.ic_maps_200dp;
            case "InfoFragment":
                return R.drawable.ic_info_200dp;
            case "WeatherFragment":
                return R.drawable.ic_weather_200dp;
            case "CameraFragment":
                return R.drawable.ic_videocam_200dp;
            case "OBD2Fragment":
                return R.drawable.ic_obd2_200dp;
            default:
                return R.drawable.ic_delete_200dp;

        }
    }
}
