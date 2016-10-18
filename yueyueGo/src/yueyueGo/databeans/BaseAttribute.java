package yueyueGo.databeans;

public interface BaseAttribute {

	public abstract BaseAttribute copy();

	public abstract String equalsMsg(Object arg0);

	public abstract int index();

	public abstract int indexOfValue(String arg0);

	public abstract boolean isNominal();

	public abstract boolean isNumeric();

	public abstract boolean isString();

	public abstract String name();

	public abstract String toString();

}