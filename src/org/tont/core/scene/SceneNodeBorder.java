package org.tont.core.scene;

public class SceneNodeBorder {
	
	public int leftBorder;
	public int rightBorder;
	public int topBorder;
	public int bottomBorder;
	
	public SceneNodeBorder(int left, int right, int top, int bottom) {
		this.leftBorder = left;
		this.rightBorder = right;
		this.topBorder = top;
		this.bottomBorder = bottom;
	}
	
	public Position getCentralPosition() {
		return new Position( (leftBorder + rightBorder) / 2 , (topBorder + bottomBorder) / 2);
	}
}
