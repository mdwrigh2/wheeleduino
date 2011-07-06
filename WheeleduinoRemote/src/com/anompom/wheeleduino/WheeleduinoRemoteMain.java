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
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
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
  private OutputStream carOutput;

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

    /* Setup the buttons for control */
    ((TextView) findViewById(R.id.topLeft)).setOnTouchListener(buttonHandler(FORWARD_LEFT));
    ((TextView) findViewById(R.id.topCenter)).setOnTouchListener(buttonHandler(FORWARD));
    ((TextView) findViewById(R.id.topRight)).setOnTouchListener(buttonHandler(FORWARD_RIGHT));

    ((TextView) findViewById(R.id.centerLeft)).setOnTouchListener(buttonHandler(CLOCKWISE));
    ((TextView) findViewById(R.id.centerCenter)).setOnTouchListener(buttonHandler(STOP));
    ((TextView) findViewById(R.id.centerRight)).setOnTouchListener(buttonHandler(COUNTERCLOCKWISE));

    ((TextView) findViewById(R.id.bottomLeft)).setOnTouchListener(buttonHandler(BACK_LEFT));
    ((TextView) findViewById(R.id.bottomCenter)).setOnTouchListener(buttonHandler(BACK));
    ((TextView) findViewById(R.id.bottomRight)).setOnTouchListener(buttonHandler(BACK_RIGHT));

    //ListView btList = (ListView) findViewById(R.id.BtList);
    //btList.setAdapter(mBtList);
    //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    //registerReceiver(mReceiver, filter);
    //mBluetoothAdapter.startDiscovery();

    // TODO: Check this is not null
    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice("00:06:66:42:AA:9F");
    BluetoothSocket carSocket = connect(device);
    carOutput = new LogStream(TAG);
    try {
      carOutput = carSocket.getOutputStream();
    } catch (IOException e) {
      Log.e(TAG, "Error getting OutputStream from BluetoothSocket, output being redirected to logs: "
                 + e.getMessage());
    } catch (NullPointerException e) {
      Log.e(TAG, "Unable to connect to device, OutputStream being redirected to logs");
    }
  }

  private BluetoothSocket connect(BluetoothDevice device) {
    try{
      Method m = device.getClass().getMethod("createInsecureRfcommSocket",
          new Class[] {int.class});
      BluetoothSocket carSocket = (BluetoothSocket) m.invoke(device, 1);
      InputStream inputStream = carSocket.getInputStream();
      carSocket.connect();
      inputStream.read();
      return carSocket;
    } catch(IOException e) {
      Log.e(TAG, "IOException: " + e.getMessage());
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

  private OnTouchListener buttonHandler(final byte b) {
    return new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
        /* Make sure the connection is established first */
        if (carOutput == null) {
          Log.v(TAG, "Button pressed when connection stream has not yet been established");
          return false;
        }
        /* When a button is pushed down, send the signal to go in the appropriate direction */ 
        try {
          if (event.getAction() == MotionEvent.ACTION_DOWN){
            carOutput.write(b);
            carOutput.flush();
          } else if (event.getAction() == MotionEvent.ACTION_UP) {
            carOutput.write(STOP);
            carOutput.flush();
          } else {
            return false;
          }
          return true;
        } catch (IOException e) {
          Log.e(TAG, "IOException thrown while sending command to car: " + e.getMessage());
          return false;
        }
      }
    };
  }
}
