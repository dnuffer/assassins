package com.nbs.client.assassins.navigation;

import android.view.View;

public interface MenuListItem {
    public View getView(View convertView);
    public int getViewType();
}
