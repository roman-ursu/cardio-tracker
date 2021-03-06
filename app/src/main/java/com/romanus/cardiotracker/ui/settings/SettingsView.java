package com.romanus.cardiotracker.ui.settings;

import com.romanus.cardiotracker.db.beans.SavedBluetoothDevice;

import java.util.List;

/**
 * Created by roman on 7/26/16.
 */
public interface SettingsView {
    void onScannedDevicesDetected(List<SavedBluetoothDevice> devices);
    void onSavedDevicesLoaded(List<SavedBluetoothDevice> devices);
    void showScanProgress(boolean show);
    void showDeviceConnected(String address);
    void showDeviceConnecting(String address);
    void showDeviceDisconnected(String address);
    void heartRateUpdated(int data, String address);
}
