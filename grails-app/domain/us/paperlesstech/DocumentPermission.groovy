package us.paperlesstech

public enum DocumentPermission {
	Delete,
	GetSigned,
	Notes,
	Print,
	Sign,
	Tag,
	Upload,
	View

	String getKey() {
		name()
	}

	@Override
	String toString() {
		name()
	}
}
