package us.paperlesstech

import org.springframework.context.i18n.LocaleContextHolder

class NotificationService {

    static transactional = true
	def messageSource

	private Map notification(String title, String message, NotificationStatus status, List args=[]) {
		message = messageSource.getMessage(message, args.toArray(), message, LocaleContextHolder.getLocale())
		title = messageSource.getMessage(title, args.toArray(), title, LocaleContextHolder.getLocale())
		[title:title, message:message, status:status.name().toLowerCase()]
	}

	Map error(String title, String message, List args=[]) {
		notification(title, message, NotificationStatus.Error, args)
	}

	Map error(String message, List args=[]) {
		notification('document-vault.api.notification.error', message, NotificationStatus.Error, args)
	}

	Map info(String title, String message, List args=[]) {
		notification(title, message, NotificationStatus.Info, args)
	}

	Map info(String message, List args=[]) {
		notification('document-vault.api.notification.info', message, NotificationStatus.Info, args)
	}

	Map success(String title, String message, List args=[]) {
		notification(title, message, NotificationStatus.Success, args)
	}

	Map success(String message, List args=[]) {
		notification('document-vault.api.notification.success', message, NotificationStatus.Success, args)
	}

	Map warning(String title, String message, List args) {
		notification(title, message, NotificationStatus.Warning, args)
	}

	Map warning(String message, List args=[]) {
		notification('document-vault.api.notification.warning', message, NotificationStatus.Warning, args)
	}
}

enum NotificationStatus {
	Error,
	Info,
	Success,
	Warning
}

