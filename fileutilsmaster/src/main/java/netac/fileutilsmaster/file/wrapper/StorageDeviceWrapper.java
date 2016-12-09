package netac.fileutilsmaster.file.wrapper;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import netac.fileutilsmaster.file.vo.StorageDeviceInfo;
import netac.fileutilsmaster.filebroadcast.BroadCastReciverManager;
import netac.fileutilsmaster.filebroadcast.DeviceBroadcast;
import netac.fileutilsmaster.utils.DocumentTreePermissionUtils;
import netac.fileutilsmaster.utils.Logger;

import static java.lang.System.getenv;

/**
 * Created by siwei.zhao on 2016/9/14.
 */
public class StorageDeviceWrapper {

    private Map<StorageDeviceInfo.StorageDeviceType, StorageDeviceInfo> mStorageDeviceInfoMap;
    private List<StorageDeviceChangeListener> mChangeListeners;
    private DocumentFile mSecondExtrageRootFile;
    private DocumentFile mUsbDeviceExtrageRootFile;

    public StorageDeviceWrapper(Context context){
        mStorageDeviceInfoMap=new HashMap<>();
        mChangeListeners=new ArrayList<>();

        initStorageDeviceData(context);

        BroadCastReciverManager manager=BroadCastReciverManager.getManager();
        manager.getBroadCastReciver(DeviceBroadcast.class).registerBroadCastListener(mBroadCastCallBack);
    }


    /**初始化已挂在的所有存储设备的信息,以及对应的权限信息*/
    private void initStorageDeviceData(Context context){
        mStorageDeviceInfoMap.clear();
        //初始化手机存储信息
        initExtrage(context);
        //初始化外置存储设备
        initExtrageSecond(context);
        //初始化usb存储设备
        initExtrageUsb(context);
        //初始化存储设备信息
        initStorageInfo(context);

        //初始化权限信息
        initDocumentTreeUriPermission(context);
    }

    private void initExtrage(Context context){
        //初始化手机存储信息
        StorageDeviceInfo extrage=new StorageDeviceInfo();
        extrage.setDeviceType(StorageDeviceInfo.StorageDeviceType.ExtrageDevice);
        extrage.setRootPath(getStoragePath12(context, false).get(0));
        getSpaceInfo(extrage.getRootPath(), extrage.getCapacity(), extrage.getUsedSpace(), extrage.getFreeSpace());
        Logger.d("extrage="+extrage.getRootPath());
        mStorageDeviceInfoMap.put(StorageDeviceInfo.StorageDeviceType.ExtrageDevice, extrage);
    }

    private void initExtrageSecond(Context context){
        //判断外置sd卡是否存在
        String secondExtragePath=getSecondExtragePath(context);
        if(secondExtragePath!=null){
            StorageDeviceInfo extrageSecond=new StorageDeviceInfo();
            extrageSecond.setDeviceType(StorageDeviceInfo.StorageDeviceType.SecondExtrageDevice);
            extrageSecond.setRootPath(secondExtragePath);
            getSpaceInfo(extrageSecond.getRootPath(), extrageSecond.getCapacity(), extrageSecond.getUsedSpace(), extrageSecond.getFreeSpace());
            Logger.d("extrageSecond="+extrageSecond.getRootPath());
            mStorageDeviceInfoMap.put(StorageDeviceInfo.StorageDeviceType.SecondExtrageDevice, extrageSecond);
        }
    }

    private void initExtrageUsb(Context context){
        //判断usb存储设备是否存在
        if(usbDeviceExists(context)){
            String usbDevicePath=getUsbDevicePath(context);
            StorageDeviceInfo extrageUsb=new StorageDeviceInfo();
            extrageUsb.setDeviceType(StorageDeviceInfo.StorageDeviceType.UsbDevice);
            extrageUsb.setRootPath(usbDevicePath);
            getSpaceInfo(extrageUsb.getRootPath(), extrageUsb.getCapacity(), extrageUsb.getUsedSpace(), extrageUsb.getFreeSpace());
            Logger.d("extrageUsb="+extrageUsb.getRootPath());
            mStorageDeviceInfoMap.put(StorageDeviceInfo.StorageDeviceType.UsbDevice, extrageUsb);
        }
    }

