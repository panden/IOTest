package netac.fileutilsmaster.file.operation.dialogview;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import netac.fileutilsmaster.R;
import netac.fileutilsmaster.file.operation.FileOperationWrapper;
import netac.fileutilsmaster.file.operation.FileTaskInfo;
import netac.fileutilsmaster.utils.ResourceUtils;

/**
 * Created by siwei.zhao on 2016/12/1.
 */

public class CopyDialogViewBuilder extends DialogViewBuilder {

    public TextView operation_title_tv, operation_name_tv, operation_count_tv, from_tv, to_tv,
            task_name, task_progress_tv, operation_name, operation_progress_tv, remaining_time_tv, speed_tv, task_error_tv;

    public ProgressBar task_pb, operation_pb;

    public LinearLayout task_error_ll;

    public CopyDialogViewBuilder(Context context) {
        super(context);
    }

    @Override
    protected View getDialogView() {
        View baseView=View.inflate(mContext, R.layout.dialog_layout_copy, null);

        operation_title_tv= (TextView) baseView.findViewById(R.id.operation_title_tv);
        operation_name_tv= (TextView) baseView.findViewById(R.id.operation_name_tv);
        operation_count_tv= (TextView) baseView.findViewById(R.id.operation_count_tv);
        from_tv= (TextView) baseView.findViewById(R.id.from_tv);
        to_tv= (TextView) baseView.findViewById(R.id.to_tv);
        task_name= (TextView) baseView.findViewById(R.id.task_name);
        task_progress_tv= (TextView) baseView.findViewById(R.id.task_progress_tv);
        operation_name= (TextView) baseView.findViewById(R.id.operation_name);
        operation_progress_tv= (TextView) baseView.findViewById(R.id.operation_progress_tv);
        remaining_time_tv= (TextView) baseView.findViewById(R.id.remaining_time_tv);
        speed_tv= (TextView) baseView.findViewById(R.id.speed_tv);
        task_error_tv= (TextView) baseView.findViewById(R.id.task_error_tv);

        task_pb= (ProgressBar) baseView.findViewById(R.id.task_pb);
        operation_pb= (ProgressBar) baseView.findViewById(R.id.operation_pb);

        task_error_ll= (LinearLayout) baseView.findViewById(R.id.task_error_ll);

        return baseView;
    }

    /**更新所有的进度*/
    public void viewNotify(FileOperationWrapper wrapper, FileTaskInfo taskInfo){
        operation_count_tv.setText(ResourceUtils.loadStr(R.string.operation_excuting, FileTaskInfo.getTaskTypeString(wrapper.getTaskType())));
        operation_name.setText("总的任务名称");
    }

    /**更新当前执行的任务进度*/
    public void taskViewNotify(FileTaskInfo taskInfo){

    }

    /**更新当前总的任务量进度*/
    public void operationViewNotify(FileOperationWrapper wrapper){

    }
}
