package netac.fileutilsmaster.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.util.List;

import netac.fileutilsmaster.file.FileFactory;
import netac.fileutilsmaster.file.vo.StorageDeviceInfo;

public class GetPathFromUri4kitkat {

    private static final String PATH_TREE = "tree";

    /**
     * 专为Android6.0设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        final boolean isLolipop = Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP;

        Logger.i("is tree="+isTreeUri(uri)+"  is documet="+DocumentsContract.isDocumentUri(context, uri));

        // DocumentProvider
        if (isKitKat && (DocumentsContract.isDocumentUri(context, uri) || isTreeUri(uri))) {
            // ExternalStorageProvider
            //document uri在19到21只有document uri的地址，在21之后就增加了tree uri的地址
            if (isExternalStorageDocument(uri)) {
                if(isLolipop && isTreeUri(uri)){
                    //tree uri API 21
                    String docId=DocumentsContract.getTreeDocumentId(uri);
                    return getDataColumUsingTree(context, uri, docId, null, null);
                }else if(DocumentsContract.isDocumentUri(context, uri)){
                    //document uri
                    final String docId = DocumentsContract.getDocumentId(uri);
                    Logger.i("doc docid="+docId);
                    final String[] split = docId.split(":");
//                    final String type = split[0];
//                    List<String> strs=uri.getPathSegments();
//                    for(String str:strs)Logger.i("getPathSegments str="+str);

//                    if ("primary".equalsIgnoreCase(type)) {
//                        return Environment.getExternalStorageDirectory() + "/" + split[1];
//                    }
                    return FileFactory.getInstance().getFileWrapper().getStorageDevice(StorageDeviceInfo.StorageDeviceType.ExtrageDevice).getRootPath() + "/" + split[1];
                }


            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    //判断当前地址是否为tree uri地址,isTreeUri()在DocumentsContract中一直被隐藏，在API 24才放出来
    public static boolean isTreeUri(Uri uri){
        final List<String> paths = uri.getPathSegments();
        return (paths.size() >= 2 && PATH_TREE.equals(paths.get(0)));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String getDataColumUsingTree(Context context, Uri uri, String documentId, String selection, String[] selectionArgs){
        String path="";
        Cursor cursor=null;
        String displayName="_display_name";
        String document_id="document_id";
        String primaryKey="primary";
        String[] colums=new String[]{document_id, displayName};
        Logger.i("search uri=%s", DocumentsContract.buildDocumentUriUsingTree(uri, documentId).toString());
        try {
            cursor=context.getContentResolver().query(DocumentsContract.buildDocumentUriUsingTree(uri, documentId), null, selection, selectionArgs, null);
            if(cursor!=null && cursor.moveToFirst()){
//                for (int i=0; i< cursor.getColumnCount(); i++){
//                    Logger.i("col=%s  value=%s;", cursor.getColumnName(i), cursor.getString(cursor.getColumnIndex(cursor.getColumnName(i))));
//                }
                String name=cursor.getString(cursor.getColumnIndex(displayName));
                if(documentId.startsWith(primaryKey)){
                    //内部存储
                    path=System.getenv("EMULATED_STORAGE_TARGET")+"/"+name;
                }else{
                    //外部存储
                    path=System.getenv("ANDROID_STORAGE")+"/"+name;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor!=null)cursor.close();
        }
        Logger.d("getDataColumUsingTree path=%s", path);
        return path;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, null, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
//                for (int i=0; i< cursor.getColumnCount(); i++){
//                    Logger.i("col=%s  value=%s;", cursor.getColumnName(i), cursor.getString(cursor.getColumnIndex(cursor.getColumnName(i))));
//                }
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}