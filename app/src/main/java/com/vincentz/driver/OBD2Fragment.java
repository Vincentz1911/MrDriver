package com.vincentz.driver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.vincentz.driver.obd.commands.*;
import com.vincentz.driver.obd.commands.engine.*;
import com.vincentz.driver.obd.commands.temperature.AmbientAirTemperatureCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.vincentz.driver.Tools.*;

public class OBD2Fragment extends Fragment {

    private String speed = "", rpm = "", fuelLevel, oilTemp, consumption;
    private TextView txt_speed, txt_rpm, txt_oiltemp;
    private Thread OBDDataThread;
    private BluetoothSocket socket = null;
    private boolean isOn;
    private Activity ACT;

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_obd2, vg, false);

        txt_speed = view.findViewById(R.id.txt_speed);
        txt_rpm = view.findViewById(R.id.txt_rpm);
        txt_oiltemp = view.findViewById(R.id.txt_oiltemp);

        //initBT();
//        Timer update = new Timer();
//        update.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                updateView();
//            }
//        }, 0, 500);

        (view.findViewById(R.id.btn_startbt)).setOnClickListener(v -> {
                    initBT();
//                    new Thread(this::initBT).start();
                }
        );

        (view.findViewById(R.id.btn_startupdates)).setOnClickListener(v -> {
                    isOn = !isOn;
                    if (isOn) OBDDataThread.start();
                    else OBDDataThread.interrupt();
                    msg(getActivity(), "Is Thread Running: " + isOn);
                }
        );

        //new Thread(this::initBT).start();

        OBDDataThread = new Thread(() -> {
            if (socket != null && socket.isConnected()) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(1000);
                        SpeedCommand speedCommand = new SpeedCommand();
                        speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                        speed = speedCommand.getFormattedResult();

                        Thread.sleep(1000);
                        RPMCommand engineRpmCommand = new RPMCommand();
                        engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                        rpm = engineRpmCommand.getFormattedResult();

                        AmbientAirTemperatureCommand command = new AmbientAirTemperatureCommand();
                        command.run(socket.getInputStream(), socket.getOutputStream());
                        oilTemp = command.getFormattedResult();

                        ACT.runOnUiThread(() -> {
                            txt_speed.setText(getString(R.string.speed, speed));
                            txt_rpm.setText(getString(R.string.rpm, rpm));
                            txt_oiltemp.setText(getString(R.string.rpm, rpm));
                            //updateView();
                        });
                        msg(getActivity(), "Running thread");

                    } catch (IOException | InterruptedException e) {
                        msg(getActivity(), e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });


//        Timer timer = new Timer("Timer");
//        timer.scheduleAtFixedRate(new TimerTask() {
//            public void run() {
//                ACT.runOnUiThread(() -> updateView());
//            }
//        }, 1000, 500);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ACT = getActivity();
    }

    private void initBT() {
        //Gets list of paired devices
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            msg(getActivity(), "No Bluetooth device detected");
            return;
        }

        final ArrayList<BluetoothDevice> paired =
                new ArrayList<>(BluetoothAdapter.getDefaultAdapter().getBondedDevices());
        if (paired.size() == 0) {
            msg(getActivity(), "No paired devices found");
            return;
        }
        //Checks if device is named OBDII and connects
        for (BluetoothDevice device : paired) {
            if (device.getName().toUpperCase().equals("OBDII")) {
                msg(getActivity(), "Bluetooth OBDII device found: " + device.getName());
                ACT.getPreferences(Context.MODE_PRIVATE).edit()
                        .putString("btaddress", device.getAddress()).apply();
                connectBT(device.getAddress());
                return;
            }
        }
        selectBT(paired);
    }

    private void selectBT(final ArrayList<BluetoothDevice> paired) {
        //Creates list for dialog with paired bluetooth devices
        ArrayList<String> list = new ArrayList<>();
        for (BluetoothDevice device : paired) list.add(device.getName());
        final ArrayAdapter<String> adp = new ArrayAdapter<>(ACT,
                android.R.layout.select_dialog_singlechoice, list);

        //Creates dialog for choosing bluetooth device
        new AlertDialog.Builder(ACT)
                .setTitle("Choose Bluetooth device")
                .setSingleChoiceItems(adp, -1, (dialog, which) -> {
                    dialog.dismiss();
                    int pos = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    final String address = paired.get(pos).getAddress();
                    ACT.getPreferences(Context.MODE_PRIVATE).edit().
                            putString("btaddress", address).apply();
                    connectBT(address);
                }).show();
    }

    private void connectBT(String address) {
        //Connects to OBDII device and opens Fragment
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            socket.connect();
            Thread.sleep(5000);
            msg(getActivity(), "Connected to ELM327 Bluetooth ODBII adapter");

            //getODBdata(socket);
        } catch (IOException | InterruptedException e) {
            msg(getActivity(), "Couldn't connect to ELM327 Bluetooth ODBII adapter");
//            ACT.runOnUiThread(() -> {
//                txt_speed.setText(getString(R.string.speed, speed));
//                txt_rpm.setText(e.getMessage());
//                //updateView();
//            });

            e.printStackTrace();
        }

    }

    private void updateView() {
        if (ACT == null) return;
        ACT.runOnUiThread(() -> {
            txt_speed.setText(getString(R.string.speed, speed));
            txt_rpm.setText(getString(R.string.rpm, rpm));
        });
//        Tools.GPSUPDATE = 100 * ((SeekBar) getView().findViewById(R.id.sb_gps)).getProgress();
//        ((TextView) getView().findViewById(R.id.txt_gps)).setText("gps: " + Tools.GPSUPDATE);
//        Tools.CAMERAUPDATE = 100 * ((SeekBar) getView().findViewById(R.id.sb_cam)).getProgress();
//        ((TextView) getView().findViewById(R.id.txt_cam)).setText("cam: " + Tools.CAMERAUPDATE);
//        Tools.TIMERUPDATE = 100 * ((SeekBar) getView().findViewById(R.id.sb_timer)).getProgress();
//        ((TextView) getView().findViewById(R.id.txt_timer)).setText("timer: " + Tools.TIMERUPDATE);

//        ((TextView) getView().findViewById(R.id.txt_time))
//                .setText(getString(R.string.time, Tools.dateFormat.format(new Date())));


//        txt_speed.setText(getString(R.string.speed, speed));
//        txt_rpm.setText(getString(R.string.rpm, rpm));
    }
}
//}
//        try {
//            ((TextView) view.findViewById(R.id.txt_oiltemp))
//                    .setText("Socket: " + socket.isConnected()
//                            + "Con Type: " + +socket.getConnectionType()
//                            + "Max Recieve " + socket.getMaxReceivePacketSize()
//                            + "Max Transmit " + socket.getMaxTransmitPacketSize()
//                            + " " + socket.getRemoteDevice().getName() + " ");
//        } catch (Exception ignored){};


