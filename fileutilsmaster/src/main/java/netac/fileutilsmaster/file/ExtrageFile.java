package netac.fileutilsmaster.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import netac.fileutilsmaster.file.vo.StorageDeviceInfo;
import netac.fileutilsmaster.utils.DocumentTreePermissionUtils;
import netac.fileutilsmaster.utils.GetPathFromUri4kitkat;
import netac.fileutilsmaster.utils.Logger;

/**
 * Created by siwei.zhao on 2016/6/29.<br/>
 * 非内置存储设备的File操作<br/>
 */
public class ExtrageFile extends FileCommon {

    //读写文件的时候不能出现File和DocumentFile是指向一个文件地址，否则DocumentFile的很多操作将会无效

    protected DocumentFile mFile;

    public ExtrageFile(String path) {
        super(path);
        this.path=path;
        getDocFile();
    }

    public ExtrageFile(Uri uri){
        super("");
        this.path= getPathByTreeUri(uri);
        getDocFile();
    }

    public ExtrageFile(File file){
        super(file.getAbsolutePath());
        this.path=file.getAbsolutePath();
        getDocFile();
    }

    private ExtrageFile(DocumentFile file, String path){
        super(path);
        this.path=path;
        this.mFile=file;
    }


    public ExtrageFile(DocumentFile file){
        super("");
        this.path= getPathByTreeUri(file.getUri());
        mFile=file;
    }

    /**获取DocumentFile，不过不存在则返回null*/
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

    //根据路径获取获取DocumentFile，不过不存在则返回null
    private DocumentFile getFileByPath(String path){
        StorageDeviceInfo.StorageDeviceType type=FileFactory.getInstance().getFileWrapper().getPathStorageType(path);
        DocumentFile file=getDocumentFileByPath(FileFactory.getInstance().getContext(), type, path);
        return file;
    }

