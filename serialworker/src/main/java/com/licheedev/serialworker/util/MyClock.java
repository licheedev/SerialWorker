package com.licheedev.serialworker.util;

import android.os.SystemClock;

public final class MyClock {
    private static final String TAG = "MyClock";

    /**
     * 不让初始化
     */
    private MyClock() {
    }

    /**
     * 等待指定的若干毫秒。
     * 类似 {@link Thread#sleep(long)}，不过不会抛出{@link InterruptedException}异常;
     *
     * @param ms 指定等待的时间，毫秒
     * @param forceDuration true，强制经过指定的时候后，方法才会返回；false，{@link Thread#interrupt()}后立即返回
     */
    private static void sleep(long ms, boolean forceDuration) {

        long start = uptimeMillis();
        long duration = ms;
        boolean interrupted = false;
        do {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                interrupted = true;
            }
            duration = start + ms - uptimeMillis();
        } while (forceDuration && duration > 0);

        if (interrupted) {
            // Important: we don't want to quietly eat an interrupt() event,
            // so we make sure to re-interrupt the thread so that the next
            // call to Thread.sleep() or Object.wait() will be interrupted.
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 等待指定的若干毫秒。
     * 类似 {@link Thread#sleep(long)}，不过不会抛出{@link InterruptedException}异常;
     * {@link Thread#interrupt()}后立即返回。
     */
    public static void sleep(long ms) {
        sleep(ms, false);
    }

    /**
     * 等待指定的若干毫秒。睡够才会停。
     * 类似 {@link Thread#sleep(long)}，不过不会抛出{@link InterruptedException}异常;
     * 调用{@link Thread#interrupt()}后，方法也不会立即返回，必须经过指定的时间后，方法才会返回。
     * 
     * @param ms
     */
    public static void sleepUntil(long ms) {
        sleep(ms, true);
    }

    /**
     * 开机到现在的时间，毫秒
     *
     * @return
     */
    private static long uptimeMillis() {
        return SystemClock.uptimeMillis();
    }
}
