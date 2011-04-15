package us.paperlesstech

import org.joda.time.LocalDateTime
import org.joda.time.contrib.hibernate.PersistentLocalDateTime

class ActivityLog {

	enum ActivityType {
		DELETE,
		EMAIL,
		PRINT,
		SIGN,
		VIEW
	}
	
	ActivityType activityType
	LocalDateTime dateCreated
	Document document
	String ip
	String notes
	String pagesAffected
	User user
	String userAgent

	static mapping = {
		dateCreated type:PersistentLocalDateTime
	}
	
    static constraints = {
		notes blank:true, nullable:true, size:0..2048
		pagesAffected nullable:true
		user nullable:true
    }
}