    //初始化存储设备信息,storageid
    private void initStorageInfo(Context context){
        try {
            Class storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            StorageManager storageManager= (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumeList=storageManager.getClass().getMethod("getVolumeList");
            Method isPrimary=storageVolumeClazz.getMethod("isPrimary");
            Method isEmulated=storageVolumeClazz.getMethod("isEmulated");
            Method getStorageId=storageVolumeClazz.getMethod("getStorageId");
            Method getPathFile=storageVolumeClazz.getMethod("getPathFile");
            Object result = getVolumeList.invoke(storageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                int mStorageId= (int) getStorageId.invoke(storageVolumeElement);
                File file= (File) getPathFile.invoke(storageVolumeElement);
                StorageDeviceInfo.StorageDeviceType type=getPathStorageType(file.getAbsolutePath());
                Logger.d("path=%s storageid=%s", file.getAbsolutePath(), String.valueOf(mStorageId));
                if(type!= StorageDeviceInfo.StorageDeviceType.UnKnow){
                    mStorageDeviceInfoMap.get(type).setStorageId(String.valueOf(mStorageId));
                    Logger.d("path=%s type=%s storageid=%s", file.getAbsolutePath(), String.valueOf(type), String.valueOf(mStorageId));
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    /**初始化权限,19以上才会执行该方法*/
    private void  initDocumentTreeUriPermission(Context context){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT)return;
        StorageDeviceInfo[] deviceInfos=new StorageDeviceInfo[mStorageDeviceInfoMap.size()];
        Iterator iterator=mStorageDeviceInfoMap.values().iterator();
        int position=0;
        while(iterator.hasNext()){
            deviceInfos[position]= (StorageDeviceInfo) iterator.next();
            position++;
        }
        //更新以获取的权限信息
        DocumentTreePermissionUtils.getInstance().initDocumentPermission(deviceInfos, context);
    }

    BroadCastReciverManager.BroadCastCallBack mBroadCastCallBack=new BroadCastReciverManager.BroadCastCallBack() {


        @Override
        public void onReceiver(Context context, Intent intent) {
            String action=intent.getAction();

            if(Intent.ACTION_MEDIA_MOUNTED.equals(action)){//存储设备挂载
                String path=intent.getData().getPath().replace("file://", "");

                //重新初始化磁盘信息,以及对应的权限信息
                initStorageDeviceData(context);
                StorageDeviceInfo.StorageDeviceType type= StorageDeviceInfo.StorageDeviceType.UnKnow;
                String secondExtrage=getSecondExtragePath(context);
                String usb=getUsbDevicePath(context);
                if(secondExtrage!=null && path.startsWith(secondExtrage)){
                    type= StorageDeviceInfo.StorageDeviceType.SecondExtrageDevice;
                }else if(usbDeviceExists(context) && usb!=null && path.startsWith(usb)){
                    type= StorageDeviceInfo.StorageDeviceType.UsbDevice;
                }
                onStorageMountedStatusChanged(true, type);
                //存储信息改变改变回调
                onStorageChanged();

            }else if(Intent.ACTION_MEDIA_EJECT.equals(action)){//存储设备被移除

                String path=intent.getData().getPath();
                StorageDeviceInfo.StorageDeviceType type=getPathStorageType(path);
                mStorageDeviceInfoMap.remove(type);

                //回调存储设备挂载状态改变
                onStorageMountedStatusChanged(false, type);
                //回调存储设备信息改变
                onStorageChanged();

            }
        }
    };

    /**注册存储设备状态改变监听*/
    public void registerStorageDeviceChanged(StorageDeviceChangeListener listener){
        if(listener==null)return;
        if(!mChangeListeners.contains(listener)){
            List<StorageDeviceInfo> infos=new ArrayList<StorageDeviceInfo>();
            infos.addAll(mStorageDeviceInfoMap.values());
            listener.onStorageDeviceChanged(infos);
            mChangeListeners.add(listener);
        }
    }

    /**注销存储设备状态改变监听*/
    public void unRegisterStorageDeviceChanged(StorageDeviceChangeListener listener){
        if(listener==null)return;
        if(mChangeListeners.contains(listener))mChangeListeners.remove(listener);
    }

    //注销所有
    private void unRegisterStorageDeviceChangedAll(){
        mChangeListeners.clear();
    }

    //回调存储设备挂载状态改变
    private void onStorageMountedStatusChanged(boolean isMounted, StorageDeviceInfo.StorageDeviceType storageDeviceType){
        for(StorageDeviceChangeListener listener : mChangeListeners){
            try {
                listener.onDeviceMountedStatusChanged(isMounted, storageDeviceType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //回调存储设备装改改变
    private void onStorageChanged(){
        List<StorageDeviceInfo> infos=new ArrayList<StorageDeviceInfo>();
        infos.addAll(mStorageDeviceInfoMap.values());
        for(StorageDeviceChangeListener listener : mChangeListeners){
            try {
                listener.onStorageDeviceChanged(infos);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //存储设备状态改变监听
    public interface StorageDeviceChangeListener{

        /**设备挂载状态改变*/
        void onDeviceMountedStatusChanged(boolean isMounted, StorageDeviceInfo.StorageDeviceType storageDeviceType);

        /**挂载存储设备改变*/
        void onStorageDeviceChanged(List<StorageDeviceInfo> deviceInfos);

    }

    /**获取指定设备的存储信息*/
    public @Nullable StorageDeviceInfo getStorageDevice(StorageDeviceInfo.StorageDeviceType type){
        return mStorageDeviceInfoMap.get(type);
    }

    //获取外置SD卡的路径
    private String getSecondExtragePath(Context context){
        String secondExtragePath=null;
        if(Build.VERSION.SDK_INT<23){
            secondExtragePath= System.getenv("SECONDARY_STORAGE");
        }else{
            secondExtragePath=getStoragePath23(context, false);
        }
        return secondExtragePath;
    }

    //获取usb设备的路径
    private String getUsbDevicePath(Context context){
        String usbPath="";
        if(Build.VERSION.SDK_INT>=23){//Android 6.0
            usbPath=getStoragePath23(context, true);
        }else{//Android 6.0以下
            if(Build.VERSION.SDK_INT<12)return usbPath;//Android 3.1以上
            String extrageSecondPath = getenv("SECONDARY_STORAGE");
            String extragePath= getStoragePath12(context, false).get(0);
            List<String> mountPaths=getStoragePath12(context, true);
            for(String p: mountPaths){
                if(!p.equals(extragePath) && !p.equals(extrageSecondPath) && !p.startsWith(getenv("EMULATED_STORAGE_TARGET"))){
                    usbPath=p;
                    Logger.d("getUsbDevicePath usb path="+p);
                    return usbPath;
                }
            }
        }

        return usbPath;
    }

    //判断usb存储设备是否存在
    private boolean usbDeviceExists(Context context){
        UsbManager manager= (UsbManager) context.getSystemService(Context.USB_SERVICE);
        Collection<UsbDevice> devices=manager.getDeviceList().values();
        while (devices.iterator().hasNext()){
            UsbDevice device=devices.iterator().next();
            for(int i=0; i<device.getInterfaceCount(); i++){
                UsbInterface usbInterface=device.getInterface(i);
                Logger.d("getInterfaceClass="+device.getDeviceClass());
                if(usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_MASS_STORAGE){
                    return true;
                }
            }

        }
        return false;
    }

    //获取容量信息
    private void getSpaceInfo(String path, long cap, long used, long free){
        StatFs fs=new StatFs(path);
        long blockSize, totalBlock, availableBlocks;
        if(Build.VERSION.SDK_INT>=18){//Android 4.3以上
            blockSize=fs.getBlockSizeLong();
            totalBlock=fs.getBlockCountLong();
            availableBlocks=fs.getAvailableBlocksLong();
        }else{//Android4.3以下
            blockSize=fs.getBlockSize();
            totalBlock=fs.getBlockCount();
            availableBlocks=fs.getAvailableBlocks();
        }
        cap=blockSize*totalBlock;
        free=blockSize*availableBlocks;
        used=cap-free;
    }

    /**判断当前目录是否为根目录*/
    public boolean isRootPath(String path){
        StorageDeviceInfo.StorageDeviceType type=getPathStorageType(path);
        StorageDeviceInfo info=getStorageDevice(type);
        if(info==null)return false;
        return info.getRootPath().equals(path);
    }

    /**判断当前路径的是挂载到那个盘下*/
    public StorageDeviceInfo.StorageDeviceType getPathStorageType(String path){
        StorageDeviceInfo.StorageDeviceType deviceType=StorageDeviceInfo.StorageDeviceType.UnKnow;
        if(path==null || TextUtils.isEmpty(path))return deviceType;
        Collection<StorageDeviceInfo> infos=mStorageDeviceInfoMap.values();
        Iterator iterator=infos.iterator();
        //storage目录
        while(iterator.hasNext()){
            StorageDeviceInfo s= (StorageDeviceInfo) iterator.next();
            if(path.startsWith(s.getRootPath())){
                deviceType = s.getDeviceType();
                break;
            }
        }

        //emulated目录
        if(System.getenv().containsKey("EMULATED_STORAGE_TARGET")){
            if(path.toLowerCase().startsWith(System.getenv("EMULATED_STORAGE_TARGET").toLowerCase()))deviceType = StorageDeviceInfo.StorageDeviceType.ExtrageDevice;
        }else if(System.getenv().containsKey("ENC_EMULATED_STORAGE_TARGET")){
            if(path.toLowerCase().startsWith(System.getenv("ENC_EMULATED_STORAGE_TARGET").toLowerCase()))deviceType = StorageDeviceInfo.StorageDeviceType.ExtrageDevice;
        }else if (System.getenv().containsKey("ANDROID_STORAGE")){
            if(path.toLowerCase().startsWith((System.getenv("ANDROID_STORAGE")+"/emulated").toLowerCase()))deviceType = StorageDeviceInfo.StorageDeviceType.ExtrageDevice;
        }

        //Logger.d("path=%s  getPathStorageType=%s", path, deviceType);

        return deviceType;
    }

    //API 23 Android 6.0 获取外置SD卡和USB设备的路径,能获取外置和usb设备的路径
    private String getStoragePath23(Context context, boolean isUsb){

        String path="";

        if(Build.VERSION.SDK_INT<23)return path;


        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> volumeInfoClazz=null;
        Class<?> diskInfoClaszz=null;

        try {
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            diskInfoClaszz = Class.forName("android.os.storage.DiskInfo");

            Method StorageManager_getVolumes=Class.forName("android.os.storage.StorageManager").getMethod("getVolumes");
            Method VolumeInfo_GetDisk = volumeInfoClazz.getMethod("getDisk");
            Method VolumeInfo_GetPath = volumeInfoClazz.getMethod("getPath");
            Method DiskInfo_IsUsb = diskInfoClaszz.getMethod("isUsb");
            Method DiskInfo_IsSd = diskInfoClaszz.getMethod("isSd");

            List<Object> List_VolumeInfo = (List<Object>) StorageManager_getVolumes.invoke(mStorageManager);
            for(int i=0; i<List_VolumeInfo.size(); i++){
                Object volumeInfo = List_VolumeInfo.get(i);
                Object diskInfo = VolumeInfo_GetDisk.invoke(volumeInfo);


                if(diskInfo==null)continue;

                boolean sd= (boolean) DiskInfo_IsSd.invoke(diskInfo);
                boolean usb= (boolean) DiskInfo_IsUsb.invoke(diskInfo);

                File file= (File) VolumeInfo_GetPath.invoke(volumeInfo);
               // Logger.d("diskinfo="+file.getAbsolutePath()+"; is_usb="+usb+";  is_sd="+sd);

                if(isUsb == usb){//usb
                    path=file.getAbsolutePath();
                    break;
                }else if(!isUsb == sd){//sd
                    path=file.getAbsolutePath();
                    break;
                }

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return path;
    }


    //获取外置SD卡路径 API 12以上 Android 3.1,能获取内置和外置的路径和USB存储设备的路径
    private List<String> getStoragePath12(Context mContext, boolean removeAble) {

        if(Build.VERSION.SDK_INT<12)return null;
        List<String> paths=new ArrayList<>();
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;


        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method getStorageId=storageVolumeClazz.getMethod("getStorageId");

            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            //for(int i = 0; i < length; i++) System.out.println("result data="+Array.get(result, i));
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                int id= (int) getStorageId.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removeAble == removable) {
                    //Logger.i("getpath="+path+"; is_extra="+removeAble+" allLen="+length);
                    paths.add(path);
                }else{
                    paths.add(path);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return paths;
    }





    /**获取根目录文件*/
    public DocumentFile getStorageDocumentFile(StorageDeviceInfo.StorageDeviceType type){
        DocumentFile documentFile=null;
        switch (type){
            case SecondExtrageDevice:
                documentFile=mSecondExtrageRootFile;
                break;
            case UsbDevice:
                documentFile=mUsbDeviceExtrageRootFile;
                break;
        }
        return documentFile;
    };
}
