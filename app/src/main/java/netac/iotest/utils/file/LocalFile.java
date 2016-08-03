package netac.iotest.utils.file;

import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
        List<FileCommon> files=new ArrayList<FileCommon>();
        File[] fs=mFile.listFiles();
        if(fs==null)return files;
        for(File file:fs)files.add(new LocalFile(file));
        return files;
    }

    @Override
    public List<FileCommon> listFiles(FileFilter filter) {
        List<FileCommon> files=new ArrayList<FileCommon>();
        File[] fs=mFile.listFiles();
        if(fs==null)return files;
        for(File file:fs){
            LocalFile f=new LocalFile(file);
            if(filter.accept(f))files.add(f);
        }
        return files;
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public String getAbsultPath() {
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
}
