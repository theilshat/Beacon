package com.team14.beacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by theilshat on 05.06.17.
 */

public class BLEScanner extends ScanCallback{

        private static final android.os.ParcelUuid SERVICE_UUID = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");

        /**
         * The BLE Scanner Callback which can be used to get feedback from the scanner
         */
        interface BLEScannerCallback {
            /**
             * Called Whenever a device with the scan criteria has been found
             *
             */
            void onDeviceFound(ScanResult scanResult);

            /**
             * Called when the scan fails
             *
             * @param reason The reason of the failure
             */
            void onScanFailed(ScannerFailureReason reason);

            /**
             * Called when the scan completes after the defined amount of time, regardless of device found or not
             */
            void onScanCompleted();
        }

        /**
         * The reasons for Scanner Failure
         */
        enum ScannerFailureReason {
            SCAN_ALREADY_STARTED,
            APPLICATION_REGISTRATION_FAILED,
            SCAN_NOT_SUPPORTED,
            INTERNAL_ERROR
        }

        private static final String TAG = BLEScanner.class.getName();
        /**
         * The BLE Scan Time in Millis
         */
        private static final long SCAN_TIME = 20000;

        /**
         * The handler used to post delayed runnable tasks, usually to stop the scanner after a certain time
         */

        /**
         * defines whether the device is currently BLE Scanning or not
         */
        private boolean mScanning;
        /**
         * The bluetooth adapter, to start start the LE Scans
         */
        private BluetoothAdapter adapter;

        /**
         * Callback that is waiting for scanner results
         */
        private BLEScannerCallback mCallback;

        BLEScanner(BluetoothAdapter adapter, BLEScannerCallback callback) {
        this.adapter = adapter;

        mScanning = false;
        this.mCallback = callback;
    }


        /**
         * Starts a scan for the specified device name
         *
         */
    void startScan() {
        if (!mScanning) {
            mScanning = true;

            //ScanFilter
            List<ScanFilter> filters = new ArrayList<>(1);
            ScanFilter.Builder mBuilder = new ScanFilter.Builder();
            mBuilder.setServiceUuid(SERVICE_UUID);

            //ScanSetting
            ScanSettings.Builder scanSettingBuilder = new ScanSettings.Builder();
            scanSettingBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
            scanSettingBuilder.setReportDelay(0);

            /*was handler*/

            adapter.getBluetoothLeScanner().startScan(filters, scanSettingBuilder.build(), this);
        } else {
            Log.d(TAG, "startScan: Scanner already running");
        }
    }

    private byte[] getIdAsByte(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    /**
     * Stops the scan
     */
    void stopScan() {
        if (mScanning) {
            Log.d(TAG, "stopScan: Stopping LE Scanner");
            adapter.getBluetoothLeScanner().stopScan(this);
            mScanning = false;
            if (mCallback != null) {
                mCallback.onScanCompleted();
            }
        } else {
            Log.d(TAG, "stopScan: Scanner not running");
        }
    }
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        /*Log.d(TAG, "onScanResult: Device Found: " + result.toString());*/
        if (mCallback != null) {

            mCallback.onDeviceFound(result);
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        //nothing to do here
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e(TAG, "onScanFailed: The Scan Failed with error code: " + errorCode);
        ScannerFailureReason reason;
        switch (errorCode) {
            case SCAN_FAILED_ALREADY_STARTED:
                reason = ScannerFailureReason.SCAN_ALREADY_STARTED;
                break;
            case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                reason = ScannerFailureReason.APPLICATION_REGISTRATION_FAILED;
                break;
            case SCAN_FAILED_FEATURE_UNSUPPORTED:
                reason = ScannerFailureReason.SCAN_NOT_SUPPORTED;
                break;
            case SCAN_FAILED_INTERNAL_ERROR:
                reason = ScannerFailureReason.INTERNAL_ERROR;
                break;
            default:
                reason = ScannerFailureReason.INTERNAL_ERROR;
        }
        if (reason == ScannerFailureReason.SCAN_ALREADY_STARTED) {
            // if scan already started, it is not a failure
            mScanning = true;
        } else if (mCallback != null) {
            mCallback.onScanFailed(reason);
        }
    }
}
