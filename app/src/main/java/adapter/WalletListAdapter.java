package adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mvendpay.mvendpay.R;

import java.util.List;

import model.Wallet;

public class WalletListAdapter extends BaseAdapter {
	private Activity activity;
	private LayoutInflater inflater;
	private List<Wallet> myWallets;

	public WalletListAdapter(Activity activity, List<Wallet> myWallets) {
		this.activity = activity;
		this.myWallets = myWallets;
	}

	@Override
	public int getCount() {
		return myWallets.size();
	}

	@Override
	public Object getItem(int location) {
		return myWallets.get(location);
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
			convertView = inflater.inflate(R.layout.wallet_list_row, null);

		TextView walletPhoneNumber = (TextView) convertView.findViewById(R.id.walletPhoneNumber);
		TextView walletNetwork = (TextView) convertView.findViewById(R.id.walletNetwork);
		TextView walletOptionStatus = (TextView) convertView.findViewById(R.id.walletOptionStatus);
		TextView walletItemID = (TextView) convertView.findViewById(R.id.walletItemID);

		// getting wallet data for the row
		Wallet m = myWallets.get(position);


		// wallet phone Number
		walletPhoneNumber.setText(m.getPhoneNumber());
		
		// Wallet network name
		walletNetwork.setText(m.getNetworkName());
		
		// Wallet Status
		if (m.getStatus().equalsIgnoreCase("null")){
			walletOptionStatus.setText("----");
		}else {
			walletOptionStatus.setText(m.getStatus());
		}
		// Item ID
		walletItemID.setText(m.getWalletItemID());

		return convertView;
	}

}