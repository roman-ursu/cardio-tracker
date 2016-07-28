package com.romanus.cardiotracker.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.romanus.cardiotracker.db.DataBaseHelper;
import com.romanus.cardiotracker.db.beans.SavedBluetoothDevice;
import com.romanus.cardiotracker.util.RxHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by rursu on 27.07.16.
 */
public class BluetoothDeviceManager {

    private static final String TAG = BluetoothDeviceManager.class.getSimpleName();
    private BluetoothAPI bluetoothAPI;
    private DataBaseHelper dataBaseHelper;

    public BluetoothDeviceManager(BluetoothAPI bluetoothAPI, DataBaseHelper dataBaseHelper) {
        this.bluetoothAPI = bluetoothAPI;
        this.dataBaseHelper = dataBaseHelper;
    }

    public Observable<List<SavedBluetoothDevice>> scanForBLEDevices() {
        return Observable.create(new Observable.OnSubscribe<List<SavedBluetoothDevice>>() {
            @Override
            public void call(final Subscriber<? super List<SavedBluetoothDevice>> subscriber) {
                if (subscriber != null && !subscriber.isUnsubscribed()) {

                    bluetoothAPI.setScanCallback(new BluetoothAPI.ScanCallback() {
                        @Override
                        public void onDevicesFound(Set<BluetoothDevice> devices) {
                            updateDB(devices);
                            subscriber.onNext(convertData(devices));
                            subscriber.onCompleted();
                        }
                    });
                    bluetoothAPI.startScanLeDevices();
                }
            }
        }).compose(RxHelper.<List<SavedBluetoothDevice>>getSchedulers());
    }

    public void stopScan() {
        bluetoothAPI.stopScanLeDevices();
    }

    public Observable<List<SavedBluetoothDevice>> getBLEDevicesFromDB() {
        return Observable.create(new Observable.OnSubscribe<List<SavedBluetoothDevice>>() {
            @Override
            public void call(final Subscriber<? super List<SavedBluetoothDevice>> subscriber) {
                try {
                    List<SavedBluetoothDevice> savedBluetoothDevices = dataBaseHelper.getBluetoothDeviceDao().queryForAll();

                    if (subscriber != null && !subscriber.isUnsubscribed()) {
                        subscriber.onNext(savedBluetoothDevices);
                        subscriber.onCompleted();
                    }
                } catch (SQLException e) {
                    if (subscriber != null && !subscriber.isUnsubscribed()) {
                        subscriber.onError(e);
                    }
                }
                bluetoothAPI.startScanLeDevices();

            }
        }).compose(RxHelper.<List<SavedBluetoothDevice>>getSchedulers());
    }

    private List<SavedBluetoothDevice> convertData(Set<BluetoothDevice> devices) {
        List<SavedBluetoothDevice> list = new ArrayList<>();

        if (devices.size() > 0) {
            Iterator<BluetoothDevice> iterator = devices.iterator();
            while (iterator.hasNext()) {
                BluetoothDevice bluetoothDevice = iterator.next();
                SavedBluetoothDevice savedBluetoothDevice = new SavedBluetoothDevice();
                savedBluetoothDevice.setAddress(bluetoothDevice.getAddress());
                savedBluetoothDevice.setName(bluetoothDevice.getName());
                savedBluetoothDevice.setType(bluetoothDevice.getType());

                list.add(savedBluetoothDevice);
            }
        }
        return list;
    }

    private void updateDB(Set<BluetoothDevice> scannedDevices) {
        try {
            for (SavedBluetoothDevice newDevice : convertData(scannedDevices)) {
                dataBaseHelper.getBluetoothDeviceDao().createIfNotExists(newDevice);
            }

        } catch (SQLException e) {
            Log.e(TAG, "Error updating bluetooth devices DB");
        }
    }
}