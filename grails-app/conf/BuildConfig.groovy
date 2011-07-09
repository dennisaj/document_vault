grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
	inherits("global") {
		// uncomment to disable ehcache
		// excludes 'ehcache'
	}
	log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	repositories {
		grailsPlugins()
		grailsHome()
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/libs-release-local"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/plugins-release-local"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/remote-repos"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/libs-release"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/libs-snapshot"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/plugins-release"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/plugins-snapshot"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/itextpdf/"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/thebuzzmedia/"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/jboss/"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/mygrid/"
		grailsRepo "http://artifactory.ptdev.lan.vbn/artifactory/plugins.grails.org"
		grailsCentral()

		// uncomment the below to enable remote dependency resolution
		// from public Maven repositories
		//mavenLocal()
		//mavenCentral()
		//mavenRepo "http://snapshots.repository.codehaus.org"
	}
	dependencies {
		// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

		compile group:'com.itextpdf', name:'itextpdf', version:'5.0.6'
		runtime group:'mysql', name:'mysql-connector-java', version:'5.1.15'
		compile group:'com.sun.media', name:'jai-codec', version:'1.1.3'
		compile group:'net.java.dev.jai-imageio', name:'jai-imageio-core-standalone', version:'1.2-pre-dr-b04-2010-04-30'
		compile group:'com.amazonaws', name:'aws-java-sdk', version:'1.2.2'
		compile group:'c3p0', name:'c3p0', version:'0.9.1.2'
	}
}

coverage {
	exclusions = ["changelog*", "201*_*", "*Config*"]
}
