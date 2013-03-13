package com.nbs.client.assassins;

public class MenuEventData extends MenuRowData {

	private final String mainText;
	private final String subText;
	private final int imageId;
	
	public MenuEventData(String mainText, String subText, int imageId) {
		super();
		this.mainText = mainText;
		this.subText = subText;
		this.imageId = imageId;
	}

	public String getMainText() {
		return mainText;
	}

	public String getSubText() {
		return subText;
	}

	public int getImageId() {
		return imageId;
	}


}
