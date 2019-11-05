package com.licheedev.serialworkerdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import org.greenrobot.eventbus.EventBus;

public class CommServiceDelegate {

    private Intent mServiceIntent;
    private ServiceConnection mConn;
    private final Context mContext;
    private Callback mCallback;

    public interface Callback {

        void onServiceConnected(CommService commService);

        void onServiceDisconnected();
    }

    public CommServiceDelegate(Context context) {
        mContext = context;
    }

    /**
     * 连接用车服务服务
     */
    public void connectService(boolean startService, final boolean bindService) {
        mServiceIntent = new Intent(mContext, CommService.class);

        if (startService) {
            mContext.startService(mServiceIntent);
        }

        if (bindService) {
            mConn = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    CommService.CommBinder binder = (CommService.CommBinder) service;
                    if (mCallback != null) {
                        mCallback.onServiceConnected(binder.getService());
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    if (mCallback != null) {
                        mCallback.onServiceDisconnected();
                    }
                }
            };

            mContext.bindService(mServiceIntent, mConn, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * 解绑服务
     */
    public void unbindCommService() {
        if (mConn != null) {
            mContext.unbindService(mConn);
            mConn = null;
        }
    }

    /**
     * 停止服务
     */
    public void stopService() {

        if (mServiceIntent != null) {
            mContext.stopService(mServiceIntent);
        }
    }

    /**
     * 回调
     *
     * @param callback
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * 直接干掉服务
     */
    public static void killService() {
        EventBus.getDefault().post(StopServiceEvent.INST);
    }
}
