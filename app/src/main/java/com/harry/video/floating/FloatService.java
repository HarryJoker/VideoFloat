package com.harry.video.floating;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class FloatService extends Service {
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams wmParams;
    private LayoutInflater inflater;
    public static boolean isStarted = false;
    //view
    private View mFloatingLayout;    //布局View


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;

        initWindow();

        System.out.println("FloatVideoWindowService onCreate----------------->");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("FloatVideoWindowService onCreate----------------->");

        showFloatingWindow();

        return super.onStartCommand(intent, flags, startId);
    }


//    EMCallStateChangeListener callStateListener = new EMCallStateChangeListener() {
//
//        @Override
//        public void onCallStateChanged(final EMCallStateChangeListener.CallState callState, final EMCallStateChangeListener.CallError error) {
//            switch (callState) {
//
//                case IDLE:
//
//                case NETWORK_DISCONNECTED:
//
//                case NETWORK_UNSTABLE:
//
//                case DISCONNECTED: // call is disconnected
//                    stopSelf();
//                    break;
//            }
//
//        }
//    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWindowManager != null) {
            mWindowManager.removeView(mFloatingLayout);
            isStarted = false;
        }
    }

    /**
     * 设置悬浮框基本参数（位置、宽高等）
     */
    private void initWindow() {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        inflater = LayoutInflater.from(getApplicationContext());
        mFloatingLayout = inflater.inflate(R.layout.layout_window, null);
        wmParams = getParams();
    }

    private WindowManager.LayoutParams getParams() {
        wmParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        wmParams.format = PixelFormat.RGBA_8888;
        //设置可以显示在状态栏上
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //这是悬浮窗居中位置
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        //70、210是我项目中的位置哦
        wmParams.x = ((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth() - 300;
        wmParams.y = 210;
        return wmParams;
    }


    private void showFloatingWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断系统版本
            if (Settings.canDrawOverlays(this)) {
                mFloatingLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
                mFloatingLayout.setOnTouchListener(new FloatingListener());
                System.out.println("FloatVideoWindowService canDrawOverlays addView show----------------->");

                mWindowManager.addView(mFloatingLayout, wmParams);
            } else {
                Toast.makeText(this, "没有canDrawOverlays权限", Toast.LENGTH_LONG).show();
            }
        } else {
            mFloatingLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
                    startActivity(intent);
                }
            });
            mFloatingLayout.setOnTouchListener(new FloatingListener());
            mWindowManager.addView(mFloatingLayout, wmParams);

            System.out.println("FloatVideoWindowService less 23 INT addView show----------------->");

        }
    }

    private int mTouchStartX, mTouchStartY, mTouchCurrentX, mTouchCurrentY;
    private int mStartX, mStartY, mStopX, mStopY;
    private boolean isMove;

    private class FloatingListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    isMove = false;
                    mTouchStartX = (int) event.getRawX();
                    mTouchStartY = (int) event.getRawY();
                    mStartX = (int) event.getX();
                    mStartY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mTouchCurrentX = (int) event.getRawX();
                    mTouchCurrentY = (int) event.getRawY();
                    wmParams.x += mTouchCurrentX - mTouchStartX;
                    wmParams.y += mTouchCurrentY - mTouchStartY;
                    mWindowManager.updateViewLayout(mFloatingLayout, wmParams);

                    mTouchStartX = mTouchCurrentX;
                    mTouchStartY = mTouchCurrentY;
                    break;
                case MotionEvent.ACTION_UP:
                    mStopX = (int) event.getX();
                    mStopY = (int) event.getY();
                    if (Math.abs(mStartX - mStopX) >= 1 || Math.abs(mStartY - mStopY) >= 1) {
                        isMove = true;
                    }
                    break;
            }
            return isMove;
        }
    }
}
