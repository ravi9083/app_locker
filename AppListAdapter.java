package learning.com.applock;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    List<AppInfo> appList;
    Context context;
    OnCallBackListen onCallBackListen;

    AppListAdapter(List<AppInfo> appInfos,OnCallBackListen onCallBackListen) {

        this.appList = appInfos;
this.onCallBackListen=onCallBackListen;
    }
public interface OnCallBackListen{
        void onClick(AppInfo appInfo);
}
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (context == null)
            context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.app_card, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder
                ;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        viewHolder.appLogo.setImageDrawable(appList.get(i).appLogo);
        viewHolder.appName.setText(appList.get(i).appName);
        if (appList.get(i).appStatus)
            viewHolder.appStatus.setImageResource(R.drawable.ic_lock);
        else
            viewHolder.appStatus.setImageResource(R.drawable.ic_lock_open);

        viewHolder.appStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppInfo appInfo=appList.get(i);
                appInfo.setAppStatus(!appInfo.isAppStatus());

                onCallBackListen.onClick(appInfo);
                if (appList.get(i).appStatus) {
                  //  appList.get(i).appStatus=false;
                    viewHolder.appStatus.setImageResource(R.drawable.ic_lock_open);
                }
                else {
                  //  appList.get(i).appStatus=true;

                    viewHolder.appStatus.setImageResource(R.drawable.ic_lock);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appLogo, appStatus;
        TextView appName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            appName = itemView.findViewById(R.id.app_name);
            appStatus = itemView.findViewById(R.id.app_status);
            appLogo = itemView.findViewById(R.id.app_logo);

        }
    }
}
