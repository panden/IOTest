package netac.fileutilsmaster.exception;

/**
 * Created by siwei.zhao on 2016/12/1.
 * 无DocumentFile操作权限，在Android 4.4以上在进行文件操作的时候会抛出
 */

public class NoDocumentPermissionException extends Exception {

    private String mDetailMessage="document file not has permission, please get permission.";

    public NoDocumentPermissionException() {
    }

    public NoDocumentPermissionException(String detailMessage) {
        super(detailMessage);
    }

    public NoDocumentPermissionException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NoDocumentPermissionException(Throwable throwable) {
        super(throwable);
    }
}
