package us.paperlesstech

public enum MimeType {
	PCL("application/pcl"),
	PNG("image/png"),
	PDF("application/pdf")

	private final String id

	MimeType(String id) {
		this.id = id
	}
}