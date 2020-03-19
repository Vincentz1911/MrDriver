package com.vincentz.driver;

import androidx.fragment.app.Fragment;

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

import static com.vincentz.driver.Tools.*;

public class InfoFragment extends Fragment {

    private ImageView centerbig;
    private Timer timer;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View root = li.inflate(R.layout.fragment_info, vg, false);
        // SharedPreferences pref = activity.getSharedPreferences("12", Context.MODE_PRIVATE);

        //region TIME AND DATE
//        TextView time = root.findViewById(R.id.txt_time);
//        TextView date = root.findViewById(R.id.txt_date);
//        TextView week = root.findViewById(R.id.txt_week);
//        timer = new Timer("Timer");
//        timer.scheduleAtFixedRate(new TimerTask() {
//            public void run() {
//                ACT.runOnUiThread(() -> {
//                    Date dateNow = new Date();
//                    time.setText(new SimpleDateFormat("HH:mm:ss",
//                            Locale.getDefault()).format(dateNow));
//                    date.setText(new SimpleDateFormat("EEEE d. MMMM",
//                            Locale.getDefault()).format(dateNow));
//                    week.setText(getString(R.string.week, new SimpleDateFormat("w - yyyy",
//                            Locale.getDefault()).format(dateNow)));
//                });
//            }
//        }, 1000, 1000);
        //endregion

        //region INIT ONCLICK
        centerbig = root.findViewById(R.id.btn_big);
        centerbig.setImageResource(R.drawable.fic_maps_200dp);

        ImageView lefttop = root.findViewById(R.id.btn_left_top);
        lefttop.setImageResource(R.drawable.fic_info_200dp);

        ImageView leftbtm = root.findViewById(R.id.btn_left_bottom);
        leftbtm.setImageResource(R.drawable.fic_spotify_logo_200dp);

        ImageView righttop = root.findViewById(R.id.btn_right_top);
        righttop.setImageResource(R.drawable.fic_weather_200dp);

        ImageView rightbtm = root.findViewById(R.id.btn_right_bottom);
        rightbtm.setImageResource(R.drawable.fic_obd2_200dp);

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

    @Override
    public void onResume() {
        super.onResume();
        //region TIME AND DATE
        TextView time = getView().findViewById(R.id.txt_time);
        TextView date = getView().findViewById(R.id.txt_date);
        TextView week = getView().findViewById(R.id.txt_week);
        timer = new Timer("Timer");
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                ACT.runOnUiThread(() -> {
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer.purge();
    }

    private boolean removeFragment(int fragment, ImageView button) {
        FM.beginTransaction().replace(fragment, new SelectorFragment(), "").commit();
        button.setImageResource(R.drawable.fic_delete_200dp);
        return true;
    }

    private void switchViews(int idFrom, ImageView button) {
        Fragment fragFrom = FM.findFragmentById(idFrom);
        View vw = fragFrom.getView();
        ViewGroup parent = (ViewGroup) vw.getParent();
        parent.removeView(vw);

        Fragment fragCenter = FM.findFragmentById(R.id.fl_big_center);
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
                return R.drawable.fic_spotify_logo_200dp;
            case "MapFragment":
                return R.drawable.fic_maps_200dp;
            case "InfoFragment":
                return R.drawable.fic_info_200dp;
            case "WeatherFragment":
                return R.drawable.fic_weather_200dp;
            case "CameraFragment":
                return R.drawable.fic_videocam_200dp;
            case "OBD2Fragment":
                return R.drawable.fic_obd2_200dp;
            default:
                return R.drawable.fic_delete_200dp;

        }
    }
}
