package learning.com.applock;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import org.litepal.LitePal;

import javax.xml.transform.Result;

public class PinLock extends Activity {
    PinLockView mPinLockView;
    IndicatorDots mIndicatorDots;
    String _pin="";
    Boolean isPinConfirm=false;
    TextView enter_pin;
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
        setContentView(R.layout.activity_pin_lock);
        Intent intent=new Intent(this,MyService.class);
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        mPinLockView.setPinLockListener(mPinLockListener);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        enter_pin = (TextView) findViewById(R.id.enter_pin);
        mIndicatorDots.setPinLength(0);
        //mPinLockView.attachIndicatorDots(mIndicatorDots);
findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
});
    }
    private PinLockListener mPinLockListener = new PinLockListener() {
        @Override
        public void onComplete(String pin) {
            Log.d("PinLock", "Pin complete: " + pin);
            if(mServiceBinder.getLockType()==2) {
                LockInfo lockInfo = LitePal.findFirst(LockInfo.class);
                if (pin.equalsIgnoreCase(lockInfo.lockString)) {
                    if (AppLockActivity.isLaunchByUser)
                        startActivity(new Intent(PinLock.this, MainActivity.class));
                    else
                        mServiceBinder.addUnlockedApp();
                    finish();
                }
            }
            else if(isPinConfirm) {

                if (_pin.equalsIgnoreCase(pin)) {
                    isPinConfirm=false;
                    mServiceBinder.saveLockInfo(_pin,2);
                    setResult(RESULT_OK);
                    finish();

                } else {
                    Toast.makeText(PinLock.this, "Pin Mismatch. Try Again.", Toast.LENGTH_SHORT).show();
mIndicatorDots.setPinLength(0);
mPinLockView.resetPinLockView();
                    isPinConfirm=false;
                    enter_pin.setText("Enter PIN");

                }

            }
            else{
                _pin=pin;
                isPinConfirm=true;
                mIndicatorDots.setPinLength(0);
                mPinLockView.resetPinLockView();

                enter_pin.setText("Confirm PIN");
            }
        }

        @Override
        public void onEmpty() {
            Log.d("PinLock", "Pin empty");
            mIndicatorDots.setPinLength(0);
        }

        @Override
        public void onPinChange(int pinLength, String intermediatePin) {
            Log.d("PinLock", "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
            mIndicatorDots.setPinLength(pinLength);
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}
