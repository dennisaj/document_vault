package us.paperlesstech.helpers

class NotificationHelper {
	static Map notification(String message, String title, NotificationStatus status) {
		[message:message, title:title, status:status.name()]
	}
}

enum NotificationStatus {
	Error,
	Info,
	Success
}
