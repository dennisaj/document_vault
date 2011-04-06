package us.paperlesstech

class Printer {

	String host
	String deviceType
	String name
	int port
	
    static constraints = {
		name blank:false, unique:true
		port range:0..65535
    }
	
	String toString() {
		"Printer (${id}) - ${name} - ${host}:${port}"
	}
}
