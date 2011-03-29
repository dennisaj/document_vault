package us.paperlesstech

import org.joda.time.*

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
	
    static constraints = {
		pagesAffected(nullable: true)
    }
}
