package netac.fileutilsmaster.file.operation;

import netac.fileutilsmaster.file.FileCommon;

/**
 * Created by siwei.zhao on 2016/11/15.
 * 单个文件或空目录操作
 */

public class SingleFileOperation {

    private static SingleFileOperation sSingleFileOperation;

    private SingleFileOperation(){};

    public static SingleFileOperation getInstance(){
        if(sSingleFileOperation==null)sSingleFileOperation=new SingleFileOperation();
        return sSingleFileOperation;
    }


    /**文件删除*/
    public boolean fileDelete(FileCommon fileCommon){
        return fileCommon.delete();
    }

    /**文件剪切*/
    public boolean fileMove(FileCommon fileCommon, String toDir){
        return true;
    }

    /**文件复制*/
    public boolean fileCopy(FileCommon fileCommon, String toDir){
        return true;
    }

    /**文件重命名*/
    public boolean fileRename(){
        return true;
    }

    public boolean createNewFile(String dir, String name){
        return true;
    }

    public boolean createNewDir(String dir, String name){
        return true;
    }

}
