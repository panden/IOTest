package netac.fileutilsmaster.utils;

import android.app.Application;
import android.support.annotation.StringRes;

/**
 * Created by siwei.zhao on 2016/11/15.
 * 资源工具类，帮助资源的快速加载
 */

public class ResourceUtils {

    private static Application sApplication;

    public static void initResourece(Application application){
        sApplication=application;
    }

    public static String loadStr(@StringRes int res){
        return sApplication.getString(res);
    }

    public static String loadStr(@StringRes int res, Object... params){
        return String.format(sApplication.getString(res), params);
    }
}
