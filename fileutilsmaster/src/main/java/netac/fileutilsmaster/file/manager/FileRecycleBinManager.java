package netac.fileutilsmaster.file.manager;

/**
 * Created by siwei.zhao on 2016/11/15.
 * 文件回收站管理
 */

public class FileRecycleBinManager {

    private static FileRecycleBinManager sFileRecycleBinManager;

    private FileRecycleBinManager(){};

    public static FileRecycleBinManager getInstance(){
        if(sFileRecycleBinManager==null)sFileRecycleBinManager=new FileRecycleBinManager();
        return sFileRecycleBinManager;
    }
}
