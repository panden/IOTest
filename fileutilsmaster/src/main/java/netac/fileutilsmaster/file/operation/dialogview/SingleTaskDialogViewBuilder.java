package netac.fileutilsmaster.file.operation.dialogview;

import android.content.Context;
import android.view.View;

/**
 * Created by siwei.zhao on 2016/12/24.
 * 单任务状态信息提示
 */

public class SingleTaskDialogViewBuilder extends DialogViewBuilder{

    private View mBaseView;

    public SingleTaskDialogViewBuilder(Context context) {
        super(context);
    }

    @Override
    protected View getDialogView() {
        return null;
    }

}