    //根据路径，如果路径不存在，则创建目录或者文件，主要用于IO流操作
    private DocumentFile getCreateFileByPath(Context context, StorageDeviceInfo.StorageDeviceType type, String filePath){
        StorageDeviceInfo info=FileFactory.getInstance().getFileWrapper().getStorageDevice(type);
        if(info==null)return null;
        String rootPath=info.getRootPath();
        DocumentFile documentFile= DocumentTreePermissionUtils.getInstance().getStorageRootDocumentFile(type);
        if(documentFile==null)return null;//没有获取到该存储设备的权限
        String[] rootPaths=rootPath.split("/");
        String[] paths=filePath.split("/");
        ExtrageFile createFile=new ExtrageFile(rootPath);
        if(paths.length>=rootPaths.length){
            for(int i=rootPaths.length; i<paths.length; i++){
                //documentFile.getParentFile只能获取到在用户授权的范围内的文件，否则就会报错
                String name=paths[i];
//                rootPath+="/"+name;
                createFile= (ExtrageFile) createFile.getChildFile(createFile.getAbsolutePath(), name);
                if(createFile.exists()){
                    documentFile=documentFile.findFile(name);
                }else{
                    if(createFile.isDirectory()){
                        documentFile=documentFile.createFile(DIRECTORY_MIMETYPE, name);//创建目录
                    }else if(createFile.isFile()){
                        documentFile=documentFile.createFile(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(name)), name);
                    }
                }

                if(documentFile==null)return null;
            }
        }else{
            //超过了根目录，返回null
            return null;
        }
        return documentFile;
    }

    //根据给出的路径循环遍历出跟目录下的DocumentFile,isCreate 当目录不存在的时候，直接返回null
    private @Nullable DocumentFile getDocumentFileByPath(Context context, StorageDeviceInfo.StorageDeviceType type, String filePath){
        StorageDeviceInfo info=FileFactory.getInstance().getFileWrapper().getStorageDevice(type);
        if(info==null)return null;
        String rootPath=info.getRootPath();
        DocumentFile documentFile=DocumentTreePermissionUtils.getInstance().getStorageRootDocumentFile(type);
        if(documentFile==null)return null;//没有获取到该存储设备的权限
        String[] rootPaths=rootPath.split("/");
        String[] paths=filePath.split("/");
        if(paths.length>=rootPaths.length){
            for(int i=rootPaths.length; i<paths.length; i++){
                //documentFile.getParentFile只能获取到在用户授权的范围内的文件，否则就会报错
                documentFile=documentFile.findFile(paths[i]);
                if(documentFile==null)return null;//路径中表示的文件不存在
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
        if(mFile==null)return false;
        return mFile.delete();
    }

    @Override
    public List<FileCommon> listFiles() {
        return listFiles(null, null);
    }

    @Override
    public List<FileCommon> listFiles(FileFilter filter) {
        return listFiles(filter, null);
    }

    @Override
    public List<FileCommon> listFiles(@Nullable FileFilter filter, @Nullable BubbleSort sort) {
        List<FileCommon> files=new ArrayList<FileCommon>(), directory=new ArrayList<>(), fileCommons=new ArrayList<>();
        if(mFile==null)return files;
        DocumentFile[] fs=mFile.listFiles();
        if(fs==null)return fileCommons;
        for(DocumentFile file : fs){
            FileCommon f=new ExtrageFile(file, getAbsolutePath()+"/"+file.getName());
            if(filter==null || (filter!=null && filter.accept(f))){
                if(file.isDirectory()){
                    directory.add(f);
                }else if(file.isFile()){
                    files.add(f);
                }
            }
        }
        //对文件按照指定的规则去进行排序
        if(sort!=null){
            directory=sort.sortList(directory);
            files=sort.sortList(fileCommons);
        }
        //默认的排序，目录在前面，文件在后面
        fileCommons.addAll(directory);
        fileCommons.addAll(files);
        return fileCommons;
    }

    @Override
    public String getName() {
        int separatorIndex = path.lastIndexOf(File.separator);
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }

    @Override
    public String getAbsolutePath() {
        return path;
    }

    @Override
    public boolean deleteExists() {
        if(mFile==null)return false;
        return mFile.delete();
    }

    @Override
    public boolean isFile() {
        if(mFile==null)return getName().indexOf(".")>=0;
        else return mFile.isFile();
    }

    @Override
    public boolean isDirectory() {
        if(mFile==null)return !isFile();
        else return mFile.isDirectory();
    }

    @Override
    public long length() {
        if(mFile==null)return 0;
        return mFile.length();
    }

    @Override
    public long lastModify() {
        if(mFile==null)return 0;
        return mFile.lastModified();
    }

    @Override
    public boolean mkdirs() {
        if(mFile==null)mFile=getDocFileNotExists();//创建文件也会被执行额
        return mFile.exists();
    }

    @Override
    public boolean canReader() {
        if(mFile==null)return false;
        return mFile.canRead();
    }

    @Override
    public boolean canWriter() {
        if(mFile==null)return false;
        return mFile.canWrite();
    }

    @Override
    public boolean createNewFile() {
        if(mFile!=null)return true;
        return getDocFileNotExists()!=null;
    }

    @Override
    public boolean exists() {
        if(mFile==null)return false;
        return mFile.exists();
    }

    @Override
    public boolean renameTo(String newName) {
        if(mFile==null)return false;
        return mFile.renameTo(newName);
    }

    @Override
    public boolean isRootFile() {
        return FileFactory.getInstance().getFileWrapper().isRootPath(path);
    }

    @Override
    public String getParent() {
        //切割路径去获取父路径名称
        int length = path.length(), firstInPath = 0;
        if (File.separatorChar == '\\' && length > 2 && path.charAt(1) == ':') {
            firstInPath = 2;
        }
        int index = path.lastIndexOf(File.separatorChar);
        if (index == -1 && firstInPath > 0) {
            index = 2;
        }
        if (index == -1 || path.charAt(length - 1) == File.separatorChar) {
            return null;
        }
        if (path.indexOf(File.separatorChar) == index
                && path.charAt(firstInPath) == File.separatorChar) {
            return path.substring(0, index + 1);
        }
        return path.substring(0, index);
    }

    @Override
    public FileCommon getParentFile() {
        if(FileFactory.getInstance().getFileWrapper().isRootPath(path))return null;//根目录则不返回
        else return new ExtrageFile(getParent());
    }

    @Override
    public Bitmap getFileIco() {
        return null;
    }

    @Override
    public boolean hasChildFile(String name) {
        if(mFile==null)return false;
        return mFile.findFile(name)!=null;
    }

    @Override
    public FileCommon getChildFile(String name) {
        if(mFile==null || !hasChildFile(name))return new ExtrageFile(path+"/"+name);
        else return new ExtrageFile(mFile.findFile(name));
    }

    public FileCommon getChildFile(String parentPath, String name) {
        if(mFile==null || !hasChildFile(name))return new ExtrageFile(path+"/"+name);
        else return new ExtrageFile(mFile.findFile(name), parentPath+"/"+name);
    }

    @Override
    public @Nullable FileInputStream getFileInputStream() throws FileNotFoundException, IOException {
        //写入的流，当文件不存在，直接抛出文件不存在的异常
        if(getDocFile()==null)throw new FileNotFoundException();
        Uri uri=mFile.getUri();
//        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
//            //Android 5.0以上需要把tree uri转换为document uri才能正常访问文件
//            String docID=DocumentsContract.getTreeDocumentId(uri);
//            uri=DocumentsContract.buildDocumentUriUsingTree(uri, docID);
//        }
        Logger.d("GetFileInputStream path=%s Uri=%s", getAbsolutePath(), uri);
        return mContentResolver.openAssetFileDescriptor(uri, "rw").createInputStream();
    }

    @Override
    public @NonNull FileOutputStream getFileOutputStream() throws FileNotFoundException, IOException {
        //写出的流，当文件不存在则直接创建文件，文件创建失败再抛出文件不存在的异常
        if(getDocFileNotExists()==null)throw new FileNotFoundException();
        Uri uri=mFile.getUri();
//        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
//            //Android 5.0以上需要把tree uri转换为document uri才能正常访问文件
//            String docID=DocumentsContract.getTreeDocumentId(uri);
//            uri=DocumentsContract.buildDocumentUriUsingTree(uri, docID);
//        }
        Logger.d("GetFileOutputStream path=%s Uri=%s", getAbsolutePath(), uri);
        return mContentResolver.openAssetFileDescriptor(uri, "rw").createOutputStream();
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
