package netac.fileutilsmaster.file.operation;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import netac.fileutilsmaster.R;
import netac.fileutilsmaster.file.FileCommon;
import netac.fileutilsmaster.utils.FileMainCallBackHandler;
import netac.fileutilsmaster.utils.Logger;
import netac.fileutilsmaster.utils.ResourceUtils;

import static netac.fileutilsmaster.file.operation.FileTaskInfo.TASK_ERROR_NONE;


/**
 * Created by siwei.zhao on 2016/11/16.<br>
 * 任务之间的关系：总任务里包含多个子任务（一个子任务就是用户选中的一个文件或者目录），子任务里有多个任务列表（任务列表就是需要执行的任务）;<br>
 * 任务量的计算：计算出需要操作的文件和总文件大小或者计算结果改变的时候都会回调operationFileCaculationChange();<br>
 * 任务的执行：当需要详细去执行某一个任务的时候会回调excuteSingleTask()并返回执行的结果<br>
 * 任务的生命周期：<br>
 * 任务被取消的时候，生命周期为onOperationCancle()——>onOperationCompleted();<br>
 * 任务正常执行完成的生命周期onOperationCompleted();
 */

public abstract class FileOperationTaskCommon extends FileOperantionCommon{

    private FileOperationStatusInfo mOperationStatusInfo;
    private List<FileOperationWrapper> mFileOperationWrappers;
    private @FileTaskInfo.TaskType int taskType;
    private String toDir;
    private Context mContext;
    private long taskId;

    /**用户选择的所有接下来的重复任务执行类型*/
    private @FileOperationWrapper.SameFileOperation int sameFileOperation= FileOperationWrapper.SAME_NONE_CHOOSE;

    private Handler fileOperationHandler;//总任务依次执行的handler
    private Handler singleOperationHandler;//单个子任务依次执行的handler

    protected FileOperationTaskCommon(Context context, List<FileCommon> fileCommons, String toDir, @FileTaskInfo.TaskType int taskType){
        mFileOperationWrappers=new ArrayList<>();
        mOperationStatusInfo=new FileOperationStatusInfo();
        this.mContext=context;
        this.taskId=System.currentTimeMillis();
        this.toDir=toDir;
        this.taskType=taskType;

        mOperationStatusInfo.setOperationType(taskType);
        mOperationStatusInfo.setToDir(toDir);
        mOperationStatusInfo.setFromDir(fileCommons.size()>1 ? fileCommons.get(0).getParent() : fileCommons.get(0).getAbsolutePath());

        for(FileCommon fileCommon : fileCommons){
            FileOperationWrapper wrapper=FileOperationWrapper.createFileOperation(context, fileCommon, toDir, taskType);
            mFileOperationWrappers.add(wrapper);
            mOperationStatusInfo.getOpeartionName().add(fileCommon.getName());
        }
        mFileOperationWrappers=sortOperation(mFileOperationWrappers);
    }

    public List<FileOperationWrapper> getFileOperationWrappers() {
        return mFileOperationWrappers;
    }

    public int getTaskType() {
        return taskType;
    }

