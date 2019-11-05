package com.licheedev.serialworkerdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.licheedev.serialworkerdemo.serial.SerialManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 在后台运行的服务
 */
public class CommService extends Service {

    private static final int NOTIFICATION_ID = 233 + 10086;
    private Notification mNotification;

    public static class CommBinder extends Binder {

        private CommService mService;

        public CommBinder(CommService service) {
            mService = service;
        }

        public CommService getService() {
            return mService;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new CommBinder(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SerialManager.get().initDevice();

        // 显示前台服务
        setNotification();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {

        SerialManager.get().release();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(StopServiceEvent event) {
        // 关闭服务
        stopSelf();
    }

    /**
     * 设置前台服务
     */
    private void setNotification() {

        String CHANNEL_ID = "com.licheedev.serialworkerdemocommservice_noti";
        String CHANNEL_NAME = "commservice_noti";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);

        builder.setContentTitle(getString(R.string.app_name))
            .setContentText("COMM Service Running")
            .setContentIntent(newIntent(Notification.FLAG_ONGOING_EVENT))
            .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
            .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
            .setAutoCancel(false)//设置这个标志当用户单击面板就可以让通知将自动取消
            // 他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
            // 设置他为一个正在进行的通知。
            .setOngoing(true)
            // 向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
            //.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
            .setSmallIcon(R.mipmap.ic_launcher);

        Notification notification = builder.build();
        // 如果已经保存的通知为空，则表示还没发过通知，也就是没设置前台服务
        if (mNotification == null) {
            startForeground(NOTIFICATION_ID, notification);
        } else { // 否则更新通知
            NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
        // 缓存上一条通知
        mNotification = notification;
    }

    /**
     * Intent
     *
     * @param flags
     * @return
     */
    public PendingIntent newIntent(int flags) {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        //Intent intent = new Intent(this, MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, flags);
        return pendingIntent;
    }
}
