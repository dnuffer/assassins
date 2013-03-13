package com.nbs.client.assassins;

public class GalleryItem {

	private final String mainText;
	private final String detail;
	private final String imgId;
	
	public GalleryItem(String mainText, String detail, String imgId) {
		this.mainText = mainText;
		this.detail = detail;
		this.imgId = imgId;
	}
	public String getMainText() {
		return mainText;
	}
	public String getDetail() {
		return detail;
	}
	public String getImgId() {
		return imgId;
	}
}
