package adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mvendpay.mvendpay.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import model.History;
import model.MainMenuItem;

/**
 * Created by tlubega on 11/29/2017.
 */

public class MainMenuAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<MainMenuItem> mainMenuList;


    public MainMenuAdapter(Activity activity, List<MainMenuItem> mainMenu) {
        this.activity = activity;
        this.mainMenuList = mainMenu;
    }

    @Override
    public int getCount() {
        return mainMenuList.size();
    }

    @Override
    public Object getItem(int location) {
        return mainMenuList.get(location);
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
            convertView = inflater.inflate(R.layout.main_menu_list_row, null);

        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        TextView tvTitle = (TextView) convertView.findViewById(R.id.title);
        TextView tvSubtitle = (TextView) convertView.findViewById(R.id.subtitle);

        // getting main menu Items data for the row
        MainMenuItem h = mainMenuList.get(position);

        //Title
        tvTitle.setText(h.getTitle());

        //Subtitle
        tvSubtitle.setText(h.getSubTitle());

        //icon
        icon.setImageResource(h.getDrawable());


        return convertView;
    }

}