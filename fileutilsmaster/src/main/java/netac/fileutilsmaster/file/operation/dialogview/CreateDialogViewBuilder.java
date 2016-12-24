package netac.fileutilsmaster.file.operation.dialogview;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import netac.fileutilsmaster.R;

/**
 * Created by siwei.zhao on 2016/12/24.
 * 创建文件或者文件夹的dialog view builder
 */

public class CreateDialogViewBuilder extends DialogViewBuilder {

    private boolean isCreateFile;//记录当前是创建文件还是创建文件夹

    private View mBaseView;
    public TextView operation_title_tv, create_name_et, task_error_tv;
    public LinearLayout task_error_ll;

    public CreateDialogViewBuilder(Context context, boolean isCreateFile) {
        super(context);
        this.isCreateFile=isCreateFile;
    }

    @Override
    protected View getDialogView() {
        mBaseView=View.inflate(mContext, R.layout.dialog_layout_create, null);
        operation_title_tv= (TextView) mBaseView.findViewById(R.id.operation_title_tv);
        create_name_et= (TextView) mBaseView.findViewById(R.id.create_name_et);
        task_error_tv= (TextView) mBaseView.findViewById(R.id.task_error_tv);
        task_error_ll= (LinearLayout) mBaseView.findViewById(R.id.task_error_ll);
        return mBaseView;
    }

    /**判断当前是否是创建文件还是创建文件夹*/
    public boolean isCreateFile(){
        return isCreateFile;
    }
}
