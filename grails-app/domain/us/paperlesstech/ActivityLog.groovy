package us.paperlesstech

class ActivityLog {

	enum ActivityType {
		DELETE,
		EMAIL,
		PRINT,
		SIGN,
		VIEW
	}

	ActivityType activityType
	Date dateCreated
	Document document
	String ip
	String notes
	String pagesAffected
	User user
	String userAgent

	static mapping = {
	}

    static constraints = {
		notes blank:true, nullable:true, size:0..2048
		pagesAffected nullable:true
		user nullable:true
    }
}
