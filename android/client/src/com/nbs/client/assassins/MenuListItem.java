package com.nbs.client.assassins;

import android.view.View;

public interface MenuListItem {
    public View getView(View convertView);
    public int getViewType();
    public int getId();
}
