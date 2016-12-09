package netac.fileutilsmaster.file.operation.dialogview;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

/**
 * Created by siwei.zhao on 2016/12/1.
 */

public abstract class DialogViewBuilder {

    protected Context mContext;
    private View mBaseView;
    protected Dialog mDialog;

    public DialogViewBuilder(Context context){
        mContext=context;
    }

    public void bindDialog(Dialog dialog){
        mDialog=dialog;
    }

    public View getView(){
        if(mBaseView==null)mBaseView=getDialogView();
        return mBaseView;
    }

    /**获取弹窗的view*/
    protected abstract View getDialogView();

}
