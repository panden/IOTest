package netac.fileutilsmaster.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import netac.fileutilsmaster.R;
import netac.fileutilsmaster.file.vo.StorageDeviceInfo;
import netac.fileutilsmaster.utils.DocumentTreePermissionUtils;
import netac.fileutilsmaster.utils.ResourceUtils;
import netac.fileutilsmaster.views.CircleIndicator;

/**
 * 获取document permission的activity，其他继承他即可;<br/>
 * 在权限获取之后会自动将权限进行持久化，在持久化之后
 * 如果用户想代码去实现权限获取的功能：<br/>
 * 1.权限获取：DocumentTreePermissionUtils.getInstance().getDocumentTreePermission(Activity, RequestCode);<br/>
 * 2.权限接受：DocumentTreePermissionUtils.getInstance().onPermissionReciver(Intent, Context);
 * */
public class DocumentPermissionActivity extends Activity {

    private MaterialDialog mDialog;
    private Map<String, Integer[]> images;
    private StorageDeviceInfo.StorageDeviceType mStorageDeviceType;
    private MyPagerAdapter adapter;
    private CircleIndicator indicator;

    public static final String EXTRAL_STORAGE_TYPE="extral_storage_type";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_permission);
        System.out.println("DocumentPermisssionActivity onCreate");
        initIntriductoryImage();
        initMaterialDialog();
    }

    private void initIntriductoryImage(){
        images=new HashMap<>();

        //samsung
        Integer[] android_sd=new Integer[]{R.drawable.android_1, R.drawable.android_2, R.drawable.android_3, R.drawable.android_4, R.drawable.android_sd_5, R.drawable.android_sd_6};
        images.put("android_"+String.valueOf(StorageDeviceInfo.StorageDeviceType.SecondExtrageDevice), android_sd);
        Integer[] android_usb=new Integer[]{R.drawable.android_1, R.drawable.android_2, R.drawable.android_3, R.drawable.android_4, R.drawable.android_usb_5, R.drawable.android_usb_6};
        images.put("android_"+String.valueOf(StorageDeviceInfo.StorageDeviceType.UsbDevice), android_usb);

        //huawei
        Integer[] huawei_sd=new Integer[]{R.drawable.huawei_1, R.drawable.huawei_2, R.drawable.huawei_3, R.drawable.huawei_5, R.drawable.huawei_sd_6, R.drawable.huawei_sd_7};
        images.put("huawei_"+String.valueOf(StorageDeviceInfo.StorageDeviceType.SecondExtrageDevice), huawei_sd);
        Integer[] huawei_usb=new Integer[]{R.drawable.huawei_1, R.drawable.huawei_2, R.drawable.huawei_3, R.drawable.huawei_5, R.drawable.huawei_usb_6, R.drawable.huawei_usb_7};
        images.put("huawei_"+String.valueOf(StorageDeviceInfo.StorageDeviceType.UsbDevice), huawei_usb);

        //nubia

        //xiaomi
    }

    /**获取引导页的图片
     * @param phoneModal 手机品牌
     * @param type 存储设备的类型
     * */
    private Integer[] getIntroductoryImage(String phoneModal, StorageDeviceInfo.StorageDeviceType type){
        String key=phoneModal.toLowerCase()+"_"+String.valueOf(type);
        if(!images.containsKey(key)){
            key="android_"+String.valueOf(type);
            return images.get(key);
        }
        return images.get(key);
    }

    private void initMaterialDialog(){

        int type=getIntent().getIntExtra(EXTRAL_STORAGE_TYPE, StorageDeviceInfo.StorageDeviceType.UnKnow.ordinal());
        mStorageDeviceType=StorageDeviceInfo.StorageDeviceType.values()[type];
        System.out.println("====DocumentPermisssionActivity mStorageDeviceType="+mStorageDeviceType);
        if(mStorageDeviceType== StorageDeviceInfo.StorageDeviceType.UnKnow || mStorageDeviceType== StorageDeviceInfo.StorageDeviceType.ExtrageDevice){
            DocumentPermissionActivity.this.finish();
            return;
        }

        adapter=new MyPagerAdapter(getIntroductoryImage(Build.BRAND, mStorageDeviceType));
        MaterialDialog.Builder builder=new MaterialDialog.Builder(this);
        mDialog=builder.title(ResourceUtils.loadStr(R.string.authorization_step, 1, adapter.getCount()))
                .customView(R.layout.dialog_document_permission, false)
                .positiveText(ResourceUtils.loadStr(R.string.start_authorization))
                .negativeText(ResourceUtils.loadStr(R.string.cancle))
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        DocumentPermissionActivity.this.finish();
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialog.dismiss();
                        DocumentPermissionActivity.this.finish();
                    }
                })
                .theme(Theme.LIGHT)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which){
                            case POSITIVE:
                                dialog.dismiss();
                                getDocumentPermission();
                                break;
                            case NEGATIVE:
                                dialog.dismiss();
                                DocumentPermissionActivity.this.finish();
                                break;
                        }
                    }
                }).build();
        ViewPager viewPager= (ViewPager) mDialog.getCustomView().findViewById(R.id.vp);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(mOnPageChangeListener);
        indicator= (CircleIndicator) mDialog.getCustomView().findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
        mDialog.show();
    }

    ViewPager.OnPageChangeListener mOnPageChangeListener=new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mDialog.setTitle(ResourceUtils.loadStr(R.string.authorization_step, position+1, adapter.getCount()));
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };


    /**获取document 访问权限*/
    protected void getDocumentPermission(){
        DocumentTreePermissionUtils.getInstance().getDocumentTreePermission(DocumentPermissionActivity.this, 1900);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==resultCode && requestCode==1900 && data!=null){
            //document permission
            StorageDeviceInfo.StorageDeviceType type=DocumentTreePermissionUtils.getInstance().onPermissionReciver(data, this);
        }
        DocumentPermissionActivity.this.finish();
    }



    private class MyPagerAdapter extends PagerAdapter{

        private List<ImageView> mImageViews;

        private MyPagerAdapter(Integer[] images){
            mImageViews=new ArrayList<>();
            for(Integer image : images){
                ImageView imageView=new ImageView(DocumentPermissionActivity.this);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(DocumentPermissionActivity.this).load(image).into(imageView);
                mImageViews.add(imageView);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mImageViews.get(position));
            return mImageViews.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mImageViews.get(position));
        }

        @Override
        public int getCount() {
            return mImageViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }
    }

}
