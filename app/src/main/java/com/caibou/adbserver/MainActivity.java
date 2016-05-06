package com.caibou.adbserver;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AdbServer mAdbServer;

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

        mAdbServer = new AdbServer();
        mAdbServer.connect("10.17.174.200");


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                mAdbServer.init();
                break;
            case R.id.open_server:
                mAdbServer.openApk();
                break;
            case R.id.btn_response_ok:
//                send(MSG_OKAY, LOCAL_ID, REMOTE_ID, null);
                break;
            case R.id.btn_response_close:
//                send(MSG_CLSE, LOCAL_ID, REMOTE_ID, null);
                break;
            case R.id.btn_open_sync:
//                send(MSG_OPEN, LOCAL_ID, REMOTE_ID, "sync:\u0000".getBytes());
                mAdbServer.openSync();
                break;
            case R.id.btn_send_apk:
//                pushApk();
                mAdbServer.pushApk(Environment.getExternalStorageDirectory() + File.separator + "app.apk");
                break;
            case R.id.btn_install:
//                send(MSG_OPEN, LOCAL_ID, REMOTE_ID, "shell: pm install -r /data/local/tmp/app.apk.".getBytes());
                mAdbServer.install();
                break;
        }
    }


}
