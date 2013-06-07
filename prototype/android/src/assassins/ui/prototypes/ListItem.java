package assassins.ui.prototypes;

public class ListItem {
	
	private String title;
	private String subTitle;
	private int imgResource;
	private int layout;
	
	
	public ListItem(String bigText, String smallText, int imgResource, int layout)
	{
		this.setTitle(bigText);
		this.setSubTitle(smallText);
		this.setImgResource(imgResource);
		this.setLayout(layout);
	}


	public int getImgResource() {
		return imgResource;
	}


	public void setImgResource(int imgResource) {
		this.imgResource = imgResource;
	}


	public String getSubTitle() {
		return subTitle;
	}


	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public int getLayout() {
		return layout;
	}


	public void setLayout(int layout) {
		this.layout = layout;
	}
	
	public String toString()
	{
		return this.title;
	}
	
	
}
