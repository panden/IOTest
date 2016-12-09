package netac.fileutilsmaster.file.operation;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import netac.fileutilsmaster.file.FileCommon;
import netac.fileutilsmaster.file.FileFactory;
import netac.fileutilsmaster.file.manager.FileShearPlateManager;
import netac.fileutilsmaster.file.vo.StorageDeviceInfo;
import netac.fileutilsmaster.utils.FileUtils;
import netac.fileutilsmaster.utils.Logger;

/**
 * Created by siwei.zhao on 2016/11/7.
 * 文件操作信息
 */

public class FileOperationWrapper {

    /**等待被执行状态*/
    public static final int OPERATION_STATUS_WAITTING=1;

    /**正在执行*/
    public static final int OPERATION_STATUS_EXCUTING=2;

    /**任务执行成功*/
    public static final int OPERATION_STATUS_SUCCESS=3;

    /**任务执行失败*/
    public static final int OPEATION_STATUS_FAILD=4;

    @IntDef({OPEATION_STATUS_FAILD, OPERATION_STATUS_EXCUTING, OPERATION_STATUS_SUCCESS, OPERATION_STATUS_WAITTING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OperationStatus{};//文件操作执行的状态

    /**重复文件或目录覆盖*/
    public static final int SAME_FILE_DIR_OVERRIED=5;

    /**重复文件重命名*/
    public static final int SAME_FILE_RENAME=6;

    /**重复目录合并*/
    public static final int SAME_DIR_MERGE=7;

    /**重复任务，但是尚未选择需要处理的方式*/
    public static final int SAME_NONE_CHOOSE=8;

    /**跳过重复任务*/
    public static final int SAME_SKIP_TASK=9;

    @IntDef({SAME_DIR_MERGE,SAME_FILE_RENAME, SAME_FILE_DIR_OVERRIED, SAME_NONE_CHOOSE, SAME_SKIP_TASK})
    public @interface SameFileOperation{};//文件或文件夹重复的处理方式

    private List<FileTaskInfo> mFileTaskInfos=new ArrayList<>();//需要执行的文件操作任务
    private List<FileTaskInfo> mFileErrorTaskInfos=new ArrayList<>();//执行完成之后存在问题的任务
    private String fromDir;//fromDir
    private String toDir;//toDir
    private String oldFileName;//oldName
    private String newFileName;//newName 如果文件名重复则newName是新的文件名，否则和oldFileName一致
    private @SameFileOperation int sameOperation=SAME_NONE_CHOOSE;//重复文件或者文件夹处理方式
    private Context mContext;//Activity Context
    private long taskId;//任务ID
    private int progress;//任务执行进度
    private long taskFileSize;//任务需要操作的文件大小
    private int taskCount;//需要进行操作的文件的个数
    private int localTaskExcutePosition=-1;//当前执行到第几个任务了
    private @FileTaskInfo.TaskType int taskType;

    private FileOperationWrapper(){};

    /**创建文件操作任务*/
    public static FileOperationWrapper createFileOperation(@NonNull Context context, @NonNull FileCommon file, @NonNull String toDir, @FileTaskInfo.TaskType int taskType){
        FileOperationWrapper fileOperation=new FileOperationWrapper();
        fileOperation.fromDir=file.getParent();
        fileOperation.oldFileName=file.getName();
        fileOperation.newFileName=file.getName();
        fileOperation.taskType=taskType;
        fileOperation.toDir=toDir;
        fileOperation.taskId=System.currentTimeMillis();
        fileOperation.mContext=context;
        return fileOperation;
    }

    /**创建多文件操作任务*/
    public static List<FileOperationWrapper> createFileOperations(@NonNull Context context, @NonNull List<FileCommon> files, @NonNull String toDir, @FileTaskInfo.TaskType int taskType){
        List<FileOperationWrapper> fileOperations=new ArrayList<>();
        for(FileCommon file : files){
            fileOperations.add(createFileOperation(context, file, toDir, taskType));
        }
        return fileOperations;
    }

    /**创建文件操作任务*/
    public static FileOperationWrapper createFileOperation(@NonNull Context context, @NonNull FileShearPlateManager.FileSharePalteFile file, @NonNull String toDir){
        FileOperationWrapper fileOperation=new FileOperationWrapper();
        fileOperation.fromDir=file.getFile().getParent();
        fileOperation.oldFileName=file.getFile().getName();
        fileOperation.newFileName=file.getFile().getName();
        fileOperation.mContext=context;
        switch (file.getFileShareType()){
            case FileShearPlateManager.FileSharePalteFile.FILE_COPY:
                fileOperation.taskType=FileTaskInfo.TASK_TYPE_COPY;
                break;
            case FileShearPlateManager.FileSharePalteFile.FILE_CUT:
                fileOperation.taskType=FileTaskInfo.TASK_TYPE_MOVE;
                break;
            case FileShearPlateManager.FileSharePalteFile.FILE_DELETE:
                fileOperation.taskType=FileTaskInfo.TASK_TYPE_DELETE;
                break;
        }
        fileOperation.toDir=toDir;
        fileOperation.taskId=System.currentTimeMillis();
        return fileOperation;
    }

    /**创建多文件操作任务*/
    public static List<FileOperationWrapper> createFileOperations(@NonNull Context context, @NonNull List<FileShearPlateManager.FileSharePalteFile> files, @NonNull String toDir){
        List<FileOperationWrapper> fileOperations=new ArrayList<>();
        for(FileShearPlateManager.FileSharePalteFile file : files){
            fileOperations.add(createFileOperation(context, file, toDir));
        }
        return fileOperations;
    }

    /**检验执行的操作的文件是否已存在*/
    public boolean checkOperation(){
        return new File(toDir, newFileName).exists();
    }

    @SameFileOperation
    public int getSameOperation() {
        return sameOperation;
    }

    public void setSameOperation(@SameFileOperation int sameOperation) {
        this.sameOperation = sameOperation;
    }

    public Context getContext() {
        return mContext;
    }

    public List<FileTaskInfo> getFileTaskInfos() {
        return mFileTaskInfos;
    }

    public void setFileTaskInfos(List<FileTaskInfo> fileTaskInfos) {
        mFileTaskInfos = fileTaskInfos;
    }

    public String getFromDir() {
        return fromDir;
    }

    public void setFromDir(String fromDir) {
        this.fromDir = fromDir;
    }

    public String getToDir() {
        return toDir;
    }

    public void setToDir(String toDir) {
        this.toDir = toDir;
    }

    public String getOldFileName() {
        return oldFileName;
    }

    public void setOldFileName(String oldFileName) {
        this.oldFileName = oldFileName;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getTaskFileSize() {
        return taskFileSize;
    }

    public void setTaskFileSize(long taskFileSize) {
        this.taskFileSize = taskFileSize;
    }

    public int getTaskType() {
        return taskType;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public int getLocalTaskExcutePosition() {
        return localTaskExcutePosition;
    }

    public void setLocalTaskExcutePosition(int localTaskExcutePosition) {
        this.localTaskExcutePosition = localTaskExcutePosition;
    }

    /***计算文件信息，主要是计算出需要操作文件的个数和操作文件的总大小*/
    public void caculationFileInfo(){
        caculationFileInfo(new File(fromDir, oldFileName));
    }

    /***计算文件信息，主要是计算出需要操作文件的个数和操作文件的总大小*/
    private int caculationFileInfo(File file){
        if(file.isDirectory()){
            File[] fileCommons=file.listFiles();
            for(File fileCommon : fileCommons)caculationFileInfo(fileCommon);
            return taskCount;
        }else{
            taskFileSize+=file.length();
            taskCount=taskCount+1;
            return taskCount;
        }
    }

    /**计算需要执行的任务*/
    public void caculationTasks(){
        Logger.d("文件操作任务计算开始");
        File fileCommon=new File(fromDir, oldFileName);
        if(!caculationSingleTask(fileCommon, toDir, taskType)){
            //不是一次性能执行完的任务，则计算多任务
            caculationTasks(fileCommon, toDir, taskType);
            if(taskType==FileTaskInfo.TASK_TYPE_MOVE){
                mFileTaskInfos.add(new FileTaskInfo(fileCommon.getParent(), fileCommon.getParent(), taskId, fileCommon.getName(), fileCommon.getName(), FileTaskInfo.TASK_TYPE_DELETE, fileCommon.length()));
                Logger.d("task deleteDir fromDir="+fileCommon.getAbsolutePath());
            }
        }
        Logger.d("文件操作任务计算结束");
    }

    /**计算任务,from到to的目录不能为同一个目录，要是目录相同，则相当于不进行操作*/
    private void caculationTasks(File file, String toDir, @FileTaskInfo.TaskType int taskType){
        if(file==null || toDir==null || file.getParent().equals(toDir))return;
        if(file.isDirectory()){
            toDir=getToNewDir(toDir, file.getName());
            switch (taskType){
                case FileTaskInfo.TASK_TYPE_DELETE:
                    //删除目录,删除from
                    goNextDir(file, toDir, taskType);//先删文件，再建目录
                    Logger.d("task deleteDir fromDir="+file.getAbsolutePath());
                    mFileTaskInfos.add(new FileTaskInfo(file.getParent(), file.getParent(), taskId, file.getName(), file.getName(), FileTaskInfo.TASK_TYPE_DELETE, file.length()));
                    break;
                case FileTaskInfo.TASK_TYPE_COPY://文件夹复制
                    //创建目录，创建todir
                    Logger.d("task createDir toDir=%s", toDir);
                    mFileTaskInfos.add(new FileTaskInfo(file.getParent(), toDir, taskId, file.getName(), file.getName(), FileTaskInfo.TASK_TYPE_CREATE_DIR, file.length()));
                    goNextDir(file, toDir, taskType);//先建目录再去复制文件
                    break;
                case FileTaskInfo.TASK_TYPE_MOVE://分为同存储设备内移动、不同存储设备间移动，这是是处理不同存储设备间移动
                    //移动文件夹，from移动到to
                    Logger.d("task createDir toDir=%s", toDir);
                    mFileTaskInfos.add(new FileTaskInfo(file.getParent(), toDir, taskId, file.getName(), file.getName(), FileTaskInfo.TASK_TYPE_CREATE_DIR, file.length()));
                    goNextDir(file, toDir, taskType);//先建目录再去移动文件
                    break;
                case FileTaskInfo.TASK_TYPE_DELETE_TO_RECYCLE_BIN:
                    //删除到回收站，移动到该文件对应的存储设备的制定目录下，再让文件不能直接读写
                    //属于单文件任务，不再计算的考虑范围内
                    goNextDir(file, toDir, taskType);
                    break;
                case FileTaskInfo.TASK_TYPE_CREATE_DIR:
                    //单文件任务，不在计算的考虑范围内
                    //mFileTaskInfos.add(new FileTaskInfo(file.getParent(), toDir, taskId, file.getName(), file.getName(), FileTaskInfo.TASK_TYPE_CREATE_DIR, file.length()));
                    goNextDir(file, toDir, taskType);
                    break;
            }
            System.out.println("=====Directory end");
        }else{
            //文件任务
            switch (taskType){
                case FileTaskInfo.TASK_TYPE_DELETE:
                    //删除文件，删除from
                    Logger.d("deleteFile from=%s", file.getAbsolutePath());
                    mFileTaskInfos.add(new FileTaskInfo(file.getParent(), file.getParent(), taskId, file.getName(), file.getName(), FileTaskInfo.TASK_TYPE_DELETE, file.length()));
                    break;
                case FileTaskInfo.TASK_TYPE_COPY:
                    //复制文件,from复制到to
                    Logger.d("copyFile from=%s ————> to=%s", file.getAbsolutePath(), toDir+"/"+file.getName());
                    mFileTaskInfos.add(new FileTaskInfo(file.getParent(), toDir, taskId, file.getName(), file.getName(), FileTaskInfo.TASK_TYPE_COPY, file.length()));
                    break;
                case FileTaskInfo.TASK_TYPE_MOVE://不同存储设备间移动文件，先复制后删除
                    //移动文件，from移动到to
                    Logger.d("moveFile from=%s ————> to=%s", file.getAbsolutePath(), toDir+"/"+file.getName());
                    mFileTaskInfos.add(new FileTaskInfo(file.getParent(), toDir, taskId, file.getName(), file.getName(), FileTaskInfo.TASK_TYPE_MOVE, file.length()));
                    break;
                case FileTaskInfo.TASK_TYPE_DELETE_TO_RECYCLE_BIN:
                    //删除到回收站，移动到该文件对应的存储设备的制定目录下，再让文件不能直接读写
                    //属于单文件任务，不再计算的考虑范围内
                    break;
                case FileTaskInfo.TASK_TYPE_CREATE_FILE:
                    //单文件任务，不再计算的考虑范围内
                    //mFileTaskInfos.add(new FileTaskInfo(file.getParent(), file.getParent(), taskId, file.getName(), file.getName(), FileTaskInfo.TASK_TYPE_CREATE_FILE, file.length()));
                    break;
            }

        }
    }

    //遍历目录，继续递归执行,caculationTask方法中抽出的一个方法，需要结合caculationTask使用
    private void goNextDir(File file, String toDir, @FileTaskInfo.TaskType int taskType){
        File[] files=file.listFiles();
        if(files!=null && files.length>0){
            for(File fileCommon : files) caculationTasks(fileCommon, toDir, taskType);
        }
    }

    /**任务计算出来只会产生一个任务的, return true当前始终只会计算出一个任务
     * from到to的目录不能相同，要是相同则相当于不操作
     * SAME_FILE_RENAME这种情况只会出现在单文件的操作上*/
    private boolean caculationSingleTask(File file, String toDir, @FileTaskInfo.TaskType int taskType){
        boolean bool=false;
        switch (taskType){
            case FileTaskInfo.TASK_TYPE_CREATE_DIR:
                if(file.isDirectory()){
                    Logger.d("single create dir task from=%s to=%s", file.getAbsolutePath(), toDir+"/"+newFileName);
                    mFileTaskInfos.add(new FileTaskInfo(file.getParent(), toDir, taskId, file.getName(), file.getName(), FileTaskInfo.TASK_TYPE_CREATE_DIR, file.length()));
                    bool = true;
                }
                break;
            case FileTaskInfo.TASK_TYPE_CREATE_FILE:
                if(file.isFile()){
                    Logger.d("single create file task from=%s to=%s", file.getAbsolutePath(), toDir+"/"+newFileName);
                    mFileTaskInfos.add(new FileTaskInfo(file.getParent(), toDir, taskId, file.getName(), file.getName(), FileTaskInfo.TASK_TYPE_CREATE_FILE, file.length()));
                    bool = true;
                }
                break;
            case FileTaskInfo.TASK_TYPE_MOVE:
                StorageDeviceInfo.StorageDeviceType fromType=FileFactory.getInstance().getFileWrapper().getPathStorageType(file.getAbsolutePath());
                StorageDeviceInfo.StorageDeviceType toType=FileFactory.getInstance().getFileWrapper().getPathStorageType(toDir);
                Logger.d("from type=%s, toType=%s same=%s", String.valueOf(fromType), String.valueOf(toType), String.valueOf(fromType==toType));
                if(fromType==toType){//同一个存储设备内的move能一次性move
                    //低于4.4的同一存储设备或者高于4.4且只在内置存储设备上
                    if(FileUtils.BlowKitKat() || (FileUtils.AboveKitKat() && fromType== StorageDeviceInfo.StorageDeviceType.ExtrageDevice)){
                        Logger.d("移动到的目录和被移动的目录在同一个存储设备上, 一次性任务，直接执行");
                        if(sameOperation==SAME_FILE_RENAME)newFileName=FileUtils.getNewFileName(toDir, file.getName());//生成一个新的文件名
                        Logger.d("single move task from=%s to=%s", file.getAbsolutePath(), toDir+"/"+newFileName);
                        mFileTaskInfos.add(new FileTaskInfo(file.getParent(), toDir, taskId, file.getName(), newFileName, FileTaskInfo.TASK_TYPE_ONE_TIME_MOVE, file.length()));
                        bool = true;
                    }
                }
                break;
            case FileTaskInfo.TASK_TYPE_COPY:
                if(file.isFile()){
                    //单文件复制进行重命名，目录只会出现覆盖不会出现重命名
                    if(sameOperation==SAME_FILE_RENAME)newFileName= FileUtils.getNewFileName(toDir, file.getName());//生成一个新的文件名
                    Logger.d("single copy task from=%s to=%s", file.getAbsolutePath(), toDir+"/"+newFileName);
                    mFileTaskInfos.add(new FileTaskInfo(file.getParent(), toDir, taskId, file.getName(), newFileName, FileTaskInfo.TASK_TYPE_COPY, file.length()));
                    bool=true;
                }
                break;
        }
        return bool;
    }

    //获取下级目录
    public String getToNewDir(String dir, String name){
        if(dir.endsWith("/")){
            dir+=name;
        }else{
            dir+="/"+name;
        }
        return dir;
    }

    @Override
    public String toString() {
        String wrapperStr=fromDir+"/"+oldFileName+"   "+FileTaskInfo.getTaskTypeString(taskType)+"   "+toDir+"/"+newFileName;
        return wrapperStr;
    }
}
