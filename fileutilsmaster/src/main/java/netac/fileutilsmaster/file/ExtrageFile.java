package netac.fileutilsmaster.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import netac.fileutilsmaster.file.vo.StorageDeviceInfo;
import netac.fileutilsmaster.utils.GetPathFromUri4kitkat;
import netac.fileutilsmaster.utils.Logger;

/**
 * Created by siwei.zhao on 2016/6/29.
 */
public class ExtrageFile extends FileCommon {

    protected DocumentFile mFile;
    protected File mLocalFile;


    public ExtrageFile(String path) {
        super(path);
        this.path=path;
        this.mLocalFile=new File(path);
    }

    public ExtrageFile(Uri uri){
        super("");
        this.path= getPathByTreeUri(uri);
        this.mLocalFile=new File(path);
    }

    public ExtrageFile(File file){
        super(file.getAbsolutePath());
        this.path=file.getAbsolutePath();
        this.mLocalFile=file;
    }


    public ExtrageFile(DocumentFile file){
        super("");
        this.path= getPathByTreeUri(file.getUri());
        this.mLocalFile=new File(path);
    }

    /**获取DocumentFile*/
    protected DocumentFile getDocFile(){
        if(mFile==null)mFile=getFileByPath(path);
        return mFile;
    }

    /**获取DocumentFile，如果目录不存在则创建*/
    protected DocumentFile getDocFileNotExists(){
        if(mFile==null){
            StorageDeviceInfo.StorageDeviceType type=FileFactory.getInstance().getFileWrapper().getPathStorageType(path);
            mFile=getCreateFileByPath(FileFactory.getInstance().getContext(), type, path);
        }
        return mFile;
    }

    //根据路径获取file
    private DocumentFile getFileByPath(String path){
        StorageDeviceInfo.StorageDeviceType type=FileFactory.getInstance().getFileWrapper().getPathStorageType(path);
        DocumentFile file=getDocumentFileByPath(FileFactory.getInstance().getContext(), type, path);
        return file;
    }

