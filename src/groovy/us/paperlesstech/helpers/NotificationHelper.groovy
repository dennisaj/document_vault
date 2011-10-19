package us.paperlesstech.helpers

class NotificationHelper {
	static Map notification(String title, String message, NotificationStatus status) {
		[title:title, message:message, status:status.name().toLowerCase()]
	}

	static Map error(String title, String message) {
		notification(title, message, NotificationStatus.Error)
	}

	static Map info(String title, String message) {
		notification(title, message, NotificationStatus.Info)
	}

	static Map success(String title, String message) {
		notification(title, message, NotificationStatus.Success)
	}

	static Map warning(String title, String message) {
		notification(title, message, NotificationStatus.Warning)
	}
}

enum NotificationStatus {
	Error,
	Info,
	Success,
	Warning
}
