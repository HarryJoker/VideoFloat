

# 模拟在线视频悬浮框 

解决在线视频缩略成小框，可以任意拖动位置



实现思路：

1，当用户点击最小化按钮的时候，最小化我们的视频通话Activity（这时Activity处于后台状态），

2，移除原先在Activity的视频画布

3，通过service创建开启悬浮框

4，新建一个新的视频画布SurfaceView，将画面动态添加到悬浮框里面去

5，监听悬浮框的触摸事件，让悬浮框可以拖拽移动；监听悬浮框的点击事件，

6，重写FloatView的onTouch事件实现拖到

7，点击悬浮框事件：移除悬浮框里面新建的那个视频画布，然后重新调起我们在后台的视频通话Activity

8，拉起的视频通话界面，恢复视频通话到SurfaceVIew



### 1，Manifest配置

`  <!-- 悬浮窗权限 -->
  <uses-feature android:name="android.permission.SYSTEM_ALERT_WINDOW" />
  <service android:name=".service.FloatVideoWindowService"/>
`



### 2，动态申请权限打开悬浮框

```
public void openMinWindow() {
    if (!FloatVideoWindowService.isStarted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断系统版本
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
            } else {
                serviceIntent = new Intent(VideoNewCallHXActivity.this, FloatVideoWindowService.class);
                startService(serviceIntent);
                moveTaskToBack(true);
            }
        } else {
            serviceIntent = new Intent(VideoNewCallHXActivity.this, FloatVideoWindowService.class);
            startService(serviceIntent);
            moveTaskToBack(true);
        }
    }
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                serviceIntent = new Intent(VideoNewCallHXActivity.this, FloatVideoWindowService.class);
                startService(serviceIntent);
                moveTaskToBack(true);
            }
        }
    }
}
```

## 3，FloatView创建

```
private void initWindow() {
    mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    inflater = LayoutInflater.from(getApplicationContext());
    mFloatingLayout = inflater.inflate(R.layout.layout_window, null);
    oppositeSurface = mFloatingLayout.findViewById(R.id.opposite_surface);
    wmParams = getParams();
    EMClient.getInstance().callManager().setSurfaceView(null, oppositeSurface);
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
    wmParams.x = ((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth() - ScreenUtils.dip2px(this, 100) - 100;
    wmParams.y = 210;
    return wmParams;
}


  //addView添加进phonewindow
  mWindowManager.addView(mFloatingLayout, wmParams);
```

## 4，FloatView（悬浮框）拖动及点击

```
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
```

## 5，FloatView绑定Toutch与click

```
mFloatingLayout.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

    }
});
mFloatingLayout.setOnTouchListener(new FloatingListener());
```



## 6，VideoActivity后台与前台切换

```
/*************必须要配置启动模式为：singleInstance  *****************/

//通过service打开浮框，将自己切入后台
serviceIntent = new Intent(VideoNewCallHXActivity.this, FloatVideoWindowService.class);
startService(serviceIntent);
moveTaskToBack(true);


//浮框click事件，将视频页拉回前台
mFloatingLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), VideoNewCallHXActivity.class);
                    startActivity(intent);
                }
            });
```

