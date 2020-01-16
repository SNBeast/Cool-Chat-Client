import java.io.Serializable;
public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final byte message = 0;
	public static final byte name = 1;
	private byte type;
	private Object contents;
	public Message (byte type, Object contents) {
		this.type = type;
		this.contents = contents;
	}
	public byte type () {
		return type;
	}
	public Object contents () {
		return contents;
	}
}