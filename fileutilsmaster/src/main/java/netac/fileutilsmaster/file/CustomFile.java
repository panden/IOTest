package netac.fileutilsmaster.file;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by siwei.zhao on 2016/6/29.
 */
public class CustomFile extends FileCommon{

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
    public List<FileCommon> listFiles(@Nullable FileFilter filter, @Nullable BubbleSort sort) {
        return mFile.listFiles(filter, sort);
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
    public boolean renameTo(String newName) {
        return mFile.renameTo(newName);
    }

    @Override
    public boolean isRootFile() {
        return mFile.isRootFile();
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

    @Override
    public boolean hasChildFile(String name) {
        return mFile.hasChildFile(name);
    }

    @Override
    public FileCommon getChildFile(String name) {
        return mFile.getChildFile(name);
    }

    @Override
    public FileInputStream getFileInputStream() throws FileNotFoundException, IOException {
        return mFile.getFileInputStream();
    }

    @Override
    public FileOutputStream getFileOutputStream() throws FileNotFoundException, IOException {
        return mFile.getFileOutputStream();
    }

    @Override
    public DocumentFile getFile() {
        return mFile.getFile();
    }

    protected FileCommon createFile(String path){
        return FileFactory.getInstance().createFile(path);
    }

}
