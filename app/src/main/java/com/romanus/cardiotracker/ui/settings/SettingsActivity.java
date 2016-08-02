package com.romanus.cardiotracker.ui.settings;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.romanus.cardiotracker.CardioTrackerApp;
import com.romanus.cardiotracker.R;
import com.romanus.cardiotracker.db.beans.SavedBluetoothDevice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by roman on 7/26/16.
 */
public class SettingsActivity extends Activity implements SettingsView {

    @BindView(R.id.rv_scanned_devices_list)
    RecyclerView scannedDevicesList;

    @BindView(R.id.rv_saved_devices_list)
    RecyclerView savedDevicesList;

    @BindView(R.id.bt_scan)
    Button scanButton;

    @BindView(R.id.pb_progress)
    ProgressBar progressBar;

    @BindView(R.id.tv_scan_status)
    TextView scanStatusTextView;

    @Inject
    SettingsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        CardioTrackerApp.getAppComponent().inject(this);
        presenter.setView(this);

        scannedDevicesList.setLayoutManager(new LinearLayoutManager(this));
        scannedDevicesList.setAdapter(new DeviceInfoAdapter());

        savedDevicesList.setLayoutManager(new LinearLayoutManager(this));
        savedDevicesList.setAdapter(new DeviceInfoAdapter());
    }

    @Override
    protected void onStop() {
        presenter.stopScanForDevices();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @OnClick(R.id.bt_scan)
    public void onScanClicked(View view) {
        presenter.startScanForDevices();
    }

    @Override
    public void onScannedDevicesDetected(List<SavedBluetoothDevice> devices) {
        updateDeviceList((DeviceInfoAdapter) scannedDevicesList.getAdapter(), devices);
    }

    @Override
    public void onSavedDevicesLoaded(List<SavedBluetoothDevice> devices) {
        updateDeviceList((DeviceInfoAdapter) savedDevicesList.getAdapter(), devices);
    }

    private void updateDeviceList(DeviceInfoAdapter adapter, List<SavedBluetoothDevice> devices) {
        adapter.getDevices().clear();
        adapter.getDevices().addAll(devices);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showScanProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            scanStatusTextView.setText("Scanning");
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            scanStatusTextView.setText("Stopped");
        }
    }

    private void onDeviceClicked(SavedBluetoothDevice device) {
        presenter.onDeviceSelected(device);
    }

    class DeviceInfoAdapter extends RecyclerView.Adapter<DeviceInfoViewHolder> {

        private List<SavedBluetoothDevice> devices = new ArrayList<>();

        public List<SavedBluetoothDevice> getDevices() {
            return devices;
        }

        @Override
        public DeviceInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
            return new DeviceInfoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DeviceInfoViewHolder holder, int position) {
            final SavedBluetoothDevice device = devices.get(position);

            holder.textView.setText(device.getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeviceClicked(device);
                }
            });
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }
    }

    class DeviceInfoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_text)
        TextView textView;

        public DeviceInfoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
