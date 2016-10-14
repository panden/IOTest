package netac.fileutilsmaster.app;

import android.app.Application;

import netac.fileutilsmaster.utils.MasterUtils;

/**
 * Created by siwei.zhao on 2016/9/14.
 * 继承该Application或者在自己的Application.onCreate中调用MasterUtils.initMaster(Application);
 */
public class FileMasterApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        MasterUtils.initMaster(this);
        MasterUtils.debug(true, "FileMasterLib");
    }
}
