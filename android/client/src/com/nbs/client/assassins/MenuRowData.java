package com.nbs.client.assassins;

public class MenuRowData {
	private final MenuItemType itemType;
	private final String mainText;
	private final String subText;
	private final int imageId;
	private final int itemId;
	
	public MenuRowData(MenuItemType itemType, String mainText, String subText,
			int imageId, int itemId) {
		this.itemType = itemType;
		this.mainText = mainText;
		this.subText = subText;
		this.imageId = imageId;
		this.itemId = itemId;
	}
	
	public MenuRowData(MenuItemType itemType, String mainText,
			int imageId, int itemId) {
		this.itemType = itemType;
		this.mainText = mainText;
		this.imageId = imageId;
		this.itemId = itemId;
		this.subText = null;
	}
	
	public MenuRowData(MenuItemType itemType, String mainText, int itemId) {
		this.itemType = itemType;
		this.mainText = mainText;
		this.itemId = itemId;
		this.subText = null;
		this.imageId = -1;
	}
	
	public int getImageId() {
		return imageId;
	}
	public String getSubText() {
		return subText;
	}
	public String getMainText() {
		return mainText;
	}
	public int getId() {
		return itemId;
	}

	public MenuItemType getItemType() {
		return itemType;
	}
}
