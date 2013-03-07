package assassins.ui.prototypes;

public class AssassinsHttpException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AssassinsHttpException(String s, Throwable t)
	{	
		super(s,t);	
	}
	public AssassinsHttpException()
	{
		super();	
	}
	public AssassinsHttpException(String s)
	{
		super(s);	
	}
	public AssassinsHttpException(Throwable t)
	{
		super(t);	
	}
}
