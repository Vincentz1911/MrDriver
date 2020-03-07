package com.vincentz.driver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

//import com.vincentz1911.mapsandbtandobd2.obd.commands.*;
//import com.vincentz1911.mapsandbtandobd2.obd.commands.engine.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class OBD2Fragment extends Fragment {

    private String speed, rpm, fuelLevel, oilTemp, consumption;
    private Thread OBDDataThread;
    private BluetoothSocket socket = null;
    private boolean isOn;

    void msg(final String text) { getActivity().runOnUiThread(() ->
            Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show());
    }
    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_obd2, vg, false);

        (view.findViewById(R.id.btn_ison)).setOnClickListener(v -> isOn=true);

        OBDDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (socket != null && socket.isConnected()) {
                    if (isOn)
                        while (!Thread.currentThread().isInterrupted()) {
//                            try {
//                                Thread.sleep(500);
//                                RPMCommand engineRpmCommand = new RPMCommand();
//                                engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
//                                rpm = engineRpmCommand.getFormattedResult();
//
//                                SpeedCommand speedCommand = new SpeedCommand();
//                                speedCommand.run(socket.getInputStream(), socket.getOutputStream());
//                                speed = speedCommand.getFormattedResult();
//                            } catch (IOException | InterruptedException e) {
//                                e.printStackTrace();
//                            }
                        }
                }
            }
        });

        new Thread(() -> initBT()).start();

        Timer timer = new Timer("Timer");
        timer.scheduleAtFixedRate(new TimerTask() {public void run() {
            getActivity().runOnUiThread(() -> updateView()); }}, 1000, 1000);

        return view;
    }

    private void initBT() {
        //Gets list of paired devices
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            msg("No Bluetooth device detected");
            return;
        }

        final ArrayList<BluetoothDevice> paired =
                new ArrayList<>(BluetoothAdapter.getDefaultAdapter().getBondedDevices());
        if (paired.size() == 0) {
            msg("No paired devices found");
            return;
        }
        //Checks if device is named OBDII and connects
        for (BluetoothDevice device : paired) {
            if (device.getName().toUpperCase().equals("OBDII")) {
                msg("Bluetooth OBDII device found: " + device.getName());
                getActivity().getPreferences(Context.MODE_PRIVATE).edit()
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
        final ArrayAdapter<String> adp = new ArrayAdapter<>(getContext(),
                android.R.layout.select_dialog_singlechoice, list);

        //Creates dialog for choosing bluetooth device
        new AlertDialog.Builder(getContext())
                .setTitle("Choose Bluetooth device")
                .setSingleChoiceItems(adp, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        int pos = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        final String address = paired.get(pos).getAddress();
                        getActivity().getPreferences(Context.MODE_PRIVATE).edit().
                                putString("btaddress", address).apply();
                        connectBT(address);
                    }
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
            //getODBdata(socket);
        } catch (IOException e) {
            msg("Couldn't connect to ELM327 Bluetooth ODBII adapter");
            e.printStackTrace();
        }



        OBDDataThread.start();
    }

    private void updateView() {

//        ((TextView) getView().findViewById(R.id.txt_time))
//                .setText(getString(R.string.time, Tools.dateFormat.format(new Date())));
        ((TextView) getView().findViewById(R.id.txt_speed))
                .setText(getString(R.string.speed, speed));
        ((TextView) getView().findViewById(R.id.txt_rpm))
                .setText(getString(R.string.rpm, rpm));
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



