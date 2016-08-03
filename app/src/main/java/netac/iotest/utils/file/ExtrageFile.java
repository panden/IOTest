package netac.iotest.utils.file;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by siwei.zhao on 2016/6/29.
 */
public class ExtrageFile extends FileCommon {

    protected DocumentFile mFile;

    public ExtrageFile(String path) {
        super(path);
        File file=new File(path);
        mFile=DocumentFile.fromFile(file);
    }

    public ExtrageFile(Uri uri){
        super("");
        path=getPathByUri(uri);
        mFile=DocumentFile.fromSingleUri(CustomFile.getContext(), uri);
    }

    public ExtrageFile(File file){
        super(file.getAbsolutePath());
        mFile=DocumentFile.fromFile(file);
    }

    public ExtrageFile(DocumentFile file){
        super("");
        path=getPathByUri(file.getUri());
        mFile=file;
    }

    private String getPathByUri(Uri uri){
        return null;
    }


    @Override
    public boolean delete() {
        return mFile.delete();
    }

    @Override
    public List<FileCommon> listFiles() {
        List<FileCommon> files=new ArrayList<FileCommon>();
        DocumentFile[] fs=mFile.listFiles();
        if(fs==null)return files;
        for(DocumentFile file : fs)files.add(new ExtrageFile(file));
        return files;
    }

    @Override
    public List<FileCommon> listFiles(FileFilter filter) {
        List<FileCommon> files=new ArrayList<FileCommon>();
        DocumentFile[] fs=mFile.listFiles();
        if(fs==null)return files;
        for(DocumentFile file : fs){
            FileCommon f=new ExtrageFile(file);
            if(filter.accept(f))files.add(new ExtrageFile(file));
        }
        return files;
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public String getAbsultPath() {
        return path;
    }

    @Override
    public boolean deleteExists() {
        return mFile.delete();
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
        return false;
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
        DocumentFile parent=mFile.getParentFile();
        if(parent.exists()){
            mFile=parent.createFile("*/*", mFile.getName());
            return true;
        }
        return false;
    }

    @Override
    public boolean exists() {
        return mFile.exists();
    }

    @Override
    public String getParent() {
        return getPathByUri(mFile.getParentFile().getUri());
    }

    @Override
    public FileCommon getParentFile() {
        return new ExtrageFile(mFile.getParentFile());
    }

    @Override
    public Bitmap getFileIco() {
        return null;
    }
}
