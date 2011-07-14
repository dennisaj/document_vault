package us.paperlesstech

class HomeController {
    def index = {
		// Using the full URL because the load balancer is messing up redirects in production
		def url = g.createLink(absolute:true, controller:'document',action:'index')

		redirect(url: url)
	}
}
