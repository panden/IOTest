package netac.fileutilsmaster.file.wrapper;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import netac.fileutilsmaster.file.FileFactory;
import netac.fileutilsmaster.file.vo.StorageDeviceInfo;
import netac.fileutilsmaster.filebroadcast.BroadCastReciverManager;
import netac.fileutilsmaster.filebroadcast.DeviceBroadcast;
import netac.fileutilsmaster.utils.Logger;

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

    private void initStorageDeviceData(Context context){
        mStorageDeviceInfoMap.clear();
        //初始化手机存储信息
        initExtrage(context);
        //初始化外置存储设备
        initExtrageSecond(context);
        //初始化usb存储设备
        initExtrageUsb(context);
    }

    private void initExtrage(Context context){
        //初始化手机存储信息
        StorageDeviceInfo extrage=new StorageDeviceInfo();
        extrage.setDeviceType(StorageDeviceInfo.StorageDeviceType.ExtrageDevice);
        extrage.setRootPath(getStoragePath12(context, false).get(0));
        getSpaceInfo(extrage.getRootPath(), extrage.getCapacity(), extrage.getUsedSpace(), extrage.getFreeSpace());
        System.out.println("extrage="+extrage.getRootPath());
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
            System.out.println("extrageSecond="+extrageSecond.getRootPath());
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
            System.out.println("extrageUsb="+extrageUsb.getRootPath());
            mStorageDeviceInfoMap.put(StorageDeviceInfo.StorageDeviceType.UsbDevice, extrageUsb);
        }
    }

    BroadCastReciverManager.BroadCastCallBack mBroadCastCallBack=new BroadCastReciverManager.BroadCastCallBack() {


        @Override
        public void onReceiver(Context context, Intent intent) {
            String action=intent.getAction();

            if(Intent.ACTION_MEDIA_MOUNTED.equals(action)){//存储设备挂载
                String path=intent.getData().getPath().replace("file://", "");

                //重新初始化磁盘信息
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
    public StorageDeviceInfo getStorageDevice(StorageDeviceInfo.StorageDeviceType type){
        return mStorageDeviceInfoMap.get(type);
    }

    //获取外置SD卡的路径
    private String getSecondExtragePath(Context context){
        String secondExtragePath=null;
        if(Build.VERSION.SDK_INT<23){
            secondExtragePath=System.getenv("SECONDARY_STORAGE");
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
        }else{
            if(Build.VERSION.SDK_INT<12)return usbPath;//Android 3.1以上
            String extrageSecondPath = System.getenv("SECONDARY_STORAGE");
            String extragePath= getStoragePath12(context, false).get(0);
            List<String> mountPaths=getStoragePath12(context, true);
            for(String p: mountPaths){
                if(!p.equals(extragePath) && !p.equals(extrageSecondPath) && !p.startsWith(System.getenv("EMULATED_STORAGE_TARGET"))){
                    usbPath=p;
                    System.out.println("usb path="+p);
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
                System.out.println("getInterfaceClass="+device.getDeviceClass());
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
        if(Build.VERSION.SDK_INT>=18){//Android 4.3
            blockSize=fs.getBlockSizeLong();
            totalBlock=fs.getBlockCountLong();
            availableBlocks=fs.getAvailableBlocksLong();
        }else{
            blockSize=fs.getBlockSize();
            totalBlock=fs.getBlockCount();
            availableBlocks=fs.getAvailableBlocks();
        }
        cap=blockSize*totalBlock;
        free=blockSize*availableBlocks;
        used=cap-free;
    }


    /**判断当前路径的是挂载到那个盘下*/
    public StorageDeviceInfo.StorageDeviceType getPathStorageType(String path){
        if(path==null || TextUtils.isEmpty(path))return StorageDeviceInfo.StorageDeviceType.UnKnow;
        Collection<StorageDeviceInfo> infos=mStorageDeviceInfoMap.values();
        Iterator iterator=infos.iterator();
        //storage目录
        while(iterator.hasNext()){
            StorageDeviceInfo s= (StorageDeviceInfo) iterator.next();
            if(path.startsWith(s.getRootPath())){
                return s.getDeviceType();
            }
        }
        //emulated目录
        if(path.startsWith(System.getenv("EMULATED_STORAGE_TARGET")))return StorageDeviceInfo.StorageDeviceType.ExtrageDevice;

        return StorageDeviceInfo.StorageDeviceType.UnKnow;
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
                System.out.println("diskinfo="+file.getAbsolutePath()+"; is_usb="+usb+";  is_sd="+sd);

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

    //获取所有的挂载路径
    private List<String> getMountPaths(){
        List<String> mountPaths=new ArrayList<>();
        try {
            String storageKey="ANDROID_STORAGE";
            String storageTagKey="EMULATED_STORAGE_TARGET";
            Map<String, String> maps=System.getenv();
            if(maps.containsKey(storageKey) && maps.containsKey(storageTagKey)){
                //获取android存储设备的挂载目录
                String stroagePath=maps.get(storageKey);
                String storageTagPath=maps.get(storageTagKey);
                //执行df统计指令获取所有的挂载点
                Process process=new ProcessBuilder().command("df").redirectErrorStream(true).start();
                process.waitFor();
                BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream()));
                while(reader.read()!=-1){
                    //readline会去掉头部的/,在前面加上被去掉的/
                    String s="/"+reader.readLine();
                    if(s.toLowerCase().startsWith(stroagePath.toLowerCase())){
                        s=s.substring(0, s.indexOf(" "));
                        if(s.toLowerCase().equals(storageTagPath.toLowerCase()) || mountPaths.contains(s))continue;
                        //去除重复的路径,去除相同的父目录
                        System.out.println("add path="+s);
                        mountPaths.add(s);
                    }

                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mountPaths;
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
            for(int i = 0; i < length; i++) System.out.println("result data="+Array.get(result, i));
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                int id= (int) getStorageId.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removeAble == removable) {
                    Logger.i("getpath="+path+"; is_extra="+removeAble+" allLen="+length);
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

    /**设置根目录文件*/
    public void setStorageRootDocumentFile(StorageDeviceInfo.StorageDeviceType type, Uri uri){
        DocumentFile documentFile=null;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)documentFile=DocumentFile.fromTreeUri(FileFactory.getInstance().getContext(), uri);
        else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT)documentFile=DocumentFile.fromSingleUri(FileFactory.getInstance().getContext(), uri);
        switch (type){
            case SecondExtrageDevice:
                mSecondExtrageRootFile=documentFile;
                break;
            case UsbDevice:
                mUsbDeviceExtrageRootFile=documentFile;
                break;
        }
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
