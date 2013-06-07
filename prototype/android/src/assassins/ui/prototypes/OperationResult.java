package assassins.ui.prototypes;

public class OperationResult<T> {
	
	private T result;
	private Exception e;
	
	public OperationResult() {
	}
	
	public void setResult(T result) {
		this.result = result;
	}

	public T getResult() {
		return result;
	}

	public Exception getException() {
		return e;
	}

	public void setException(Exception e) {
		this.e = e;
	}

};