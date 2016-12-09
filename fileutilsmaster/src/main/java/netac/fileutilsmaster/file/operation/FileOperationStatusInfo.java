package netac.fileutilsmaster.file.operation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by siwei.zhao on 2016/11/28.
 * 文件操作执行的状态信息
 */

public class FileOperationStatusInfo {

    private List<String> opeartionName=new ArrayList<>();//执行任务的任务名

    private @FileTaskInfo.TaskType int operationType;//任务类型

    private String fromDir;//from目录

    private String toDir;//to目录

    private int allFileCount;//总的文件个数

    private long allFileSize;//总的文件大小

    private int operationFileCount;//已操作的文件个数

    /**现在正在执行的任务信息*/
    private FileTaskInfo currentOperationTaskInfo;

    private long currentFileOperationSize;//当前已操作文件的大小

    public @FileTaskInfo.TaskType int getOperationType() {
        return operationType;
    }

    public void setOperationType(@FileTaskInfo.TaskType int operationType) {
        this.operationType = operationType;
    }

    public FileTaskInfo getCurrentOperationTaskInfo() {
        return currentOperationTaskInfo;
    }

    public void setCurrentOperationTaskInfo(FileTaskInfo currentOperationTaskInfo) {
        this.currentOperationTaskInfo = currentOperationTaskInfo;
    }

    protected void setAllFileCount(int allFileCount) {
        this.allFileCount = allFileCount;
    }

    protected void setAllFileSize(long allFileSize) {
        this.allFileSize = allFileSize;
    }

    protected void setOperationFileCount(int operationFileCount) {
        this.operationFileCount = operationFileCount;
    }

    protected void setCurrentFileOperationSize(long currentFileOperationSize) {
        this.currentFileOperationSize = currentFileOperationSize;
    }

    public long getAllFileSize() {
        return allFileSize;
    }

    public List<String> getOpeartionName() {
        return opeartionName;
    }

    public void setOpeartionName(List<String> opeartionName) {
        this.opeartionName = opeartionName;
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

    public int getAllFileCount() {
        return allFileCount;
    }

    public int getOperationFileCount() {
        return operationFileCount;
    }

    public long getCurrentFileOperationSize() {
        return currentFileOperationSize;
    }

    /**获取总的执行进度*/
    public float getFileOperationProgress(){
        if(operationFileCount==0)return 0;
        else return (float) (operationFileCount *1.0/allFileCount);
    }

    /**获取当前操作的文件的执行的进度*/
    public float getCurrentFileOperationProgress(){
        if(currentOperationTaskInfo==null || currentFileOperationSize==0)return 0;
        else{
            return (float) (currentFileOperationSize*1.0/currentOperationTaskInfo.fileSize);
        }
    }
}