    public String getToDir() {
        return toDir;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public long getTaskId() {
        return taskId;
    }

    @Override
    protected void fileOperantionRun() {

        int operationAllFileCount=0;
        long operationAllFileLength=0;

        //计算文件大小和需要处理的文件个数
        for(FileOperationWrapper wrapper : mFileOperationWrappers){
            wrapper.caculationFileInfo();
            operationAllFileCount+=wrapper.getTaskCount();
            operationAllFileLength+=wrapper.getTaskFileSize();
        }

        onMainShowOperationStatus(mContext);

        //计算总的完成，如果后续有任务跳过，则任务数和总文件大小也会变，进行回调========================================
        try {
            mOperationStatusInfo.setAllFileCount(operationAllFileCount);
            mOperationStatusInfo.setAllFileSize(operationAllFileLength);
            operationFileCaculation(operationAllFileCount, operationAllFileLength);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Looper.prepare();
        fileOperationHandler =new FileOperationHandler();
        //开始执行第一项任务,对第一个任务进行检查
        fileOperationHandler.sendMessage(fileOperationHandler.obtainMessage(FileOperationHandler.HANDLER_CHECK_SINGLE_OPERATION));
        Looper.loop();
    }

    private class FileOperationHandler extends Handler{

        private int checkOperationPosition;//检查子任务的下标

        private int startSingleOperationPosition;//执行子任务的下标


        /**任务停止运行*/
        public static final int HANDLER_TASK_STOP=1;

        /**开始执行一个单独的子任务*/
        public static final int HANDLER_START_SINGLE_OPERATION=2;

        /**任务执行完成*/
        public static final int HANDLER_TASK_COMPLETED=3;

        /**检查单个子任务*/
        public static final int HANDLER_CHECK_SINGLE_OPERATION=4;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HANDLER_TASK_STOP://所有任务停止
                    getLooper().quit();
                    operationCancle();
                    break;
                case HANDLER_START_SINGLE_OPERATION://开始执行单个子任务
                    if(mFileOperationWrappers.size()>0 && startSingleOperationPosition<mFileOperationWrappers.size() && !isCancleExcute()){
                        //去执行单个子任务
                        startSingleOperations(mFileOperationWrappers.get(startSingleOperationPosition));
                        startSingleOperationPosition++;
                    }else{
                        //任务执行完成
                        sendMessage(obtainMessage(HANDLER_TASK_COMPLETED));
                    }

                    break;
                case HANDLER_TASK_COMPLETED://任务执行完成
                    operationCompleted(isCancleExcute());
                    break;
                case HANDLER_CHECK_SINGLE_OPERATION://开启去检查单个子任务
                    if(checkOperationPosition<mFileOperationWrappers.size() && !isCancleExcute()){
                        onMainOperationCheck(mFileOperationWrappers.get(checkOperationPosition));
                        checkOperationPosition++;
                    }else{
                        //任务检查完成，开始去执行子任务
                        Logger.d("check end, start operation wrapper");
                        sendMessage(obtainMessage(HANDLER_START_SINGLE_OPERATION, mFileOperationWrappers.get(0)));
                        onMainShowOperationStatus(mContext);
                    }

                    break;
            }
        }
    }

    @Override
    public void stopOperation() {
        super.stopOperation();
        if(fileOperationHandler!=null){
            fileOperationHandler.sendMessage(fileOperationHandler.obtainMessage(FileOperationHandler.HANDLER_TASK_STOP));
        }else{
            operationCancle();
        }
    }

    /**操作被取消*/
    private void operationCancle(){
        Logger.d("file operation cancle");
        onOperationCancle();
    }

    /**任务操作只需完成*/
    private void operationCompleted(boolean isCancleCompleted){
        Logger.d("file operation completed isCancle=%s", String.valueOf(isCancleCompleted));
        onOperationCompleted(isCancleCompleted);
    }

