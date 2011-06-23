var Show = {
	//global objects
	$canvas: null,
	currentPage: null,
	urls: {},

	realSetupCanvas: function(canvas, page) {
		this.currentPage = page;

		$('#right-arrow a').attr('href', '#' + Math.min(Document.pageCount, page.pageNumber + 1));
		$('#left-arrow a').attr('href', '#' + Math.max(Document.FIRST_PAGE, page.pageNumber - 1));

		$('.arrow a').removeClass('disabled ui-state-disabled');

		if (page.pageNumber == 1) {
			$('#left-arrow a').addClass('disabled ui-state-disabled');
		}

		if (page.pageNumber == Document.pageCount) {
			$('#right-arrow a').addClass('disabled ui-state-disabled');
		}

		$('#page-number').text(this.currentPage.pageNumber + '/' + Document.pageCount);

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

	init: function(urls) {
		var self = this;
		this.urls = urls;

		this.$canvas = $('#canvas')
		this.$canvas.css('overflow', 'hidden');

		$('#sign').button({
			icons: { primary: 'ui-icon-pencil' }
		});

		$('#close').button({
			icons: { primary: 'ui-icon-circle-close' }
		}).click(function() {
			window.location.href = self.urls.close;
		});

		$('#left-arrow a').button({
			icons: { primary: 'ui-icon-circle-arrow-w' },
			text: false
		});

		$('#right-arrow a').button({
			icons: { primary: 'ui-icon-circle-arrow-e' },
			text: false
		});

		$('.arrow a').click(function() {
			if ($(this).is('.disabled')) {
				return false;
			}
		});

		$(window).hashchange(function() {
			var newPage = parseInt(location.hash.substring(1)) || Document.FIRST_PAGE;

			if (!self.currentPage || newPage != self.currentPage.pageNumber) {
				Document.getPage(newPage, function(page) {
					self.setupCanvas(self.$canvas, page);
				});
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
		Document.init($.extend({}, this.urls));
		$('#print').button({
			icons: { primary: 'ui-icon-print' }
		}).click(function() {
			Document.print();
		});

		// Load the page indicated by the location hash
		$(window).hashchange();
	}
};
