package netac.fileutilsmaster.file;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by siwei.zhao on 2016/6/29.
 */
public class LocalFile extends FileCommon {

    protected File mFile;

    public LocalFile(String path) {
        super(path);
        mFile=new File(path);
    }

    public LocalFile(File file){
        super(file.getAbsolutePath());
        mFile=file;
    }

    @Override
    public boolean delete() {
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
        List<FileCommon> files=new ArrayList<FileCommon>(), directorys=new ArrayList<>(), fileCommons=new ArrayList<>();
        File[] fs=mFile.listFiles();
        if(fs==null)return fileCommons;
        for(File file:fs){
            LocalFile f=new LocalFile(file);
            if(filter==null ||(filter!=null && filter.accept(f))){
                if(file.isDirectory())directorys.add(f);
                else if(file.isFile())files.add(f);
            }
        }
        if(sort!=null){
            directorys=sort.sortList(directorys);//目录排序
            files=sort.sortList(files);//文件排序
        }
        fileCommons.addAll(directorys);
        fileCommons.addAll(files);
        return fileCommons;
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public String getAbsolutePath() {
        return mFile.getAbsolutePath();
    }

    @Override
    public boolean deleteExists() {
        mFile.deleteOnExit();
        return true;
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
        return mFile.lastModified();
    }

    @Override
    public boolean mkdirs() {
        return mFile.mkdirs();
    }

    @Override
    public boolean canReader() {
        return mFile.canRead();
    }

    @Override
    public boolean canWriter() {
        return mFile.canWrite();
    }

    @Override
    public boolean createNewFile() {
        try {
            mFile.createNewFile();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean exists() {
        return mFile.exists();
    }

    @Override
    public boolean renameTo(String newName) {
        return mFile.renameTo(new File(mFile.getParent(), newName));
    }

    @Override
    public boolean isRootFile() {
        return FileFactory.getInstance().getFileWrapper().isRootPath(path);
    }

    @Override
    public String getParent() {
        return mFile.getParent();
    }

    @Override
    public FileCommon getParentFile() {
        return new LocalFile(mFile.getParentFile());
    }

    @Override
    public Bitmap getFileIco() {
        return null;
    }

    @Override
    public boolean hasChildFile(String name) {
        String[] files=mFile.list();
        if(files==null || files.length==0)return false;
        return Arrays.asList(files).contains(name);
    }

    @Override
    public FileCommon getChildFile(String name) {
        return new LocalFile(mFile.getAbsolutePath()+"/"+name);
    }

    @Override
    public FileInputStream getFileInputStream() throws IOException {
        return new FileInputStream(mFile);
    }

    @Override
    public FileOutputStream getFileOutputStream() throws FileNotFoundException, IOException {
        return new FileOutputStream(mFile);
    }

    @Override
    public DocumentFile getFile() {
        return DocumentFile.fromFile(mFile);
    }
}
