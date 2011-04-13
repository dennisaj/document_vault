package us.paperlesstech

/**
 * Stores the PCL that was originally printed.
 */
class Pcl {
	static belongsTo = [document: Document]
	byte[] data

    static constraints = {
		data(maxSize:20*1024*1024)
    }
}