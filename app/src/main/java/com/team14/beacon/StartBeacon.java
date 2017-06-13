package com.team14.beacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import static java.lang.Integer.toHexString;


/**
 * Created by theilshat on 05.06.17.
 */

public class StartBeacon extends BluetoothGattCallback implements BLEScanner.BLEScannerCallback {

    private static final String TAG = StartBeacon.class.getName();
    Context context;

    public interface StartBeaconCallback{
        void onDiscoveredBeacon(int ID, float voltage, float temperature, float d);

    }

    private Context mContext;
    private BluetoothAdapter mAdapter;
    private StartBeaconCallback mCallback;
    private BLEScanner bleScanner;
    private boolean mRunning;
    private boolean mDeviceConnected;
    private boolean mBusy;
    private BluetoothGatt mGatt;
    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public StartBeacon(Context context, BluetoothAdapter adapter, StartBeaconCallback callback){
        this.mContext= context;
        this.mAdapter = adapter;
        this.mCallback= callback;
        this.bleScanner= new BLEScanner(adapter, this);
        this.context=context;
        mRunning= false;
        mDeviceConnected= false;
        mBusy=false;

    }

    public void startService(){
        if (!mRunning){
            bleScanner.startScan();
        } else{
            Log.d(TAG, "startService: System alredy runnning");
        }
    }
    public void stopService(){
        bleScanner.stopScan();
    }

    @Override
    public void onDeviceFound(ScanResult scanResult) {
        int rxPower=scanResult.getRssi();
        TextView txtDist = (TextView) ((AppCompatActivity)context).findViewById(R.id.dist);
        TextView txtUrl = (TextView) ((AppCompatActivity)context).findViewById(R.id.url);
        TextView txtVolt = (TextView) ((AppCompatActivity)context).findViewById(R.id.volt);
        TextView txtTemp = (TextView) ((AppCompatActivity)context).findViewById(R.id.temp);
        TextView txtId = (TextView) ((AppCompatActivity)context).findViewById(R.id.id);

        byte [] b=scanResult.getScanRecord().getBytes();

        String frameSign=String.valueOf(toHexString(b[11]));

        int txPower=255;
        double distance=0;
        float temp=0.0F;
        int bat=0;
        byte []IDBytes=new byte[16];
        String URL="";
        String ID="";
        int k=0;
        int m=0;
        for(k=13, m=0;k<29;k++,m++){
            IDBytes[m]=b[k];
        }

        // Check which type of frame we recieve
        if (frameSign.equals(String.valueOf(10))){
            txPower=b[12]-41;
            //Log.d(TAG, "Tx_Power: " + txPower);
            distance=getDistance(txPower,rxPower);
            URL=getUrl(b);
            Log.d(TAG, "Distance: " + distance);

            txtDist.setText(String.valueOf(distance));
            txtUrl.setText(URL);
            Log.d(TAG, "URL: " + URL);

        }
        if(frameSign.equals(String.valueOf(0))){
            txPower=b[12]-41;
            distance=getDistance(txPower,rxPower);
            ID=bytesToHex(IDBytes);
            Log.d(TAG, "Distance " + distance);
            txtDist.setText(String.valueOf(distance));
            Log.d(TAG, "ID: " + ID);
            txtId.setText(ID);

        }
        if (frameSign.equals(String.valueOf(20))){
            bat=getBatLev(b[13],b[14]);
            temp=getTemp(b[15],b[16]);
            Log.d(TAG, "Batt Lev1: " + bat);
            Log.d(TAG, "Temp: " + temp);
            txtVolt.setText(String.valueOf(bat));
            txtTemp.setText(String.valueOf(temp));

        }


        /*Log.d(TAG, "Rx power = " +str);*/
    }

    @Override
    public void onScanFailed(BLEScanner.ScannerFailureReason reason) {

    }

    @Override
    public void onScanCompleted() {

    }
    public int getBatLev(byte first, byte second){
        return ((first & 0xFF) << 8) | (second & 0xFF);
    }
    public float getTemp(byte first,byte second){
        return ((int)first+(second & 0xFF)/256.0F);
        //return first+second;

    }
    public static String bytesToHex(byte[] bytes) {

        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    public String getUrl(byte[] b){
        String UrlFull="";
        String UrlSc="";
        int urlInt=0;
        char urlEnc;
        if(b[13]==3){
            UrlSc="https://";
            UrlFull=UrlSc;
        }

        for (int j=14;j<32;j++){
            urlInt=b[j];
            if(urlInt!=0) {
                urlEnc = (char) urlInt;
                UrlFull += urlEnc;
            }
        }
        return UrlFull;
    }
    public double getDistance(int txpow, int rxpow){
        double pow10;
        if (rxpow == 0) {
            return -1.0F; // if we cannot determine accuracy, return -1.
        }

        double ratio = rxpow*1.0/txpow;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            //double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            pow10=(rxpow+70.0)/(-10*6.66);
            double accuracy=Math.pow(10,pow10);
            return accuracy;
        }

    }


}

