package com.nbs.client.assassins;

public class MenuRowData {
	private final String mainText;
	private final String subText;
	private final int imageId;
	
	public MenuRowData(String mainText, String subText, int imageId) {
		this.mainText = mainText;
		this.subText = subText;
		this.imageId = imageId;
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
}
