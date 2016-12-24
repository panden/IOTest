package netac.fileutilsmaster.file.sort;

import android.support.annotation.Nullable;

import netac.fileutilsmaster.file.FileCommon;

/**
 * Created by siwei.zhao on 2016/12/24.
 * 按照文件的最后修改时间由短到长去进行排序
 */

public class FileLastModifyBubbleSort extends FileCommon.BubbleSort{
    @Override
    protected boolean changeSort(@Nullable FileCommon common1, @Nullable FileCommon common2) {
        if(common1==null || common2==null)return false;
        return common2.lastModify()>common1.lastModify();
    }
}
