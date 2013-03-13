package com.nbs.client.assassins;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuGalleryItem implements MenuListItem {
	private final LayoutInflater inflater;
	private final MenuGalleryData row;
	
	public MenuGalleryItem(LayoutInflater inflater, MenuGalleryData item) {
		this.inflater = inflater;
		this.row = item;
	}

	
	@Override
	public View getView(View convertView) {

        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.menu_gallery_item, null);
        
        //TODO add list of items into horizontal pager
        
        View view = viewGroup;

        return view;
	}

	@Override
	public int getViewType() {
		return MenuItemType.MENU_GALLERY.ordinal();
	}

}
