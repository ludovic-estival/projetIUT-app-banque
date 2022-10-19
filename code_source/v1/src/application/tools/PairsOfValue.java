package application.tools;

public class PairsOfValue <T, U> {
	private T left;
	private U right;

	public PairsOfValue (T _l, U _r) {
		this.left=_l;
		this.right = _r;
	}
	public T getLeft() {
		return this.left;
	}
	public U getRight() {
		return this.right;
	}
	public void setLeft (T _l) {
		this.left = _l;
	}
	public void setRight (U _r) {
		this.right = _r;
	}
}
