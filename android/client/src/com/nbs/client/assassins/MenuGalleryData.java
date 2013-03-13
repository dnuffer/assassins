package com.nbs.client.assassins;

import java.util.List;

public class MenuGalleryData extends MenuRowData {

	private final List<GalleryItem> items;
	
	public MenuGalleryData(List<GalleryItem> items) {
		super();
		this.items = items;
	}

	public List<GalleryItem> getItems() {
		return items;
	}

}
