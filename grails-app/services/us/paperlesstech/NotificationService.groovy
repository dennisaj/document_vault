package us.paperlesstech

import org.springframework.context.i18n.LocaleContextHolder

class NotificationService {

    static transactional = true
	def messageSource

	private Map notification(String title, String message, NotificationStatus status) {
		message = messageSource.getMessage(message, [].toArray(), message, LocaleContextHolder.getLocale())
		title = messageSource.getMessage(title, [].toArray(), title, LocaleContextHolder.getLocale())
		[title:title, message:message, status:status.name().toLowerCase()]
	}

	Map error(String title, String message) {
		notification(title, message, NotificationStatus.Error)
	}

	Map error(String message) {
		notification('document-vault.notification.error', message, NotificationStatus.Error)
	}

	Map info(String title, String message) {
		notification(title, message, NotificationStatus.Info)
	}

	Map info(String message) {
		notification('document-vault.notification.info', message, NotificationStatus.Info)
	}

	Map success(String title, String message) {
		notification(title, message, NotificationStatus.Success)
	}

	Map success(String message) {
		notification('document-vault.notification.success', message, NotificationStatus.Success)
	}

	Map warning(String title, String message) {
		notification(title, message, NotificationStatus.Warning)
	}

	Map warning(String message) {
		notification('document-vault.notification.warning', message, NotificationStatus.Warning)
	}
}

enum NotificationStatus {
	Error,
	Info,
	Success,
	Warning
}

