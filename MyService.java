package learning.com.applock;

import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class MyService extends Service {
    boolean isScreenOn = true;
    PackageManager mPackageManager;
    UsageStatsManager usageStatsManager;
    List<AppInfo> appList = new ArrayList<>();
    List<String> unlockedAppList = new ArrayList<>();
    Set<String> checkList = new HashSet<>();
    String currentLockedApp = "";
    int lockType=-1;
    AppStartThread appStartThread;

    public MyService() {
    }

    ServiceBinder serviceBinder = new ServiceBinder();
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                isScreenOn = false;
            } else {
                isScreenOn = true;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return serviceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPackageManager = getPackageManager();
        initAppList();
        Connector.getDatabase();
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        appStartThread = new AppStartThread();
        appStartThread.start();

    }

    private void initAppList() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> homeApps = mPackageManager.queryIntentActivities(intent, 0);
        Collections.sort(homeApps, new ResolveInfo.DisplayNameComparator(mPackageManager));
        List<ProtectedApplication> protectedList = LitePal.findAll(ProtectedApplication.class);
        Set<String> packageSet = new HashSet<>();

        for (ResolveInfo info : homeApps) {

            if (info.activityInfo.packageName.equals(getPackageName())) {
                continue;
            }
            if (!packageSet.contains(info.activityInfo.packageName)) {
                packageSet.add(info.activityInfo.packageName);
                AppInfo appInfo = new AppInfo();
                appInfo.setAppLogo(info.activityInfo.loadIcon(mPackageManager));
                appInfo.setPackageName(info.activityInfo.packageName);
                Log.e("Test",info.activityInfo.packageName);
                appInfo.setAppName((String) info.activityInfo.loadLabel(mPackageManager).toString());
                if (protectedList.contains(new ProtectedApplication(info.activityInfo.packageName))) {
                    appInfo.setAppStatus(true);
                    checkList.add(appInfo.getPackageName());
                } else {
                    appInfo.setAppStatus(false);
                }
                appList.add(appInfo);
            }
        }
    }

    private void checkIfNeedProtection() {
        long time = System.currentTimeMillis();
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - 2000, time);
     //   Log.d("size",""+usageStatsList.size());
        if (usageStatsList != null && !usageStatsList.isEmpty()) {
            SortedMap<Long, UsageStats> usageStatsSortedMap = new TreeMap<>();
            for (UsageStats usageStats : usageStatsList) {
                usageStatsSortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            Log.d("status",""+usageStatsSortedMap.size());
            if (!usageStatsSortedMap.isEmpty()) {
                String topPackage = getForegroundApp(usageStatsManager);
                Log.d("top1",topPackage);
                if (!topPackage.isEmpty()) {
                    Log.d("top", topPackage);
                    if (checkList.contains(topPackage) && !unlockedAppList.contains(topPackage)) {
                        Log.e("contains", topPackage);
                        Intent intent = new Intent(this, AppLockActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        this.startActivity(intent);
                        currentLockedApp = topPackage;
                    } else {
                        //  Log.e("top","EMPTYYYYYYY");
                        if (!currentLockedApp.equalsIgnoreCase(topPackage) && !topPackage.equalsIgnoreCase(BuildConfig.APPLICATION_ID) && !currentLockedApp.isEmpty()) {
                            Log.e("else", currentLockedApp + "-" + topPackage);
                            unlockedAppList.remove(currentLockedApp);
                            currentLockedApp = "";
                        }
                    }
                }
            }

        }
    }

    private String getForegroundApp(UsageStatsManager usageStatsManager) {
        String name = "";
        long time = System.currentTimeMillis();
        UsageEvents usageEvents = usageStatsManager.queryEvents(time - 6000, time);
        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);
            switch (event.getEventType()) {
                case UsageEvents.Event.MOVE_TO_FOREGROUND:
                    name = event.getPackageName();
                    Log.e("Foreground",name);
                    break;
                case UsageEvents.Event.MOVE_TO_BACKGROUND:
                    if (event.getPackageName().equals(name)) {
                        name = "";
                    }
                    break;

                //    Log.e("Foreground",name);
            }

        }
        return name;
    }


    public class ServiceBinder extends Binder {
        public List<AppInfo> getUnProtectedAppList() {
            return appList;
        }

        public int makeToProtect(AppInfo appInfo) {
            appList.set(appList.indexOf(appInfo), appInfo);
            return appList.size();
        }

        public void saveProtectedList() {
            LitePal.deleteAll(ProtectedApplication.class);
            checkList.clear();
            for (AppInfo info : appList) {
                if (info.isAppStatus()) {
                    ProtectedApplication application = new ProtectedApplication();
                    application.setPackageName(info.getPackageName());
                    application.save();
                    checkList.add(info.getPackageName());

                }
            }
        }
        public  void discardProtectedListSettings(){
            appList.clear();
            initAppList();
        }
        public void  addUnlockedApp(){
            Log.e("AppName-Current",currentLockedApp);
            unlockedAppList.add(currentLockedApp);
        }
        public  boolean isProtectedAppListChanged(){
            List<ProtectedApplication> protectedApplicationList=LitePal.findAll(ProtectedApplication.class);
            for(AppInfo appInfo:appList){

                if(appInfo.isAppStatus()){
                    ProtectedApplication application=new ProtectedApplication(appInfo.getPackageName());
                    if(!protectedApplicationList.contains(application)){
                        return true;
                    }

                }
                else{
                    for(ProtectedApplication p:protectedApplicationList){
                        if(p.getPackageName().equalsIgnoreCase(appInfo.packageName)){
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        public  void saveLockInfo(String pattern,int type ){
            LitePal.deleteAll(LockInfo.class);
            LockInfo info=new LockInfo();
            info.setLockString(pattern);
            info.setLockType(type);
            info.save();
            lockType=type;

        }
        public  int getLockType(){
            if (lockType == -1) {
            LockInfo info=LitePal.findFirst(LockInfo.class);
            if(info!=null){
                lockType=info.getLockType();
            }

            }
            return lockType;
            }

        }


    private class AppStartThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                try {

                    Thread.sleep(500);
                    checkIfNeedProtection();
                } catch (Exception e) {
                    Log.e("Error",e.toString());
                }
            }
        }
    }
}
