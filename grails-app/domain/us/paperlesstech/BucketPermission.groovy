package us.paperlesstech

public enum BucketPermission {
	Create,
	Delete,
	MoveInTo,
	MoveOutOf,
	View

	String getKey() {
		name()
	}

	@Override
	String toString() {
		name()
	}
}
