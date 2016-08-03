package netac.iotest.utils.file;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import java.util.List;

/**
 * Created by siwei.zhao on 2016/6/29.
 */
public class CustomFile extends FileCommon{

    private static Context sContext;

    protected FileCommon mFile;

    public CustomFile(String path) {
        super(path);
        mFile=createFile(path);
    }

    @Override
    public boolean delete() {
        return mFile.delete();
    }

    @Override
    public List<FileCommon> listFiles() {
        return mFile.listFiles();
    }

    @Override
    public List<FileCommon> listFiles(FileFilter filter) {
        return mFile.listFiles(filter);
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public String getAbsultPath() {
        return mFile.getAbsultPath();
    }

    @Override
    public boolean deleteExists() {
        return mFile.deleteExists();
    }

    @Override
    public boolean isFile() {
        return mFile.isFile();
    }

    @Override
    public boolean isDirectory() {
        return mFile.isDirectory();
    }

    @Override
    public long length() {
        return mFile.length();
    }

    @Override
    public long lastModify() {
        return mFile.lastModify();
    }

    @Override
    public boolean mkdirs() {
        return mFile.mkdirs();
    }

    @Override
    public boolean canReader() {
        return mFile.canReader();
    }

    @Override
    public boolean canWriter() {
        return mFile.canWriter();
    }

    @Override
    public boolean createNewFile() {
        return mFile.createNewFile();
    }

    @Override
    public boolean exists() {
        return mFile.exists();
    }

    @Override
    public String getParent() {
        return mFile.getParent();
    }

    @Override
    public FileCommon getParentFile() {
        return mFile.getParentFile();
    }

    @Override
    public Bitmap getFileIco() {
        return null;
    }

    public static void initCustomFile(Application app){
        sContext=app.getApplicationContext();
    }

    public static Context getContext(){
        if(sContext==null)throw  new IllegalStateException("CustomFile not init, Please init CustomFile in Application.");
        return sContext;
    }


    protected FileCommon createFile(String path){
        FileCommon fileCommon=null;
        if(isExtraDevice(path) && isKitKatMore()){
            //是外置存储，而且当前在Android4.4之上
            fileCommon=new ExtrageFile(path);
        }else{
            //内置存储，或者外置存储（Android版本在4.4之下）
            fileCommon=new LocalFile(path);
        }
        return fileCommon;
    }

    //是否是外置存储设备
    private boolean isExtraDevice(String path){
        if(path.startsWith(System.getenv("ANDROID_STORAGE"))){//外置存储设备
            return true;
        }else{//内置存储设备
            return false;
        }
    }

    //判断当前文件路径是否为USBOTG设备
    private boolean isUsbOTG(String path){
        return false;
    }

    /**判断当前版本是否在Android4.4之上*/
    private boolean isKitKatMore(){
        return Build.VERSION.SDK_INT>=19;
    }

    /**获取内置存储路径*/
    public static String getExternalStorage(){
        return System.getenv("EXTERNAL_STORAGE");
    }

    /**获取外置存储卡路径*/
    public static String getSecondaryStorage(){
        return System.getenv("SECONDARY_STORAGE");
    }

}
