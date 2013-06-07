package assassins.ui.prototypes;

public class MatchSettingsItem {
	private String name;
	private String detail;
	
	public MatchSettingsItem(String newName, String newDetail)
	{
		name = newName;
		detail = newDetail;
	}
	
	public String getName() {return name;}
	public void setName(String newName) {name=newName;}
	public String getDetail() {return detail;}
	public void setDetail(String newDetail) {detail=newDetail;}
}
