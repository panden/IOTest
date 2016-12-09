package netac.fileutilsmaster.file.manager;

import android.support.annotation.IntDef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import netac.fileutilsmaster.file.CustomFile;
import netac.fileutilsmaster.file.FileFactory;
import netac.fileutilsmaster.file.vo.StorageDeviceInfo;
import netac.fileutilsmaster.utils.FileMainCallBackHandler;

/**
 * Created by siwei.zhao on 2016/11/14.
 * 文件剪切板管理，主要是当文件需要进行剪切操作的时候，操作记录就存储在这里
 */

public class FileShearPlateManager {

    private static FileShearPlateManager mShearPlateManager;
    private List<FileSharePalteFile> mFileSharePalteFiles;
    private List<SharePalteChangeListener> mPlateChangeListeners;

    private FileShearPlateManager(){
        mFileSharePalteFiles=new ArrayList<>();
        mPlateChangeListeners=new ArrayList<>();
    };

    private static FileShearPlateManager getInstance(){
        if(mShearPlateManager==null)mShearPlateManager=new FileShearPlateManager();
        return mShearPlateManager;
    }

    /**清空剪切板*/
    public void clearShareFile(){
        mFileSharePalteFiles.clear();
    }

    //添加文件到剪切板
    private boolean addFile(FileSharePalteFile file){
        if(file==null || mFileSharePalteFiles.contains(file))return false;
        mFileSharePalteFiles.add(file);
        return true;
    }

    /**添加文件到剪切板*/
    public boolean addShareFile(FileSharePalteFile file){
        onSharePlateChange(mFileSharePalteFiles, Arrays.asList(file), true);
        return addFile(file);
    }

    /**添加多个文件到剪切板*/
    public boolean addShareFiles(FileSharePalteFile[] files){
        if(files==null || files.length<=0)return false;
        boolean allAdd=true;
        for(FileSharePalteFile file : files)allAdd&=addFile(file);
        onSharePlateChange(mFileSharePalteFiles, Arrays.asList(files), true);
        return allAdd;
    }

    /**添加多个文件到剪切板*/
    public boolean addShareFiles(List<FileSharePalteFile> files){
        if(files==null || files.size()<=0)return false;
        boolean allAdd=true;
        for(FileSharePalteFile file : files)allAdd&=addFile(file);
        onSharePlateChange(mFileSharePalteFiles, files, true);
        return allAdd;
    }

    //剪切板删除文件
    private boolean removeFile(FileSharePalteFile file){
        if(file==null || !mFileSharePalteFiles.contains(file))return false;
        mFileSharePalteFiles.remove(file);
        return true;
    }

    /**剪切板删除文件*/
    private boolean removeShareFile(FileSharePalteFile file){
        onSharePlateChange(mFileSharePalteFiles, Arrays.asList(file), false);
        return removeFile(file);
    }

    /**剪切板删除文件*/
    public boolean removeShareFiles(FileSharePalteFile[] files){
        if(files==null || files.length<=0)return false;
        boolean allRemove=true;
        for(FileSharePalteFile file : files)allRemove&=removeFile(file);
        onSharePlateChange(mFileSharePalteFiles, Arrays.asList(files), false);
        return allRemove;
    }

    /**剪切板删除文件*/
    public boolean removeShareFiles(List<FileSharePalteFile> files){
        if(files==null || files.size()<=0)return false;
        boolean allRemove=true;
        for(FileSharePalteFile file : files)allRemove&=removeFile(file);
        onSharePlateChange(mFileSharePalteFiles, files, false);
        return allRemove;
    }

    /**获取指定类型的剪切板文件*/
    public List<FileSharePalteFile> getSharePalteFiles(@FileSharePalteFile.FileShareType int type){
        List<FileSharePalteFile> files=new ArrayList<>();
        for(FileSharePalteFile file : mFileSharePalteFiles){
            if(file.getFileShareType()==type)files.add(file);
        }
        return files;
    }

    /**获取所有的剪切板内的文件*/
    public List<FileSharePalteFile> getSharePalteFiles(){
        return mFileSharePalteFiles;
    }

    /**注册剪切板内文件改变监听*/
    public boolean registerChangeListener(SharePalteChangeListener listener){
        if(listener==null || mPlateChangeListeners.contains(listener))return false;
        mPlateChangeListeners.add(listener);
        return true;
    }

    /**注册剪切板内文件改变监听*/
    public boolean unregisterChangeListener(SharePalteChangeListener listener){
        if(listener==null || !mPlateChangeListeners.contains(listener))return false;
        mPlateChangeListeners.remove(listener);
        return true;
    }

    //onSharePlateChange在主线程回调
    private void onMainCallBackSharePlateChange(List<FileSharePalteFile> sharePalteFiles, List<FileSharePalteFile> changeFile, boolean isAdd){
        FileMainCallBackHandler.ClassCallBackBuilder builder=FileMainCallBackHandler.createBuilder();
        builder.setObjClazz(FileShearPlateManager.this)
                .setClazz(FileShearPlateManager.class)
                .setMethod("onSharePlateChange")
                .setParameterTypes(List.class, List.class, boolean.class)
                .setParams(sharePalteFiles, changeFile, isAdd);
        FileMainCallBackHandler.getInstance().onMainCallBack(builder);
    }

    //文件剪切板内的文件发生改变
    private void onSharePlateChange(List<FileSharePalteFile> sharePalteFiles, List<FileSharePalteFile> changeFile, boolean isAdd){
        for(SharePalteChangeListener listener : mPlateChangeListeners){
            try {
                if(listener!=null)listener.onSharePlateChange(sharePalteFiles, changeFile, isAdd);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**剪切板改变监听*/
    public interface SharePalteChangeListener {

        /**剪切板文件发生改变
         * @param sharePalteFiles 当前剪切板剩余的文件
         * @param changeFile 当前发生改变的文件
         * @param isAdd 当前改变状态是添加文件还是删除文件
         * */
        void onSharePlateChange(List<FileSharePalteFile> sharePalteFiles, List<FileSharePalteFile> changeFile, boolean isAdd);
    }


    /**文件剪切板信息，主要是存储需要进行剪切的文件信息*/
    public static class FileSharePalteFile {

        /**文件复制*/
        public static final int FILE_COPY=1;

        /**文件剪切*/
        public static final int FILE_CUT=2;

        /**删除*/
        public static final int FILE_DELETE=3;

        @IntDef({FILE_COPY, FILE_CUT, FILE_DELETE})
        public @interface FileShareType{};

        private CustomFile mFile;
        private @FileShareType int  mFileShareType;

        /**获取当前文件存储的存储设备类型*/
        public StorageDeviceInfo.StorageDeviceType getFileStorageType(){
            return FileFactory.getInstance().getFileWrapper().getPathStorageType(mFile.getAbsolutePath());
        };

        /**是否是在同一个存储设备上*/
        public boolean isSameStorage(CustomFile file){
            if(file==null)return false;
            return FileFactory.getInstance().getFileWrapper().getPathStorageType(mFile.getAbsolutePath())==FileFactory.getInstance().getFileWrapper().getPathStorageType(file.getAbsolutePath());
        }

        public CustomFile getFile() {
            return mFile;
        }

        public @FileShareType int getFileShareType() {
            return mFileShareType;
        }

        public FileSharePalteFile(CustomFile file, @FileShareType int fileShareType) {
            mFile = file;
            mFileShareType = fileShareType;
        }
    }

}
