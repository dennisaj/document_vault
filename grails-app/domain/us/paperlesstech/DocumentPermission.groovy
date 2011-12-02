package us.paperlesstech

public enum DocumentPermission {
	Delete,
	GetSigned,
	ManageFolders,
	Notes,
	Print,
	Sign,
	Upload,
	View,
	Flag

	String getKey() {
		name()
	}

	@Override
	String toString() {
		name()
	}
}
