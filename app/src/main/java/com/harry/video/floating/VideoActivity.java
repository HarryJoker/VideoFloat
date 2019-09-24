package com.harry.video.floating;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

public class VideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
    }


    public void minClick(View v) {
        openMinWindow();
    }


    private Intent serviceIntent;


    public void openMinWindow() {
        if (!FloatService.isStarted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断系统版本
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
//                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);

                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivityForResult(intent, 0);
                } else {
                    serviceIntent = new Intent(this, FloatService.class);
                    startService(serviceIntent);
                    moveTaskToBack(true);
                }
            } else {
                serviceIntent = new Intent(this, FloatService.class);
                startService(serviceIntent);
                moveTaskToBack(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                    serviceIntent = new Intent(this, FloatService.class);
                    startService(serviceIntent);
                    moveTaskToBack(true);
                }
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (serviceIntent != null) {
            stopService(serviceIntent);
        }

    }

}
