package com.team14.beacon;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.team14.beacon.R;

public class MainActivity extends AppCompatActivity implements StartBeacon.StartBeaconCallback {

private static final String TAG =  MainActivity.class.getName();
    private Button button;
    public boolean butStatus = false;
private BluetoothAdapter adapter;
private StartBeacon startBeacon;
    private BluetoothGatt mGatt;

@Override
public void onDiscoveredBeacon(int ID, float voltage, float temperature, float d) {

        }

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
    adapter = manager.getAdapter();
    if (adapter == null) {
        Log.e(TAG, "Bluetooth Adapter not available");
        Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();
        return;
    }
    if (!adapter.isEnabled()) {
        Log.d(TAG, "onCreate: Bluetooth is not turned on, turning on bluetooth");
        Toast.makeText(getApplicationContext(), "Bluetooth is not turned on, turning on bluetooth", Toast.LENGTH_SHORT).show();
        adapter.enable();
    }

    startBeacon = new StartBeacon(this, adapter, this);

    button = (Button) findViewById(R.id.ContrBut);

    button.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            //start the weather service
            if (butStatus==false) {
                startBeacon.startService();// Perform action on click
                button.setText("STOP SCANNING");
                butStatus=true;
                Toast.makeText(getApplicationContext(),"Scanning...",Toast.LENGTH_SHORT).show();

            }
            else{
                startBeacon.stopService();
                button.setText("START SCANNING");
                butStatus=false;
            }

        }



    });


}
}

