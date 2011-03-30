package us.paperlesstech

import org.joda.time.*
import org.joda.time.contrib.hibernate.PersistentLocalDateTime

class ActivityLog {

	enum ActivityType {
		DELETE,
		SIGN,
		VIEW
	}
	
	ActivityType activityType
	String ip
	String userAgent
	LocalDateTime dateCreated
	User user
	Document document
	String pagesAffected

	static mapping = {
		dateCreated(type:PersistentLocalDateTime)
	}
	
    static constraints = {
		pagesAffected(nullable: true)
    }
}
