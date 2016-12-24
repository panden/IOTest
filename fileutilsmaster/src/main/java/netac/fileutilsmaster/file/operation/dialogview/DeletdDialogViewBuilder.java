package netac.fileutilsmaster.file.operation.dialogview;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import netac.fileutilsmaster.R;

/**
 * Created by siwei.zhao on 2016/12/1.
 * 删除文件夹的dialog view builder
 */

public class DeletdDialogViewBuilder extends DialogViewBuilder{

    private View mBaseView;
    public TextView operation_title_tv, operation_name_tv, operation_count_tv, operation_name, operation_progress_tv, task_error_tv;
    public ProgressBar operation_pb;
    public LinearLayout task_error_ll;

    public DeletdDialogViewBuilder(Context context) {
        super(context);
    }

    @Override
    protected View getDialogView() {
        mBaseView=View.inflate(mContext, R.layout.dialog_layout_delete, null);
        operation_title_tv= (TextView) mBaseView.findViewById(R.id.operation_title_tv);
        operation_name_tv= (TextView) mBaseView.findViewById(R.id.operation_name_tv);
        operation_count_tv= (TextView) mBaseView.findViewById(R.id.operation_count_tv);
        operation_name= (TextView) mBaseView.findViewById(R.id.operation_name);
        operation_progress_tv= (TextView) mBaseView.findViewById(R.id.operation_progress_tv);
        task_error_tv= (TextView) mBaseView.findViewById(R.id.task_error_ll);
        operation_pb= (ProgressBar) mBaseView.findViewById(R.id.operation_pb);
        task_error_ll= (LinearLayout) mBaseView.findViewById(R.id.task_error_ll);
        return mBaseView;
    }

}
