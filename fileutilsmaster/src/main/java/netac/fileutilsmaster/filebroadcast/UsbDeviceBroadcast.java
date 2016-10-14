package netac.fileutilsmaster.filebroadcast;

import android.content.IntentFilter;
import android.hardware.usb.UsbManager;

/**
 * Created by siwei.zhao on 2016/9/14.
 */
public class UsbDeviceBroadcast extends BroadCastReciverManager.BroadCastReciverCommon{
    @Override
    public IntentFilter getRegisterIntentFilter() {
        IntentFilter filter=new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        return filter;
    }

    @Override
    public IntentFilter getLocalRegisterIntentFilter() {
        return null;
    }
}
