package netac.fileutilsmaster.file.operation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import netac.fileutilsmaster.R;
import netac.fileutilsmaster.utils.ResourceUtils;

/**
 * Created by siwei.zhao on 2016/11/7.
 * 单个文件需要被执行任务的信息
 */

public class FileTaskInfo {

    /**删除文件/目录*/
    public static final int TASK_TYPE_DELETE=1;

    /**复制文件*/
    public static final int TASK_TYPE_COPY =2;

    /**移动文件/目录*/
    public static final int TASK_TYPE_MOVE=3;

    /**同一存储设备内，一次性把文件移动过去，该类型不在区分任务类型里使用，只在任务类型中被使用*/
    public static final int TASK_TYPE_ONE_TIME_MOVE=4;

    /**创建目录*/
    public static final int TASK_TYPE_CREATE_DIR=5;

    /**创建文件*/
    public static final int TASK_TYPE_CREATE_FILE=6;

    /**删除文件到回收站*/
    public static final int TASK_TYPE_DELETE_TO_RECYCLE_BIN=7;


    @IntDef({TASK_TYPE_DELETE, TASK_TYPE_COPY, TASK_TYPE_MOVE, TASK_TYPE_CREATE_DIR, TASK_TYPE_CREATE_FILE, TASK_TYPE_DELETE_TO_RECYCLE_BIN,TASK_TYPE_ONE_TIME_MOVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TaskType{};

    /**无任何异常问题*/
    public static final int TASK_ERROR_NONE=8;

    /**重复文件存在*/
    public static final int TASK_ERROR_TYPE_SAME_FILE=9;

    /**任务读取或写入失败*/
    public static final int TASK_ERROR_IO_FAILD =10;

    /**文件未找到*/
    public static final int TASK_ERROR_FILE_NOT_FOUND =11;

    /**目录创建失败*/
    public static final int TASK_ERROR_DIR_CREATE_FAILD=12;

    /**其他类型的任务执行失败*/
    public static final int TASK_ERROR_OTHER_FAILD=13;

    @IntDef({TASK_ERROR_NONE, TASK_ERROR_TYPE_SAME_FILE, TASK_ERROR_IO_FAILD, TASK_ERROR_FILE_NOT_FOUND, TASK_ERROR_DIR_CREATE_FAILD, TASK_ERROR_OTHER_FAILD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TaskErrorType{};

    public String fromDir;//fromDir
    public String toDir;//toDir
    public long taskId;//id
    public String oldFileName;//oldFileName
    public String newFileName;//newFileName 如果文件名重复，则newFileName会和oldFileName不一致
    public @TaskType int taskType;//执行的任务类型
    public @TaskErrorType int taskErrorType=TASK_ERROR_NONE;//执行完成之后遇到的任务错误类型
    public long fileSize;//当前操作的文件大小
    public int fileCount=1;//当前操作的文件个数

    public FileTaskInfo(String fromDir, String toDir, long taskId, String oldFileName, String newFileName, @TaskType int taskType, long fileSize) {
        this.fromDir = fromDir;
        this.toDir = toDir;
        this.taskId = taskId;
        this.oldFileName = oldFileName;
        this.newFileName=newFileName;
        this.taskType = taskType;
        this.fileSize = fileSize;
    }

    public FileTaskInfo() {
    }

    /**获取当前任务的错误执行信息*/
    public String getErrorString(){
        String errorStr=null;
        String formatter=newFileName+"%s";
        switch (taskErrorType){
            case TASK_ERROR_DIR_CREATE_FAILD:
                errorStr=String.format(formatter, "创建目录失败");
                break;
            case TASK_ERROR_FILE_NOT_FOUND:
                errorStr=String.format(formatter, "文件未找到");
                break;
            case TASK_ERROR_NONE:
                break;
            case TASK_ERROR_OTHER_FAILD:
                errorStr=String.format(formatter, "发送未知错误");
                break;
            case TASK_ERROR_IO_FAILD:
                errorStr=String.format(formatter, "文件读写失败");
                break;
            case TASK_ERROR_TYPE_SAME_FILE:
                errorStr=String.format(formatter, "重复文件已存在");
                break;
        }
        return errorStr;
    }


    @Override
    public String toString(){
        String taskStr=fromDir+"/"+oldFileName;
        switch (taskType){
            case TASK_TYPE_COPY:
                taskStr+="  复制到  "+toDir+"/"+newFileName;
                break;
            case TASK_TYPE_CREATE_DIR:
                taskStr=toDir+"  创建目录  ";
                break;
            case TASK_TYPE_CREATE_FILE:
                taskStr=toDir+"  创建文件  ";
                break;
            case TASK_TYPE_DELETE:
                taskStr+="  删除文件  ";
                break;
            case TASK_TYPE_DELETE_TO_RECYCLE_BIN:

                break;
            case TASK_TYPE_MOVE:
                taskStr+="  移动到  "+toDir+"/"+newFileName;
                break;
        }
        return taskStr;
    }

    public static String getTaskTypeString(@TaskType int taskType){
        String taskStr="";
        switch (taskType) {
            case TASK_TYPE_COPY:
                taskStr += ResourceUtils.loadStr(R.string.operation_copy);
                break;
            case TASK_TYPE_CREATE_DIR:
                taskStr += ResourceUtils.loadStr(R.string.operation_create_dir);
                break;
            case TASK_TYPE_CREATE_FILE:
                taskStr += ResourceUtils.loadStr(R.string.operation_create_file);
                break;
            case TASK_TYPE_DELETE:
                taskStr += ResourceUtils.loadStr(R.string.operation_delete);
                break;
            case TASK_TYPE_DELETE_TO_RECYCLE_BIN:

                break;
            case TASK_TYPE_MOVE:
                taskStr += ResourceUtils.loadStr(R.string.operation_move);
                break;
        }
        return taskStr;
    }
}
