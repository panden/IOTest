package netac.fileutilsmaster.file.serializable;

import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import java.io.Serializable;

/**
 * Created by siwei.zhao on 2016/9/19.
 */
public class DocumentUriSerializable implements Serializable{

    private String authority;
    private String documentId;

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public DocumentUriSerializable(){};

    public DocumentUriSerializable(Uri uri, boolean isTree) {
        authority=uri.getAuthority();
        documentId=getDocumentID(uri, isTree);
    }

    private String getDocumentID(Uri uri,boolean isTree){
        String id="";
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP && isTree){
            id= DocumentsContract.getTreeDocumentId(uri);
        }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT && !isTree){
            id=DocumentsContract.getDocumentId(uri);
        }
        return id;
    }


}
