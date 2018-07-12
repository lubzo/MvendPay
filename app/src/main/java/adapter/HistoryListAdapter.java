package adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mvendpay.mvendpay.R;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import model.History;
import model.Wallet;

import static android.content.Context.MODE_PRIVATE;

public class HistoryListAdapter extends BaseAdapter {
	private Activity activity;
	private LayoutInflater inflater;
	private List<History> myHistory;
	private String merchant_id;

	public HistoryListAdapter(Activity activity, List<History> myHistory,String merchant_id) {
		this.activity = activity;
		this.myHistory = myHistory;
		this.merchant_id = merchant_id;
	}

	@Override
	public int getCount() {
		return myHistory.size();
	}

	@Override
	public Object getItem(int location) {
		return myHistory.get(location);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (inflater == null)
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			convertView = inflater.inflate(R.layout.transaction_list_item_row, null);

		TextView history_date = (TextView) convertView.findViewById(R.id.history_date);
		TextView tvHistoryAmount = (TextView) convertView.findViewById(R.id.tvHistoryAmount);

		TextView tvHistoryID = (TextView) convertView.findViewById(R.id.tvHistory_id);

		// getting history data for the row
		History h = myHistory.get(position);

		// Date

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date convertedDate = new Date();
		try {
			convertedDate = dateFormat.parse(h.getDate());
		} catch (ParseException e) {
			e.printStackTrace();
		}

        //then format that date object the way you want
		SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
		String formattedDate = df.format(convertedDate);
		history_date.setText(formattedDate);

		double decimalAmount = Double.parseDouble(h.getAmount());
		//DecimalFormat decimalf = new DecimalFormat("#.###");
		String textAmount = "0";

		DecimalFormat formatter = new DecimalFormat("#,###,###");
		String formattedTotal = formatter.format(decimalAmount);
		textAmount = formattedTotal;
		/*if(decimalAmount > 100000) {
			decimalAmount = decimalAmount / 1000000;
			textAmount = decimalf.format(decimalAmount)+ "M";
		}else{
			textAmount = String.valueOf(decimalAmount);
		}*/
		//transaction ID
		tvHistoryID.setText(h.getRequest_ref());

		//  transaction type
		if(h.getType().equals("payment")) {
			//Amount
			tvHistoryAmount.setText(textAmount);
			//change text color
			tvHistoryAmount.setTextColor(Color.parseColor("#379EFB"));
			//ivHistoryIcon.setImageResource(R.drawable.ic_payment_arrow);
		}else{
			//Amount
			tvHistoryAmount.setText(textAmount);
		//	ivHistoryIcon.setImageResource(R.drawable.ic_withdrawal_arrow);
			tvHistoryAmount.setTextColor(Color.parseColor("#FD5C46"));
		}

		if(h.getPayer_id().equalsIgnoreCase(merchant_id)){
			//Amount
			tvHistoryAmount.setText(textAmount);
			tvHistoryAmount.setTextColor(Color.parseColor("#FD5C46"));

		//	ivHistoryIcon.setImageResource(R.drawable.ic_withdrawal_arrow);
		}

		return convertView;
	}

}