package netac.fileutilsmaster.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import netac.fileutilsmaster.utils.DocumentTreePermissionUtils;

/**
 * 获取document permission的activity，其他继承他即可;<br/>
 * 在权限获取之后会自动将权限进行持久化，在持久化之后
 * 如果用户想代码去实现权限获取的功能：<br/>
 * 1.权限获取：DocumentTreePermissionUtils.getInstance().getDocumentTreePermission(Activity, RequestCode);<br/>
 * 2.权限接受：DocumentTreePermissionUtils.getInstance().onPermissionReciver(Intent, Context);
 * */
public class DocumentPermisssionActivity extends AppCompatActivity {

    /**获取document 访问权限*/
    protected void getDocumentPermission(){
        DocumentTreePermissionUtils.getInstance().getDocumentTreePermission(this, Integer.MIN_VALUE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==resultCode && requestCode==Integer.MIN_VALUE){
            //document permission
            DocumentTreePermissionUtils.getInstance().onPermissionReciver(data, this);
        }
    }

}