    /**需要操作的文件计算改变
     *@param filesCount 计算出的需要操作的文件的总个数
     *@param filesSzie 计算出需要操作的文件的总大小
     * */
    private void operationFileCaculation(int filesCount, long filesSzie){
        try {
            Logger.d("wrappers caculater change. file count=%s, file size=%s", String.valueOf(filesCount), String.valueOf(Formatter.formatFileSize(mContext, filesSzie)));
            operationFileCaculationChange(filesCount, filesSzie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**文件操作被取消*/
    protected abstract void onOperationCancle();

    /**文件操作执行完成*/
    protected abstract void onOperationCompleted(boolean isCancleCompleted);

    /**需要操作的文件计算改变
     *@param filesCount 计算出的需要操作的文件的总个数
     *@param filesSzie 计算出需要操作的文件的总大小
     * */
    protected abstract void operationFileCaculationChange(int filesCount, long filesSzie);

    /**开始一个子任务的执行，如果任务只执行了一半则继续后面的去执行
     *@param wrapper 需要执行的任务
     *@return boolean 子任务执行状态
     * */
    private void startSingleOperations(FileOperationWrapper wrapper){
        if(wrapper==null)return;
        if(singleOperationHandler==null)singleOperationHandler=new SingleOperationHandler();
        if(wrapper.getSameOperation()==FileOperationWrapper.SAME_SKIP_TASK){//任务跳过，直接开始下个wrapper
            fileOperationHandler.sendMessage(fileOperationHandler.obtainMessage(FileOperationHandler.HANDLER_START_SINGLE_OPERATION));
            return;
        }
        if(wrapper.getFileTaskInfos()==null || wrapper.getFileTaskInfos().size()==0)wrapper.caculationTasks();//计算需要执行的子任务内所有的任务量
        Logger.d("**********start next fileoperationwrapper=%s  taskSize=%s**********", wrapper.toString(), wrapper.getFileTaskInfos().size());
        singleOperationHandler.sendMessage(singleOperationHandler.obtainMessage(SingleOperationHandler.NEXT_SINGLE_TASK, wrapper));
    };

    private class SingleOperationHandler extends Handler{

        /**开始执行下一个任务*/
        public static final int NEXT_SINGLE_TASK=1;

        /**单个子任务执行完成*/
        public static final int SINGLE_OPERATION_END=2;

        /**重新执行该任务*/
        public static final int RESTART_SINGLE_TASK=3;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case NEXT_SINGLE_TASK://执行下个任务
                    if(msg.obj!=null && msg.obj instanceof FileOperationWrapper){
                        FileOperationWrapper wrapper=(FileOperationWrapper) msg.obj;
                        if((wrapper.getLocalTaskExcutePosition()+1)>=wrapper.getFileTaskInfos().size()){
                            //子任务执行完
                            sendMessage(obtainMessage(SINGLE_OPERATION_END, wrapper));
                        }else{
                            //设置需要执行任务的下标
                            wrapper.setLocalTaskExcutePosition(wrapper.getLocalTaskExcutePosition()+1);
                            mOperationStatusInfo.setCurrentOperationTaskInfo(wrapper.getFileTaskInfos().get(wrapper.getLocalTaskExcutePosition()));
                            excuteSingleTask(wrapper, false);
                        }
                    }
                    break;
                case SINGLE_OPERATION_END://任务执行结束
                    fileOperationHandler.sendMessage(fileOperationHandler.obtainMessage(FileOperationHandler.HANDLER_CHECK_SINGLE_OPERATION));
                    break;
                case RESTART_SINGLE_TASK://重新执行该任务
                    FileOperationWrapper wrapper=(FileOperationWrapper) msg.obj;
                    if(wrapper.getLocalTaskExcutePosition()>wrapper.getFileTaskInfos().size()){
                        //子任务执行完成
                        sendMessage(obtainMessage(SINGLE_OPERATION_END, wrapper));
                    }else{
                        //重新执行该任务，如果文件已存在则直接覆盖
                        excuteSingleTask(wrapper, true);
                    }
                    break;
            }
        }
    }

    /**开始去执行下一个子任务的任务
     * @param wrapper 需要执行的子任务
     * @param isOverried 当操作的文件已存在是否直接覆盖，在重新执行当前任务的时候会遇到是否需要直接覆盖的问题。
     * */
    private @FileTaskInfo.TaskErrorType int excuteSingleTask(FileOperationWrapper wrapper, boolean isOverried){
        mOperationStatusInfo.setCurrentFileOperationSize(0);
        if(isCancleExcute()){//任务被取消
            Logger.d("===excuteSingleTask isCancleExcute");
            singleOperationHandler.sendMessage(singleOperationHandler.obtainMessage(SingleOperationHandler.SINGLE_OPERATION_END, wrapper));
            return TASK_ERROR_NONE;
        }else{
            FileTaskInfo taskInfo=wrapper.getFileTaskInfos().get(wrapper.getLocalTaskExcutePosition());
            //开始去执行子任务的制定的任务信息
            final int error = excuteSingleTask(mOperationStatusInfo, taskInfo, isOverried);
            if(error!= TASK_ERROR_NONE)showExcuteTaskErrorDialog(error, wrapper, wrapper.getContext());
            else singleOperationHandler.sendMessage(singleOperationHandler.obtainMessage(SingleOperationHandler.NEXT_SINGLE_TASK, wrapper));
            //Logger.d("excuteSingleTask task=%s error=%s" , taskInfo.toString(), String.valueOf(error!=TASK_ERROR_NONE));
            return error;
        }
    }

    /**根据不同的错误类型去显示不同的执行错误弹窗，让用户去选择接下来的操作，并执行接下来的操作*/
    private void showExcuteTaskErrorDialog(@FileTaskInfo.TaskErrorType int taskErrorType, final FileOperationWrapper wrapper, final Context context){
        onMainDismissOperationStatus();
        final FileTaskInfo taskInfo=wrapper.getFileTaskInfos().get(wrapper.getLocalTaskExcutePosition());
        FileMainCallBackHandler.getInstance().onMainCallBackRun(new Runnable() {
            @Override
            public void run() {
                MaterialDialog.Builder builder=new MaterialDialog.Builder(context);

                builder.title("执行错误").content(taskInfo.getErrorString()).negativeText("取消").positiveText("重试").autoDismiss(false).onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (which){
                            case NEGATIVE://取消任务

                                break;
                            case POSITIVE://重试任务
                                singleOperationHandler.sendMessage(singleOperationHandler.obtainMessage(SingleOperationHandler.NEXT_SINGLE_TASK, wrapper));
                                break;
                        }
                    }
                }).build().show();
            }
        });
    }

    /**执行子任务里的单项任务
     * @param taskInfo 执行的task信息
     * @param isOverried 当执行任务的文件已存在的时候是否进行直接覆盖，当重复执行当前任务的时候会遇到这个问题
     * @return int 返回任务执行的状态
     * */
    protected abstract @FileTaskInfo.TaskErrorType int excuteSingleTask(FileOperationStatusInfo statusInfo, FileTaskInfo taskInfo, boolean isOverried);


    /**任务排序，文件夹任务排在前，文件任务排在后*/
    private List<FileOperationWrapper> sortOperation(@NonNull List<FileOperationWrapper> wrappers){
        List<FileOperationWrapper> dirOperation=new ArrayList<>();
        List<FileOperationWrapper> fileOperation=new ArrayList<>();
        for(FileOperationWrapper wrapper : wrappers){
            File toFile=new File(wrapper.getToDir(), wrapper.getNewFileName());
            if(toFile.isFile())fileOperation.add(wrapper);
            else dirOperation.add(wrapper);
        }
        dirOperation.addAll(fileOperation);
        return dirOperation;
    }

    public void onMainDismissOperationStatus(){
        FileMainCallBackHandler.getInstance().onMainCallBackRun(new Runnable() {
            @Override
            public void run() {
                try {
                    Logger.d("onMain Thrad dismiss operation status dialog.");
                    dismissOperationTaskStatusDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**在主线程去显示当前任务执行的状态*/
    public void onMainShowOperationStatus(final Context context){
        FileMainCallBackHandler.getInstance().onMainCallBackRun(new Runnable() {
            @Override
            public void run() {
                try {
                    Logger.d("onMain Thread show operation status dialog.");
                    showOperationTaskStatusDialog(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**显示当前任务执行的状态，如果弹窗消失了则弹出，如果当前弹窗被点击过隐藏，则不再进行显示*/
    protected abstract void showOperationTaskStatusDialog(Context context);

    /**让当前任务执行的状态弹窗消失*/
    protected abstract void dismissOperationTaskStatusDialog();


    /**在主线程执行，去检查任务的重复性，并弹窗让用户去选择处理方式*/
    protected void onMainOperationCheck(final FileOperationWrapper wrapper){
        FileMainCallBackHandler.getInstance().onMainCallBackRun(new Runnable() {
            @Override
            public void run() {
                operationCheckDialog(wrapper);
            }
        });
    }

    /**弹窗询问用户，当单个子任务重复的文件名存在的时候，检查的子任务顺序必须要先文件夹子任务后文件子任务*/
    private void operationCheckDialog(final FileOperationWrapper wrapper){
        if(wrapper==null)return;
        Logger.d("check wrapper=%s operation exists=%s", wrapper.toString(), wrapper.checkOperation());
        if(wrapper.checkOperation()){
            if(sameFileOperation!=FileOperationWrapper.SAME_NONE_CHOOSE){
                //已选择了应用到所有，则不需要继续询问。
                wrapper.setSameOperation(sameFileOperation);
                //通过handler去实现循环
                fileOperationHandler.sendMessage(fileOperationHandler.obtainMessage(FileOperationHandler.HANDLER_CHECK_SINGLE_OPERATION, wrapper));
                return;
            }
            onMainDismissOperationStatus();
            final boolean isFile=new File(wrapper.getToDir(), wrapper.getNewFileName()).isFile();
            final boolean[] checkAll = {false};
            MaterialDialog.Builder builder=new MaterialDialog.Builder(wrapper.getContext());
            builder.title(ResourceUtils.loadStr(R.string.file_operation_title))
                    .content(ResourceUtils.loadStr(R.string.some_file_exists, wrapper.getNewFileName()))
                    .neutralText(ResourceUtils.loadStr(R.string.skip))
                    .positiveText(ResourceUtils.loadStr(R.string.overried))
                    .autoDismiss(false)
                    .checkBoxPrompt(ResourceUtils.loadStr(R.string.use_all), false, new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            checkAll[0] =isChecked;
                        }
                    })
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            switch (which){
                                case POSITIVE://覆盖(文件和目录都进行覆盖)
                                    //应用到所有
                                    if(checkAll[0])sameFileOperation=FileOperationWrapper.SAME_FILE_DIR_OVERRIED;
                                    wrapper.setSameOperation(FileOperationWrapper.SAME_FILE_DIR_OVERRIED);
                                    fileOperationHandler.sendMessage(fileOperationHandler.obtainMessage(FileOperationHandler.HANDLER_CHECK_SINGLE_OPERATION, wrapper));
                                    break;
                                case NEGATIVE://重命名(只包含文件)
                                    //生成一个新的文件名并确保该文件名对应的文件不存在
                                    //应用到所有
                                    if(checkAll[0])sameFileOperation=FileOperationWrapper.SAME_FILE_RENAME;
                                    wrapper.setSameOperation(FileOperationWrapper.SAME_FILE_RENAME);
                                    fileOperationHandler.sendMessage(fileOperationHandler.obtainMessage(FileOperationHandler.HANDLER_CHECK_SINGLE_OPERATION, wrapper));
                                    break;
                                case NEUTRAL://跳过
                                    //应用到所有
                                    if(checkAll[0])sameFileOperation=FileOperationWrapper.SAME_SKIP_TASK;
                                    wrapper.setSameOperation(FileOperationWrapper.SAME_SKIP_TASK);
                                    //任务跳过，则对于需要执行的文件数和文件大小也会变化，需要从新计算结果
                                    mOperationStatusInfo.setAllFileCount(mOperationStatusInfo.getAllFileCount()-wrapper.getTaskCount());
                                    mOperationStatusInfo.setAllFileSize(mOperationStatusInfo.getAllFileSize()-wrapper.getTaskFileSize());
                                    operationFileCaculation(mOperationStatusInfo.getAllFileCount(), mOperationStatusInfo.getAllFileSize());
                                    fileOperationHandler.sendMessage(fileOperationHandler.obtainMessage(FileOperationHandler.HANDLER_CHECK_SINGLE_OPERATION, wrapper));
                                    break;
                            }
                        }
                    });
            if(isFile)builder.negativeText(ResourceUtils.loadStr(R.string.rename));
            MaterialDialog operationCheckDialog=builder.build();
            operationCheckDialog.show();
        }else {
            fileOperationHandler.sendMessage(fileOperationHandler.obtainMessage(FileOperationHandler.HANDLER_CHECK_SINGLE_OPERATION, wrapper));
        }
    }

    /**任务确认的时候，确认任务的列表中文件夹任务必须在前，文件任务必须在后</br>
     * 在确认文件夹重复任务的时候，文件夹只包含覆盖和取消的功能
     * 在确认文件重复任务的时候，文件包含覆盖、重命名、取消的功能*/
//    private void operationCheckDialog(@NonNull final List<FileOperationWrapper> wrappers, int pos){
//        if(wrappers==null)return;
//        else if(wrappers.size()==0 || pos==wrappers.size()){
//            //任务检查执行完成，在方法里去执行需要执行的任务
//            //=======================NEED CODE======================
//            return;
//        }
//        final FileOperationWrapper wrapper=wrappers.get(pos);
//        pos++;
//        if(wrapper.checkOperation()){
//            final boolean isFile=new File(wrapper.getToDir(), wrapper.getNewFileName()).isFile();
//            final boolean[] checkAll = {false};
//            MaterialDialog.Builder builder=new MaterialDialog.Builder(wrapper.getContext());
//            builder.title(ResourceUtils.loadStr(R.string.file_operation_title))
//                    .content(ResourceUtils.loadStr(R.string.some_file_exists, wrapper.getNewFileName()))
//                    .neutralText(ResourceUtils.loadStr(R.string.cancle))
//                    .positiveText(ResourceUtils.loadStr(R.string.overried))
//                    .autoDismiss(false)
//                    .checkBoxPrompt(ResourceUtils.loadStr(R.string.use_all), false, new CompoundButton.OnCheckedChangeListener() {
//                        @Override
//                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                            checkAll[0] =isChecked;
//                        }
//                    })
//                    .onAny(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            int tagNextPos= (int) dialog.getTitleView().getTag();
//                            dialog.dismiss();
//                            switch (which){
//                                case POSITIVE://覆盖(文件和目录都进行覆盖)
//                                    if(checkAll[0]){//应用到所有
//                                        for(int i=tagNextPos-1; i< wrappers.size(); i++)wrappers.get(i).setSameOperation(FileOperationWrapper.SAME_FILE_DIR_OVERRIED);
//                                        operationCheckDialog(wrappers, wrappers.size());
//                                    }else{
//                                        wrapper.setSameOperation(FileOperationWrapper.SAME_FILE_DIR_OVERRIED);
//                                        operationCheckDialog(wrappers, tagNextPos);
//                                    }
//                                    break;
//                                case NEGATIVE://重命名(只包含文件)
//                                    //生成一个新的文件名并确保该文件名对应的文件不存在
//                                    if(checkAll[0]){//应用到所有
//                                        for(int i=tagNextPos-1; i< wrappers.size(); i++){
//                                            //自动生成一个新的文件名且和之前的文件不重复
//                                            FileOperationWrapper operationWrapper=wrappers.get(i);
//                                            operationWrapper.setNewFileName(FileUtils.getNewFileName(operationWrapper.getToDir(), operationWrapper.getNewFileName()));
//                                            operationWrapper.setSameOperation(FileOperationWrapper.SAME_FILE_RENAME);
//                                        }
//                                        operationCheckDialog(wrappers, wrappers.size());
//                                    }else{
//                                        wrapper.setNewFileName(FileUtils.getNewFileName(wrapper.getToDir(), wrapper.getNewFileName()));
//                                        wrapper.setSameOperation(FileOperationWrapper.SAME_FILE_RENAME);
//                                        operationCheckDialog(wrappers, tagNextPos);
//                                    }
//                                    break;
//                                case NEUTRAL://跳过
//                                    if(checkAll[0]){
//                                        wrappers.clear();
//                                        for(int i=tagNextPos-1; i< wrappers.size(); i++){
//                                            FileOperationWrapper operationWrapper=wrappers.get(i);
//                                            operationWrapper.setSameOperation(FileOperationWrapper.SAME_SKIP_TASK);
//                                        }
//                                        operationCheckDialog(wrappers, wrappers.size());
//                                    }else{
//                                        wrappers.remove(tagNextPos);
//                                        operationCheckDialog(wrappers, tagNextPos);
//                                    }
//                                    break;
//                            }
//                        }
//                    });
//            if(isFile)builder.negativeText(ResourceUtils.loadStr(R.string.rename));
//            MaterialDialog operationCheckDialog=builder.build();
//            operationCheckDialog.getTitleView().setTag(pos);
//            operationCheckDialog.show();
//        }else {
//            if(pos<wrappers.size()) operationCheckDialog(wrappers, pos);
//        }
//    }
}
