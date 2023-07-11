package learning.com.applock;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {
    SharedPreferences pref;
    MyService.ServiceBinder mServiceBinder;

    ServiceConnection mServiceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mServiceBinder= (MyService.ServiceBinder) iBinder;

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mServiceBinder=null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        pref = getSharedPreferences("AppLock", MODE_PRIVATE);
        Intent intent=new Intent(this,MyService.class);
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("Enrolled", true);
                editor.apply();
                finish();
            }
        }
    }

    public void chooseLockType(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Set Lock Type")
                .setSingleChoiceItems(R.array.lock_type, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    Intent intent = new Intent(SettingsActivity.this, PatternActivity.class);
                                    startActivityForResult(intent, 100);
                                } else if (i == 1) {
                                    Intent intent = new Intent(SettingsActivity.this, PinLock.class);
                                    startActivityForResult(intent, 100);
                                }
                                dialogInterface.dismiss();
                            }
                        }

                );
        builder.create().show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}
