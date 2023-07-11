package learning.com.applock;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
RecyclerView app_list;
FloatingActionButton ok_btn;
    AppListAdapter adapter;
MyService.ServiceBinder mServiceBinder;
SharedPreferences mPreferences;

ServiceConnection mServiceConnection=new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mServiceBinder= (MyService.ServiceBinder) iBinder;
        if(mServiceBinder!=null){
            app_list=findViewById(R.id.app_list);
            app_list.setLayoutManager(new LinearLayoutManager(MainActivity.this));
             adapter=new AppListAdapter(mServiceBinder.getUnProtectedAppList(), new AppListAdapter.OnCallBackListen() {
                @Override
                public void onClick(AppInfo appInfo) {
mServiceBinder.makeToProtect(appInfo);
adapter.notifyDataSetChanged();
                }
            });
            app_list.setAdapter(adapter);
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
        setContentView(R.layout.activity_main);
mPreferences=getSharedPreferences("AppLock",MODE_PRIVATE);
        ok_btn=findViewById(R.id.ok_btn);
if(!checkIfGetPermission()){
    showPermissionRequestDialog();
}
 Intent intent=new Intent(MainActivity.this,MyService.class);
 bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
ok_btn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        if(mServiceBinder.isProtectedAppListChanged()){
            mServiceBinder.saveProtectedList();
            Toast.makeText(MainActivity.this,"Saved",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(MainActivity.this,"Not Saved",Toast.LENGTH_SHORT).show();
        }
    }
});
    }

   /* public void loadAppList(){
        PackageManager packageManager=getPackageManager();
        Intent intent =new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> homeApps=packageManager.queryIntentActivities(intent,0);
        List<AppInfo> apps=new ArrayList<>();
        for (ResolveInfo info: homeApps){

            AppInfo  appInfo=new AppInfo();
            appInfo.setAppLogo(info.activityInfo.loadIcon(packageManager));
            appInfo.setPackageName(info.activityInfo.packageName);
            appInfo.setAppName((String) info.activityInfo.loadLabel(packageManager));
            apps.add(appInfo);
        }

    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
private boolean checkIfGetPermission(){
    AppOpsManager appOpsManager= (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
int mode=appOpsManager.checkOpNoThrow("android:get_usage_stats",android.os.Process.myUid(),this.getPackageName());
return mode==AppOpsManager.MODE_ALLOWED;
    }
    private  void showPermissionRequestDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_lock)
                .setTitle("Warning")
                .setMessage("Permission Request")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),95);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       finish();
                    }
                })
                .create().show();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.settings){
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
    private  boolean isLockEnrolled(){
        return mPreferences.getBoolean("Enrolled",false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!isLockEnrolled()){
            startActivityForResult(new Intent(this,SettingsActivity.class),90);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==95){
            if(!checkIfGetPermission()){
                Toast.makeText(this,"No Permission",Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode==90){
            if(!isLockEnrolled()){finish();}
        }
    }
}
