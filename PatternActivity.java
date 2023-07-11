package learning.com.applock;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import org.litepal.LitePal;

import java.util.List;

public class PatternActivity extends Activity {
    PatternLockView mPatternLockView;
    String _pattern="";
    Boolean isPatternConfirm=false;
    TextView pattern_label;
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
        setContentView(R.layout.activity_pattern);
        Intent intent=new Intent(this,MyService.class);
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
        mPatternLockView = (PatternLockView) findViewById(R.id.pattern_lock_view);
        mPatternLockView.addPatternLockListener(mPatternLockViewListener);
        pattern_label=findViewById(R.id.pattern_label);
        findViewById(R.id.pattern_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            Log.d(getClass().getName(), "Pattern drawing started");
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            Log.d(getClass().getName(), "Pattern progress: " +
                    PatternLockUtils.patternToString(mPatternLockView, progressPattern));
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            Log.d(getClass().getName(), "Pattern complete: " +
                    PatternLockUtils.patternToString(mPatternLockView, pattern));
            if(mServiceBinder.getLockType()==1){  LockInfo lockInfo = LitePal.findFirst(LockInfo.class);
            if (PatternLockUtils.patternToString(mPatternLockView, pattern).equalsIgnoreCase(lockInfo.lockString)) {
                if (AppLockActivity.isLaunchByUser)
                    startActivity(new Intent(PatternActivity.this, MainActivity.class));
                else
                    mServiceBinder.addUnlockedApp();
                finish();
            }
        }
         else if(isPatternConfirm){
                if(_pattern.equals(PatternLockUtils.patternToString(mPatternLockView, pattern))){
                    isPatternConfirm=false;
                    mServiceBinder.saveLockInfo(_pattern,1);
                    setResult(RESULT_OK);
                    finish();
                }
                else{
                    Toast.makeText(PatternActivity.this, "Pattern Mismatch. Try Again.", Toast.LENGTH_SHORT).show();
                  isPatternConfirm=false;
                    pattern_label.setText("Draw Pattern");
                }
            }
            else{
                _pattern=PatternLockUtils.patternToString(mPatternLockView, pattern);
                isPatternConfirm=true;
                pattern_label.setText("Confirm Pattern");
            }
            mPatternLockView.clearPattern();
        }

        @Override
        public void onCleared() {
            Log.d(getClass().getName(), "Pattern has been cleared");
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}
