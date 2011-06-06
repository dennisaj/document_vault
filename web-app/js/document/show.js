var Show = {
	//global objects
	$canvas: null,
	currentPage: null,
	FIRST_PAGE: 1,
	pageCount: 0,
	pages: null,
	urls: {},

	realSetupCanvas: function(canvas, page) {
		this.currentPage = page;

		$('#right-arrow a').attr('href', '#' + Math.min(this.pageCount, page.pageNumber + 1));
		$('#left-arrow a').attr('href', '#' + Math.max(1, page.pageNumber - 1));

		$('.arrow a').removeClass('disabled');
		$('.arrow a button').removeClass('ui-state-disabled');

		if (page.pageNumber == 1) {
			$('#left-arrow a').addClass('disabled');
			$('#left-arrow a button').addClass('ui-state-disabled');
		}

		if (page.pageNumber == this.pageCount) {
			$('#right-arrow a').addClass('disabled');
			$('#right-arrow a button').addClass('ui-state-disabled');
		}

		canvas.html($(page.background).width(Math.min(canvas.width(), page.background.width)));
	},

	setupCanvas: function(canvas, page) {
		var self = this;
		if (page.background.complete) {
			this.realSetupCanvas(canvas, page);
		} else {
			page.background.onload = function() {
				self.realSetupCanvas(canvas, page);
			};
		}
	},

	onAjaxError: function(jqXHR, textStatus, errorThrown) {
		$('#dialog-message').dialog('close');

		HtmlAlert._alert('An error has occurred', '<p><span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 50px 0;"></span>There was an error communicating with the server. Please try again.</p>');
	},

	// Document functions
	getPage: function(canvas, documentId, pageNumber) {
		var self = this;
		if (pageNumber > this.pageCount) {
			pageNumber = this.pageCount;
		} else if (pageNumber < this.FIRST_PAGE) {
			pageNumber = this.FIRST_PAGE;
		}

		if (this.pages[pageNumber]) {
			this.setupCanvas(canvas, this.pages[pageNumber]);
		} else {
			Document.getPage(documentId, pageNumber, function(page) {
				self.pages[page.pageNumber] = page;
				self.setupCanvas(self.$canvas, self.pages[page.pageNumber]);
			});
		}
	},
	// !Document functions

	init: function(urls) {
		var self = this;
		this.urls = urls;

		this.$canvas = $('#canvas')
		this.pageCount = parseInt($('#pageCount').val() || this.FIRST_PAGE);
		this.pages = new Array(this.pageCount + this.FIRST_PAGE);

		$('#close').click(function() {
			window.location.href = self.urls.close;
		});

		$('.arrow a').click(function() {
			if ($(this).is('.disabled')) {
				return false;
			}
		});

		$(window).hashchange(function() {
			var newPage = parseInt(location.hash.substring(1)) || self.FIRST_PAGE;

			if (!self.currentPage || newPage != self.currentPage.pageNumber) {
				self.getPage(self.$canvas, $('#documentId').val(), newPage);
			}
		});

		$('#dialog-message').dialog({
			autoOpen: false,
			buttons: {},
			closeOnEscape: false,
			draggable: false,
			modal: true,
			open: function(event, ui) {
				$(".ui-dialog-titlebar-close", $(this).parent()).hide();
				$(this).dialog('option', 'buttons', {});
			},
			resizable: false
		});

		// Setup document
		Document.init($.extend({}, this.urls), this.onAjaxError);
		$('#print').click(function() {
			Document.print();
		});

		$('#email').click(function() {
			Document.email();
		});

		// Load the page indicated by the location hash
		$(window).hashchange();
	}
};
