package assassins.ui.prototypes;

public class Bearing {
	
	private float degrees;
	
	public Bearing()
	{
		degrees = -1;
	}
	
	public Bearing(float degreesFromNorth)
	{
		degrees = degreesFromNorth;
	}
	
	public void setDegrees(float degreesFromNorth)
	{
		degrees = degreesFromNorth;
	}
	
	
	public float getDegrees()
	{
		return degrees;
	}
	
	
}
