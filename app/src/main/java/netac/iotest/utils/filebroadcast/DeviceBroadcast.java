package netac.iotest.utils.filebroadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by siwei.zhao on 2016/6/30.
 */
public class DeviceBroadcast extends BroadCastReciverManager.BroadCastReciverCommon {

    public static final String ACTION_TEST="action_test";
    @Override
    public IntentFilter getRegisterIntentFilter() {
        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        return filter;
    }

    @Override
    public IntentFilter getLocalRegisterIntentFilter() {
        IntentFilter filter=new IntentFilter();
        filter.addAction(ACTION_TEST);
        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action=intent.getAction();
        if(action.equals(Intent.ACTION_MEDIA_EJECT)){
            //USB设备移除

        }else if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){

        }else if(action.equals(Intent.ACTION_MEDIA_REMOVED)){

        }else if(action.equals(Intent.ACTION_MEDIA_UNMOUNTED)){
            //USB设备挂载
        }
    }
}
