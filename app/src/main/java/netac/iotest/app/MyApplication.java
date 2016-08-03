package netac.iotest.app;

import android.app.Application;

import netac.iotest.utils.filebroadcast.BroadCastReciverManager;

/**
 * Created by siwei.zhao on 2016/6/30.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BroadCastReciverManager.initManager(this);
    }
}
