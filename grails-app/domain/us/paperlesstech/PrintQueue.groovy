package us.paperlesstech

import org.joda.time.LocalDateTime
import org.joda.time.contrib.hibernate.PersistentLocalDateTime

class PrintQueue {

	LocalDateTime dateCreated
	Document document
	Printer printer
	User user
	
    static constraints = {
    }
	
	static mapping = {
		dateCreated type:PersistentLocalDateTime
	}
}
