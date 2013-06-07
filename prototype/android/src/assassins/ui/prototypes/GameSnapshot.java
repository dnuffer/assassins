package assassins.ui.prototypes;

public class GameSnapshot {

	private PlayerState myState;
	private PlayerState targetState;
	private float bearingToTarget;

	public GameSnapshot(PlayerState myState, PlayerState targetState,
			float directionToTarget) {
		this.setMyState(myState);
		this.setTargetState(targetState);
		this.setBearingToTarget(directionToTarget);
	}

	public GameSnapshot() {
		// TODO Auto-generated constructor stub
	}

	public PlayerState getMyState() {
		return myState;
	}

	public void setMyState(PlayerState myState) {
		this.myState = myState;
	}

	public PlayerState getTargetState() {
		return targetState;
	}

	public void setTargetState(PlayerState targetState) {
		this.targetState = targetState;
	}

	public float getBearingToTarget() {
		return bearingToTarget;
	}

	public void setBearingToTarget(float bearingToTarget) {
		this.bearingToTarget = bearingToTarget;
	}
	
	

}
