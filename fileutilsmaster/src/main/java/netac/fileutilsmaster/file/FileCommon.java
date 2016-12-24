package netac.fileutilsmaster.file;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.webkit.MimeTypeMap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

    public abstract List<FileCommon> listFiles(@Nullable FileFilter filter);

    /**采用指定的过滤方式和排序方式去进行返回*/
    public abstract List<FileCommon> listFiles(@Nullable FileFilter filter, @Nullable BubbleSort sort);

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

    /**文件列表查询过滤器*/
    public interface FileFilter{

        /**判断文件是否通过过滤，如果通过则返回结果，不通过则过滤掉该结果*/
        boolean accept(FileCommon file);
    }

    /**冒泡排序,对文件显示的先后去进行排序*/
    public static abstract class BubbleSort{

        private boolean isReserve=false;//是否是进行反向排序

        /**设置是采用正向还是反向的方式去进行排序*/
        public void setReserve(boolean isReserve){
            this.isReserve=isReserve;
        }

        public List<FileCommon> sortList(List<FileCommon> fileCommons){
            List<FileCommon> sortCommon=new ArrayList<>();
            sortCommon.addAll(fileCommons);
            for(int i=0; i<sortCommon.size(); i++){
                for(int j=i; j<sortCommon.size(); j++){
                    FileCommon common1=sortCommon.get(i), common2=sortCommon.get(j);
                    if(changeSort(common1, common2) && !isReserve){
                        sortCommon.set(i, common2);
                        sortCommon.set(j, common1);
                    }
                }
            }
            return sortCommon;
        }

        /**根据给出的文件信息去判断是否要更换common1和common2之间的先后顺序
         *@param common1 file1;
         *@param common2 file2;
         *@return boolean true，表示需要改变他们之间的先后顺序； false则不需要改变。
         * */
        protected abstract boolean changeSort(@Nullable FileCommon common1, @Nullable FileCommon common2);

    }



}
