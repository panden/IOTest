package netac.iotest.utils.file;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by siwei.zhao on 2016/6/29.
 */
public abstract class FileCommon {

    protected String path;

    public FileCommon(String path){
        this.path=path;
    }

    public abstract boolean delete();

    public abstract List<FileCommon> listFiles();

    public abstract List<FileCommon> listFiles(FileFilter filter);

    public abstract String getName();

    public abstract String getAbsultPath();

    public abstract boolean deleteExists();

    public abstract boolean isFile();

    public abstract boolean isDirectory();

    public abstract long length();

    public abstract long lastModify();

    public abstract boolean mkdirs();

    public abstract boolean canReader();

    public abstract boolean canWriter();

    public abstract boolean createNewFile();

    public abstract boolean exists();

    public abstract String getParent();

    public abstract FileCommon getParentFile();

    public abstract Bitmap getFileIco();

    public interface FileFilter{

        boolean accept(FileCommon file);
    }
}
