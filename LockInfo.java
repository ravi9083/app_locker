package learning.com.applock;

import org.litepal.crud.LitePalSupport;

public class LockInfo extends LitePalSupport {
    String lockString;
    int lockType;

    public String getLockString() {
        return lockString;
    }

    public void setLockString(String lockString) {
        this.lockString = lockString;
    }

    public int getLockType() {
        return lockType;
    }

    public void setLockType(int lockType) {
        this.lockType = lockType;
    }
}
