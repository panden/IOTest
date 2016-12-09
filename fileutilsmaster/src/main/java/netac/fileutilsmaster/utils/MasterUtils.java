package netac.fileutilsmaster.utils;

import android.app.Application;

import netac.fileutilsmaster.filebroadcast.BroadCastReciverManager;
import netac.fileutilsmaster.filebroadcast.DeviceBroadcast;
import netac.fileutilsmaster.filebroadcast.UsbDeviceBroadcast;

/**
 * Created by siwei.zhao on 2016/9/14.
 * 库初始化工具类
 */
public class MasterUtils {

    private static Application mApplication;
    private static DeviceBroadcast sDeviceBroadcast;
    private static UsbDeviceBroadcast sUsbDeviceBroadcast;

    /**初始化相关库*/
    public static void initMaster(Application application){
        BroadCastReciverManager.initManager(application);
        ResourceUtils.initResourece(application);
        sDeviceBroadcast=new DeviceBroadcast();
        sDeviceBroadcast.registerBroadCastReciver();
        sUsbDeviceBroadcast=new UsbDeviceBroadcast();
        sUsbDeviceBroadcast.registerBroadCastReciver();
        mApplication=application;
    }

    /**库注销（对已注册的一些广播进行注销）*/
    public static void deinitMaster(){
        if(sDeviceBroadcast!=null){
            sDeviceBroadcast.unRegisterBroadCastReciver();
            sDeviceBroadcast=null;
        }
        if(sUsbDeviceBroadcast!=null){
            sUsbDeviceBroadcast.unRegisterBroadCastReciver();
            sUsbDeviceBroadcast=null;
        }
    }

    /**是否开启debug*/
    public static void debug(boolean open){
        Logger.debug(open);
    }

    private MasterUtils(){
    }

    private static MasterUtils sMasterUtils;

    public static MasterUtils getInstance(){
        if(sMasterUtils==null)sMasterUtils=new MasterUtils();
        return sMasterUtils;
    }

    public Application getApp() {
        return mApplication;
    }
}
