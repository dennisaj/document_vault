package us.paperlesstech

class HomeController {
    def index = {
		redirect(controller: "document", action: "index")
	}
}