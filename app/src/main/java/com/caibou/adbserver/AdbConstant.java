package com.caibou.adbserver;

/**
 * Created by ZWF on 2016/5/6.
 */
public class AdbConstant {
    public static final int AUTH_TYPE_TOKEN = 1;
    public static final int AUTH_TYPE_SIGNATURE = 2;
    public static final int AUTH_TYPE_RSAPUBLICKEY = 3;

    public static final int MSG_SYNC = 0x434e5953;
    public static final int MSG_CNXN = 0x4e584e43;
    public static final int MSG_AUTH = 0x48545541;
    public static final int MSG_OPEN = 0x4e45504f;
    public static final int MSG_OKAY = 0x59414b4f;
    public static final int MSG_CLSE = 0x45534c43;
    public static final int MSG_WRTE = 0x45545257;

}
