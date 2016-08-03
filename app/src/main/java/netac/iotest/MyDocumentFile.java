package netac.iotest;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsProvider;

import java.io.File;

/**
 * Created by siwei.zhao on 2016/6/28.
 */
public class MyDocumentFile {

    private ContentProvider mProvider;
    private Uri mFilesUri;
    private File mFile;
    private static Uri mRootFilesUri;//外置SD卡或者OTG操作Uri

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public MyDocumentFile(ContentProvider provider, File file){
        mProvider=provider;
        mFile=file;
//        mFilesUri= DocumentsContract.createDocument(mProvider, mRootFilesUri, "", mFile.getName());
    }

    public static void setRootFileUri(Uri uri){
        mRootFilesUri=uri;
        DocumentsProvider provider;
    }
}
