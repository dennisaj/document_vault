package us.paperlesstech

public enum DocumentPermission {
	Delete,
	FolderCreate,
	FolderDelete,
	FolderMoveInTo,
	FolderMoveOutOf,
	GetSigned,
	Notes,
	Print,
	Sign,
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
