package kr.ac.ajou.esd.calculator;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * History ListView 에 뿌려주는 Data를 핸들링하는 Adapter 
 *
 */
public class HistoryAdapter extends ArrayAdapter<HistoryVO> {
	private List<HistoryVO> items;
	private int layoutId;
	private DBHelper db;
	private CopyClickListener copyListener;

	public HistoryAdapter(Context context, int layoutId, List<HistoryVO> items, DBHelper db, CopyClickListener copyListener) {
		super(context, layoutId, items);
		this.items = items;
		this.layoutId = layoutId;
		this.db = db;
		this.copyListener = copyListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//ViewHodler Pattern 으로 구현하여 성능 이슈 제거
		View v = convertView;
		ViewHolder holder;
		
		//View Initialize
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(layoutId, null);
			holder = new ViewHolder();
			holder.tvExpression = (TextView) v.findViewById(R.id.tv_expression);
			holder.tvResult = (TextView) v.findViewById(R.id.tv_result);
			holder.btnCopy = (ImageView) v.findViewById(R.id.btn_copy);
			holder.btnRemove = (ImageView) v.findViewById(R.id.btn_remove);
			
			v.setTag(holder);
		}else{
			holder = (ViewHolder)v.getTag();
		}
		
		//set data
		final HistoryVO vo = items.get(position);
		if (vo != null) {
			holder.tvExpression.setText(vo.getExpression());
			holder.tvResult.setText(vo.getResult());
			holder.btnCopy.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					copyListener.onCopy(vo); //copy 버튼 클릭시 activity 로 VO 를 넘겨준다.
				}
			});
			holder.btnRemove.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					db.delete(vo);
					remove(vo);
					notifyDataSetChanged();
				}
			});
		}
		return v;
	}
	
	class ViewHolder {
		TextView tvExpression;
		TextView tvResult;
		ImageView btnCopy;
		ImageView btnRemove;
	}
	
	interface CopyClickListener{
		void onCopy(HistoryVO vo);
	}
}
