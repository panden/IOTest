package netac.fileutilsmaster.file;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.support.v4.provider.DocumentFile;
import android.webkit.MimeTypeMap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by siwei.zhao on 2016/6/29.
 */
public abstract class FileCommon {

    /**目录mimetype*/
    public static final String DIRECTORY_MIMETYPE ="vnd.android.document/directory";

    protected String path;

    protected ContentResolver mContentResolver=FileFactory.getInstance().getContext().getContentResolver();

    public FileCommon(String path){
        this.path=path;
    }

    public abstract boolean delete();

    public abstract List<FileCommon> listFiles();

    public abstract List<FileCommon> listFiles(FileFilter filter);

    public abstract String getName();

    public abstract String getAbsolutePath();

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

    public abstract boolean renameTo(String newName);

    public abstract boolean isRootFile();

    public abstract String getParent();

    public abstract FileCommon getParentFile();

    public abstract Bitmap getFileIco();

    public abstract boolean hasChildFile(String name);

    public abstract FileCommon getChildFile(String name);

    /**获取文件流*/
    public abstract FileInputStream getFileInputStream() throws FileNotFoundException, IOException;

    public abstract FileOutputStream getFileOutputStream() throws FileNotFoundException, IOException;

    public abstract DocumentFile getFile();

    /**获取文件mimeType*/
    public String getMimeType(){
        if(isDirectory())return DIRECTORY_MIMETYPE;
        else return MimeTypeMap.getSingleton().getExtensionFromMimeType(MimeTypeMap.getFileExtensionFromUrl(getName()));
    }

    public interface FileFilter{

        boolean accept(FileCommon file);
    }



}
