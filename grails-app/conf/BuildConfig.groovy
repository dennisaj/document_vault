grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
	inherits("global") {
		// uncomment to disable ehcache
		// excludes 'ehcache'
	}
	log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	repositories {
		// Disable inheriting plugin repositories
		inherit false
		/*mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/libs-release-local"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/plugins-snapshot-local"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/plugins-release-local"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/remote-repos"*/
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/libs-release"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/libs-snapshot"
		/*mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/plugins-release"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/plugins-snapshot"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/itextpdf/"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/thebuzzmedia/"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/jboss/"
		mavenRepo "http://artifactory.ptdev.lan.vbn/artifactory/mygrid/"
		grailsRepo "http://artifactory.ptdev.lan.vbn/artifactory/plugins.grails.org"*/

		grailsPlugins()
		grailsHome()
		grailsCentral()

		// uncomment the below to enable remote dependency resolution
		// from public Maven repositories
		//mavenRepo "http://maven.itextpdf.com/"
		//mavenRepo "http://repository.jboss.org/nexus/content/groups/public-jboss/"
		//mavenRepo "http://www.mygrid.org.uk/maven/repository/"
		//mavenLocal()
		//mavenCentral()
		//mavenRepo "http://snapshots.repository.codehaus.org"
	}

	dependencies {
		// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
		compile group:'us.paperlesstech', name:'flea', version:'0.9.4', {
			// Grails 2 now includes the log4j slf4j binding.
			excludes([group: 'org.slf4j', name: 'slf4j-log4j12'])
		}
		// The most recent version of iText has an incompatible license. 2.1.7 is the latest version that we can use.
		compile group:'com.lowagie', name:'itext', version:'2.1.7'
		runtime group:'mysql', name:'mysql-connector-java', version:'5.1.15'
		compile group:'com.sun.media', name:'jai-codec', version:'1.1.3'
		compile group:'net.java.dev.jai-imageio', name:'jai-imageio-core-standalone', version:'1.2-pre-dr-b04-2011-07-04'
		compile group:'com.amazonaws', name:'aws-java-sdk', version:'1.2.9', {
			excludes([group: "javax.mail", name: "mail"])
		}
		// AWS depends on this but isn't pulling it in
		compile group:'stax', name:'stax', version:'1.2.0'
		compile group:'c3p0', name:'c3p0', version:'0.9.1.2'
		test group:'org.objenesis', name:'objenesis', version:'1.2'

		// Nimble integration dependencies
		compile group:'org.apache.santuario', name:'xmlsec', version:'1.4.5'
	}

	plugins {
		runtime ':cache-headers:1.1.5'
		test ':code-coverage:1.2.5'
		runtime ':database-migration:1.0'
		runtime ":hibernate:$grailsVersion"
		runtime ':jquery:1.7.1'
		runtime ':mail:1.0'
		runtime ':hibernate-hijacker:0.8.1'
		runtime ':hawk-eventing:0.5.1'
		runtime ':multi-tenant-single-db:0.8.1'
		runtime ':quartz:0.4.2'
		runtime ':resources:1.1.6'
		runtime ':taggable:1.0.1'
		runtime ':shiro:1.1.3'
		test ':spock:0.6-SNAPSHOT'

		runtime ':jetty:1.2-SNAPSHOT', {
			excludes([group: 'org.slf4j', name: 'slf4j-simple'])
		}
	}
}

coverage {
	exclusions = ["changelog*", "201*", "*Config*", "**/us/paperlesstech/nimble/**", "**/us/paperlesstech/auth/nimble/**", "**/org/pcl/parser/**", "**/us/paperlesstech/filters/**"]
}