    //根据路径，如果路径不存在，则创建
    private DocumentFile getCreateFileByPath(Context context, StorageDeviceInfo.StorageDeviceType type, String filePath){
        String rootPath=FileFactory.getInstance().getFileWrapper().getStorageDevice(type).getRootPath();
        DocumentFile documentFile=FileFactory.getInstance().getFileWrapper().getStorageDocumentFile(type);
        String[] rootPaths=rootPath.split("/");
        String[] paths=filePath.split("/");
        File createFile;
        if(paths.length>=rootPaths.length){
            for(int i=0; i<paths.length-rootPaths.length; i++){
                //documentFile.getParentFile只能获取到在用户授权的范围内的文件，否则就会报错
                String name=paths[rootPaths.length+i];
                rootPath+="/"+name;
                createFile=new File(rootPath);
                Logger.i("file path="+createFile.getAbsolutePath()+" is file="+createFile.isFile());
                if(createFile.exists()){
                    documentFile=documentFile.findFile(name);
                }else{
                    if(createFile.isDirectory())documentFile=documentFile.createFile(FileCommon.DIRECTORY_MIMETYPE, name);//创建目录
                    else if(createFile.isFile())documentFile=documentFile.createFile(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(name)), name);
                }
                if(documentFile==null)return null;
            }
        }else{
            //超过了根目录，返回null
            return null;
        }
        return documentFile;
    }

    //根据给出的路径循环遍历出跟目录下的DocumentFile,isCreate 当目录不存在的时候，直接创建该目录
    private DocumentFile getDocumentFileByPath(Context context, StorageDeviceInfo.StorageDeviceType type, String filePath){
        String rootPath=FileFactory.getInstance().getFileWrapper().getStorageDevice(type).getRootPath();
        DocumentFile documentFile=FileFactory.getInstance().getFileWrapper().getStorageDocumentFile(type);
        String[] rootPaths=rootPath.split("/");
        String[] paths=filePath.split("/");
        if(paths.length>=rootPaths.length){
            for(int i=0; i<paths.length-rootPaths.length; i++){
                //documentFile.getParentFile只能获取到在用户授权的范围内的文件，否则就会报错
                documentFile=documentFile.findFile(paths[rootPaths.length+i]);
                if(documentFile==null)return null;//路径中表示的文件不存在
                Logger.i("for doc name="+documentFile.getName());
            }
        }else{
            //超过了根目录，返回null
            return null;
        }
        return documentFile;
    }

    //根据tree uri获取到文件的地址
    private String getPathByTreeUri(Uri uri){
        //SAMSUMG SDCARD
        //   /tree/0123-4567:/document/0123-4567:DCIM
        //   /storage/0123-4567/DCIM

        //HUAWEI SDCARD
        //   /tree/9016-4EF8:/document/9016-4EF8:DCIM/Camera
        //   /storage/sdcard1/DCIM/Camera

        //HUAWEI USBOTG
        //   /tree/0233-CE5F:/documents/0233-CE5F:

        //SAMSUMG USBOTG
        //   /tree/0233-CE5F:/documents/0233-CE5F:
        //根据Uri去获取到对应的文件目录信息
        return GetPathFromUri4kitkat.getPath(FileFactory.getInstance().getContext(), uri);
    }


    @Override
    public boolean delete() {
        if(getDocFile()==null)return false;
        return getDocFile().delete();
    }

    @Override
    public List<FileCommon> listFiles() {
        return listFiles(defaultFileFilter);
    }

    @Override
    public List<FileCommon> listFiles(FileFilter filter) {
        List<FileCommon> files=new ArrayList<FileCommon>();
        File[] fs=mLocalFile.listFiles();
        if(fs==null)return files;
        for(File file : fs){
            FileCommon f=new ExtrageFile(file);
            if(filter.accept(f))files.add(new ExtrageFile(file));
        }
        return files;
    }

    @Override
    public String getName() {
        return mLocalFile.getName();
    }

    @Override
    public String getAbsultPath() {
        return mLocalFile.getAbsolutePath();
    }

    @Override
    public boolean deleteExists() {
        if(getDocFile()==null)return false;
        return getDocFile().delete();
    }

    @Override
    public boolean isFile() {
        return mLocalFile.isFile();
    }

    @Override
    public boolean isDirectory() {
        return mLocalFile.isDirectory();
    }

    @Override
    public long length() {
        return mLocalFile.length();
    }

    @Override
    public long lastModify() {
        return mLocalFile.lastModified();
    }

    @Override
    public boolean mkdirs() {
        return false;
    }

    @Override
    public boolean canReader() {
        return mLocalFile.canRead();
    }

    @Override
    public boolean canWriter() {
        return mLocalFile.canWrite();
    }

    @Override
    public boolean createNewFile() {
        if(getDocFile()!=null)return true;
        return getDocFileNotExists()!=null;
    }

    @Override
    public boolean exists() {
        return mLocalFile.exists();
    }

    @Override
    public String getParent() {
        return mLocalFile.getParent();
    }

    @Override
    public FileCommon getParentFile() {
        return new ExtrageFile(mLocalFile.getParentFile());
    }

    @Override
    public Bitmap getFileIco() {
        return null;
    }

    @Override
    public boolean hasChildFile(String name) {
        if(getDocFile()==null)return false;
        return getDocFile().findFile(name)!=null;
    }

    @Override
    public FileInputStream getFIS() throws IOException {
        if(getDocFile()==null)return null;
        Uri uri=getDocFile().getUri();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            //Android 5.0以上需要把tree uri转换为document uri才能正常访问文件
            String docID=DocumentsContract.getTreeDocumentId(getDocFile().getUri());
            uri=DocumentsContract.buildDocumentUriUsingTree(uri, docID);
        }
        return mContentResolver.openAssetFileDescriptor(uri, "rw").createInputStream();
    }

    @Override
    public DocumentFile getFile() {
        return getDocFile();
    }

    //默认的过滤方式，过滤掉隐藏文件
    private static FileFilter defaultFileFilter=new FileFilter() {
        @Override
        public boolean accept(FileCommon file) {
            if(file.getName().startsWith("."))return false;
            return true;
        }
    };
}
