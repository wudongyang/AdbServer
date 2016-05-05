package com.caibou.adbserver;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String REMOTE_IP = "10.17.174.236";
    private final int REMOTE_PORT = 13489;
    private int REMOTE_ID = 0;
    private int LOCAL_ID = 0x00000004;
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private final int SYNC = 0x434e5953;
    private final int CNXN = 0x4e584e43;
    private final int AUTH = 0x48545541;
    private final int OPEN = 0x4e45504f;
    private final int OKAY = 0x59414b4f;
    private final int CLSE = 0x45534c43;
    private final int WRTE = 0x45545257;

    private SocketConnect mSocketConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_connect).setOnClickListener(this);
        findViewById(R.id.open_server).setOnClickListener(this);
        findViewById(R.id.btn_response_ok).setOnClickListener(this);
        findViewById(R.id.btn_response_close).setOnClickListener(this);
        findViewById(R.id.btn_open_sync).setOnClickListener(this);
        findViewById(R.id.btn_send_apk).setOnClickListener(this);
        findViewById(R.id.btn_install).setOnClickListener(this);

        mSocketConnect = new SocketConnect();
        mSocketConnect.connect(REMOTE_IP, REMOTE_PORT);
        mSocketConnect.setConnectListener(new SocketConnect.ConnectCallBack() {
            @Override
            public void connected() {
                Log.i(LOG_TAG, "connected");
            }

            @Override
            public void receive(byte[] buffer) {
                ByteBuffer buf = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
                int command = buf.getInt();
                int arg1 = buf.getInt();
                int arg2 = buf.getInt();


                switch (command) {
                    case SYNC:
                        Log.d(LOG_TAG, "SYNC");
                        break;
                    case CNXN:
                        Log.d(LOG_TAG, "CNXN");
                        break;
                    case AUTH:
                        Log.d(LOG_TAG, "AUTH");
                        break;
                    case OPEN:
                        Log.d(LOG_TAG, "OPEN");
                        break;
                    case OKAY:
                        Log.d(LOG_TAG, "OKAY");
                        REMOTE_ID = arg1;
                        break;
                    case CLSE:
                        Log.d(LOG_TAG, "CLSE");
                        break;
                    case WRTE:
                        Log.d(LOG_TAG, "WRTE");
                        break;
                }
                String str = String.format("command:%s, arg1:%s, arg2:%s", command, arg1, arg2);

                Log.d(LOG_TAG, str);
//                Log.d(LOG_TAG, "revdata Data:" + Arrays.toString(buffer));
                Log.d(LOG_TAG, new String(buffer));
            }

            @Override
            public void disconnect() {

            }

            @Override
            public void error() {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                int version = 0x01000000;
                int maxLength = 256 * 1024;
                send(CNXN, version, maxLength, "host::features=shell_2".getBytes());
                break;
            case R.id.open_server:
                send(OPEN, LOCAL_ID, REMOTE_ID, "shell: am start -n com.yumeng.tvservice/.MainActivity.".getBytes());
                break;
            case R.id.btn_response_ok:
                send(OKAY, LOCAL_ID, REMOTE_ID, null);
                break;
            case R.id.btn_response_close:
                send(CLSE, LOCAL_ID, REMOTE_ID, null);
                break;
            case R.id.btn_open_sync:
                send(OPEN, LOCAL_ID, REMOTE_ID, "sync:\u0000".getBytes());
                break;
            case R.id.btn_send_apk:
                pushApk();
                break;
            case R.id.btn_install:
                send(OPEN, LOCAL_ID, REMOTE_ID, "shell: pm install -r /data/local/tmp/app.apk.".getBytes());
                break;
        }
    }

    private void pushApk() {
        String location = "/data/local/tmp/app.apk,33206";
        ByteBuffer buf = ByteBuffer.allocate(8 + location.length()).order(ByteOrder.LITTLE_ENDIAN);
        try {
            buf.put("SEND".getBytes("UTF8"));
            buf.putInt(location.length());
            buf.put(location.getBytes("UTF8"));
            send(WRTE, LOCAL_ID, REMOTE_ID, buf.array());

            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "app.apk");

            if (file.exists()) {
                Log.i(LOG_TAG, "file is exists");

                InputStream inputStream = new FileInputStream(file);
                byte[] data = getFileByte(inputStream);
                int dataLength = data.length;
                int index = 0;
                int destPosition;
                while (true) {
                    destPosition = index + 4096;
                    ByteBuffer order = ByteBuffer.allocate(destPosition >= dataLength ? dataLength - index : 4096).order(ByteOrder.LITTLE_ENDIAN);
                    String str = String.format("index=%s, position=%s", index, destPosition);
                    Log.i(LOG_TAG, str);
                    order.put(data, index, destPosition >= dataLength ? dataLength - index : 4096);
                    send(WRTE, LOCAL_ID, REMOTE_ID, order.array());
                    index = destPosition;
                    if (index >= dataLength) {
                        break;
                    }
                }

                ByteBuffer order = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN);
                order.put("DONE\u0000".getBytes("UTF8"));
                send(WRTE, LOCAL_ID, REMOTE_ID, order.array());
                ByteBuffer order2 = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN);
                order2.put("QUIT\u0000".getBytes("UTF8"));
                send(WRTE, LOCAL_ID, REMOTE_ID, order2.array());
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    private byte[] getFileByte(final InputStream inputStream) {
        int n = 0;
        try {
            ArrayList<ByteBuffer> list = new ArrayList<>();
            byte[] array = new byte[65536];
            while (true) {
                final int read = inputStream.read(array);
                if (read == -1) {
                    break;
                }
                final ByteBuffer order = ByteBuffer.allocate(read + 8).order(ByteOrder.LITTLE_ENDIAN);
                order.put("DATA".getBytes("UTF8"));
                order.putInt(read);
                order.put(array, 0, read);
                n = n + read + 8;
                list.add(order);
            }
            inputStream.close();
            ByteBuffer order2 = ByteBuffer.allocate(n).order(ByteOrder.LITTLE_ENDIAN);
            Iterator<ByteBuffer> iterator = list.iterator();
            while (iterator.hasNext()) {
                order2.put(iterator.next().array());
            }
            return order2.array();
        } catch (Exception ex) {
            return null;
        }
    }

    private void send(int command, int arg1, int arg2, byte[] data) {
        int length = (data == null ? 0 : data.length);
        ByteBuffer buf = ByteBuffer.allocate(24 + length).order(ByteOrder.LITTLE_ENDIAN);
        int data_length = length;
        int n2 = 0, i = 0;
        if (data != null) {
            while (i < data_length) {
                final byte b = data[i];
                if (b >= 0) {
                    n2 += b;
                } else {
                    n2 += b + 256;
                }
                ++i;
            }
        }
        int data_crc32 = n2;
        int magic = command ^ 0xffffffff;

        buf.putInt(command);
        buf.putInt(arg1);
        buf.putInt(arg2);
        buf.putInt(data_length);
        buf.putInt(data_crc32);
        buf.putInt(magic);
        if (data != null) {
            buf.put(data);
        }
        Log.i(LOG_TAG, "send length : " + data_length);
//        Log.i(LOG_TAG, new String(buf.array()));
        mSocketConnect.send(buf.array());
    }

}
