package learning.com.applock;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Set;

public class AppLockActivity extends AppCompatActivity {
    MyService.ServiceBinder mServiceBinder;
    static boolean isLaunchByUser = false;
    SharedPreferences mPreferences;
    ServiceConnection mServiceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mServiceBinder= (MyService.ServiceBinder) iBinder;

    if(mServiceBinder.getLockType()>0){
       if(mServiceBinder.getLockType()==1){
           startActivity(new Intent(AppLockActivity.this,
                   PatternActivity.class));
           finish();
       }
       else if(mServiceBinder.getLockType()==2){
           startActivity(new Intent(AppLockActivity.this,PinLock.class));
           finish();
       }


}
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mServiceBinder=null;
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
Intent i=getIntent();
if(i!=null){
    String action=i.getAction();
    if(action!=null && action.equalsIgnoreCase(Intent.ACTION_MAIN)){
        isLaunchByUser=true;
    }
}
else{
    isLaunchByUser=false;
}
        mPreferences=getSharedPreferences("AppLock",MODE_PRIVATE);
        setContentView(R.layout.activity_app_lock);
        Intent intent=new Intent(this,MyService.class);
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(!isLockEnrolled()){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }
    private  boolean isLockEnrolled(){
        return mPreferences.getBoolean("Enrolled",false);
    }

}
