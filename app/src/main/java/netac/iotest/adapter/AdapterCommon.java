package netac.iotest.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

@SuppressWarnings("rawtypes")
/**
 * Created by siwei.zhao on 2016/6/7.
 * Adapter是参考的RecyclerView.Adapter的思想写出来的。
 * 1.让Adapter的代码更加清晰明了；
 * 2.在Adapter增加了对ListView局部数据刷新的方式；
 */
public abstract class AdapterCommon<VH extends AdapterCommon.ViewHolder> extends BaseAdapter {

	private final int TAG_VIEW_HOLDER_KEY=Integer.MIN_VALUE;

	/**创建ViewHolder
	 * @param parent
	 * @param position
	 * */
	public abstract VH onCreateViewHolder(ViewGroup parent, int position);

	/**把数据绑定到ViewHolder
	 * @param holder
	 * @param position
	 * */
	public abstract void onBindViewHolder(VH holder, int position);


	public abstract class ViewHolder{

		public View mBaseView;

		public ViewHolder(View convertView) {
			mBaseView=convertView;
		}

	}


	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("unchecked")
	@SuppressLint("ViewTag")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		VH holder;
		if(convertView==null){
			holder=onCreateViewHolder(parent, position);
			convertView=holder.mBaseView;
			convertView.setTag(TAG_VIEW_HOLDER_KEY, holder);
		}
		holder=(VH) convertView.getTag(TAG_VIEW_HOLDER_KEY);
		onBindViewHolder(holder, position);
		return convertView;
	}

	/**刷新指定position的数据,如果指定position的集合的数据发生改变(局部刷新)
	 * @param position 刷新的item position
	 * @param bindListView 绑定该Adapter的ListView
	 * */
	public void notifyItem(int position, ListView bindListView){
		if(bindListView==null || position>=getCount() || position<0)return;
		int mFirst=bindListView.getFirstVisiblePosition();
		int mLast=bindListView.getLastVisiblePosition();
		if(position>=mFirst && position<=mLast){
			View baseView = bindListView.getChildAt(position);
			@SuppressWarnings("unchecked")
			VH holder=(VH) baseView.getTag();
			onBindViewHolder(holder, position);
		}
	}

}
