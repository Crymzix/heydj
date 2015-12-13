package ca.ubc.heydj.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ca.ubc.heydj.R;

/**
 * Created by Chris Li on 12/12/2015.
 */
public class MenuItemsAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<MenuItem> mMenuItems;

    public MenuItemsAdapter(Context context, List<MenuItem> menuItems) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mMenuItems = menuItems;
    }

    @Override
    public int getCount() {
        return mMenuItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mMenuItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MenuItem menuItem = mMenuItems.get(position);
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.menu_list_item, parent, false);
        }

        ImageView menuIcon = (ImageView) convertView.findViewById(R.id.menu_icon);
        TextView menuTitle = (TextView) convertView.findViewById(R.id.menu_text);
        menuIcon.setImageResource(menuItem.getResource());
        menuTitle.setText(menuItem.getTitle());

        return convertView;
    }

    public static class MenuItem {

        private int resource;
        private String title;

        public MenuItem(int resource, String title) {
            this.resource = resource;
            this.title = title;
        }

        public int getResource() {
            return resource;
        }

        public void setResource(int resource) {
            this.resource = resource;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
