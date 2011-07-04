package com.anompom.wheeleduino;

import com.anompom.wheeleduino.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WheeleduinoRemoteMain extends Activity {
  private final int REQUEST_ENABLE_BT = 1;
  //private final List<BluetoothDevice> mBluetoothList = new ArrayList<BluetoothDevice>();
  private ArrayAdapter<String> mBtList;

  private final byte FORWARD = 'f';
  private final byte BACK = 'b';
  private final byte FORWARD_RIGHT = 'r';
  private final byte FORWARD_LEFT = 'l';
  private final byte BACK_RIGHT = 'a';
  private final byte BACK_LEFT = 'k';
  private final byte CLOCKWISE = 'c';
  private final byte COUNTERCLOCKWISE = 'w';
  private final byte STOP = 's';

  private static final String TAG = "WheeleduinoRemote";

  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        //mBluetoothList.add(device);
        mBtList.add(device.getName() + " - " + device.getAddress());
      }
    }
  };

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mBtList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (mBluetoothAdapter == null) {
      // Device does not support bluetooth
    }

    if (!mBluetoothAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    //ListView btList = (ListView) findViewById(R.id.BtList);
    //btList.setAdapter(mBtList);
    //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    //registerReceiver(mReceiver, filter);
    //mBluetoothAdapter.startDiscovery();

    // TODO: Check this is not null
    //OutputStream carOutput = connect(mBluetoothAdapter);
  }

  public OutputStream connect(BluetoothAdapter mBluetoothAdapter) {
    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice("00:06:66:42:AA:9F");
    try{
      Method m = device.getClass().getMethod("createInsecureRfcommSocket",
          new Class[] {int.class});
      BluetoothSocket carSocket = (BluetoothSocket) m.invoke(device, 1);
      InputStream inputStream = carSocket.getInputStream();
      carSocket.connect();
      inputStream.read();
      OutputStream carOutput = carSocket.getOutputStream();
      return carOutput;
    } catch(IOException e) {
      Log.e(TAG, "IOException: " + e.getMessage());
      System.exit(1);
    } catch (NoSuchMethodException e) {
      Log.e(TAG, "NoSuchMethodException: " + e.getMessage());
      System.exit(1);
    } catch (IllegalAccessException e) {
      Log.e(TAG, "NoSuchMethodException: " + e.getMessage());
      System.exit(1);
    } catch (InvocationTargetException e) {
      Log.e(TAG, "InvocationTargetException: " + e.getMessage());
      System.exit(1);
    }
    return null;
  }
}
