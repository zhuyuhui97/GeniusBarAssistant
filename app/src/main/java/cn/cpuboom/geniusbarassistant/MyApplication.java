package cn.cpuboom.geniusbarassistant;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;

import cn.cpuboom.geniusbarassistant.model.FixQueueItemModel;

/**
 * Created by zhuyuhui on 2016/9/4.
 */
public class MyApplication extends Application {
    public FixQueueItemModel currentFix=null;
    String TAG="MyApplication";
    //String srvUrl;

    //public int staffId=10001;

    public void onCreate() {
        // 程序创建的时候执行
        //srvUrl=getString(R.string.json_src);
        super.onCreate();
        PreferenceManager.setDefaultValues(this, R.xml.pref, false);
    }



    public String getUrl(int resId){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.getString("server_domain","");
        String url="http://"+settings.getString("server_domain","")+":8080"+getString(resId);
        return url;
    }

    public int getStaffId(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        return Integer.decode(settings.getString("staff_id",""));
    }

}
