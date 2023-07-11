package learning.com.applock;

import org.litepal.crud.LitePalSupport;

import java.util.Objects;

public class ProtectedApplication extends LitePalSupport {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProtectedApplication)) return false;
        ProtectedApplication that = (ProtectedApplication) o;
        return Objects.equals(getPackageName(), that.getPackageName());
    }

    @Override
    public int hashCode() {
        return 0;
    }
    public ProtectedApplication(){
        this(null);
    }
    public ProtectedApplication(String packageName){
        this.packageName=packageName;
    }
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    String packageName;

}
