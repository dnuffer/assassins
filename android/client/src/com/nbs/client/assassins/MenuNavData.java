package com.nbs.client.assassins;

public class MenuNavData extends MenuRowData {

	private final int imageId;
	private final String mainText;
	
	public MenuNavData(String mainText, int imageId) {
		super();
		this.imageId = imageId;
		this.mainText = mainText;
	}

	public String getMainText() {
		return mainText;
	}

	public int getImageId() {
		return imageId;
	}

}
