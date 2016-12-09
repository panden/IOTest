package netac.fileutilsmaster.file;

import android.content.Context;
import android.os.Build;

import netac.fileutilsmaster.file.wrapper.StorageDeviceWrapper;
import netac.fileutilsmaster.utils.Logger;
import netac.fileutilsmaster.utils.MasterUtils;

/**
 * Created by siwei.zhao on 2016/9/13.
 */
public class FileFactory {

    private static FileFactory mFileFactory;
    private StorageDeviceWrapper mFileWrapper;
    private Context mContext;

    private FileFactory(Context context){
        mFileWrapper=new StorageDeviceWrapper(context);
        mContext=context;
    };

    public static FileFactory getInstance(){
        if(mFileFactory==null)mFileFactory=new FileFactory(MasterUtils.getInstance().getApp());
        return mFileFactory;
    }

    public Context getContext() {
        return mContext;
    }

    public FileCommon createFile(String path, String name){
        String filePath;
        if(path.endsWith("/"))filePath=path+name;
        else filePath=path+"/"+name;
        return createFile(filePath);
    }

    public FileCommon createFile(String path){
        FileCommon fileCommon=null;
        switch (mFileWrapper.getPathStorageType(path)){
            case ExtrageDevice:
                fileCommon=getExtrageDeviceFile(path);
                break;
            case SecondExtrageDevice:
                fileCommon=getExtrageSecondDeviceFile(path);
                break;
            case UsbDevice:
                fileCommon=getExtrageSecondDeviceFile(path);
                break;
            case UnKnow:
                break;
        }
        return fileCommon;
    }

    public StorageDeviceWrapper getFileWrapper() {
        return mFileWrapper;
    }

    //获取手机的文件
    private FileCommon getExtrageDeviceFile(String path){
        Logger.i("getExtrageDeviceFile path=%s;", path);
        return new LocalFile(path);
    }

    //获取外置存储设备的文件
    private FileCommon getExtrageSecondDeviceFile(String path){
        FileCommon fileCommon=null;
        if (Build.VERSION.SDK_INT >= 19) {//Android 4.4
            fileCommon=new ExtrageFile(path);
        }else{
            fileCommon=new LocalFile(path);
        }
        return fileCommon;
    };

}