//        ((TextView) view.findViewById(R.id.txt_oiltemp))
//                .setText(getString(R.string.oilTemp, oilTemp));
//        ((TextView) view.findViewById(R.id.txt_fuellevel))
//                .setText(getString(R.string.fuelLevel, fuelLevel));
//        ((TextView) view.findViewById(R.id.txt_consumption))
//                .setText(getString(R.string.consumption, consumption));
//
//
//    private void getODBdata() {
//
//        while (!Thread.currentThread().isInterrupted()) {
//
//            if (socket != null && socket.isConnected()) {
//
//                try {
//                    //BluetoothSocket socket = socket;
//                    //Thread.sleep(100);
//                    RPMCommand engineRpmCommand = new RPMCommand();
//                    engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
//                    rpm = engineRpmCommand.getFormattedResult();
//
//                    SpeedCommand speedCommand = new SpeedCommand();
//                    speedCommand.run(socket.getInputStream(), socket.getOutputStream());
//                    speed = speedCommand.getFormattedResult();
//                } catch (IOException | InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//                OilTempCommand oilTempCommand = new OilTempCommand();
//                oilTempCommand.run(socket.getInputStream(), socket.getOutputStream());
//                oilTemp = oilTempCommand.getFormattedResult();
//
//                ConsumptionRateCommand consumptionRateCommand = new ConsumptionRateCommand();
//                consumptionRateCommand.run(socket.getInputStream(), socket.getOutputStream());
//                consumption = consumptionRateCommand.getFormattedResult();
//
//                FuelLevelCommand fuelLevelCommand = new FuelLevelCommand();
//                fuelLevelCommand.run(socket.getInputStream(), socket.getOutputStream());
//                fuelLevel = fuelLevelCommand.getFormattedResult();
//        }
//
//    }



