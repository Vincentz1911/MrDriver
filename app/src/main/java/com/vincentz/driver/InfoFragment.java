package com.vincentz.driver;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.vincentz.driver.Tools.*;

public class InfoFragment extends Fragment {

    private Timer timer;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View root = li.inflate(R.layout.fragment_info, vg, false);
        TextView time = root.findViewById(R.id.txt_time);
        TextView date = root.findViewById(R.id.txt_date);
        TextView week = root.findViewById(R.id.txt_week);

        timer = new Timer("Timer");
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    Date dateNow = new Date();
                    time.setText(new SimpleDateFormat("HH:mm:ss",
                            Locale.getDefault()).format(dateNow));
                    date.setText(new SimpleDateFormat("EEE d. MMM",
                            Locale.getDefault()).format(dateNow));
                    week.setText(getString(R.string.week, new SimpleDateFormat("w - yyyy",
                            Locale.getDefault()).format(dateNow)));
                });
            }
        }, 1000, 1000);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer.purge();
    }
}
