var Tagging = {
	urls: {},
	useDocumentSearch: false,

	addTag: function(documentId, tag, callback) {
		var self = this;
		callback = callback || function() {};
		$.ajax({
			data: {documentId:documentId, tag:tag},
			global: false,
			success: callback,
			type: 'POST',
			url: self.urls.addTag
		});
	},

	create: function(name, callback) {
		var self = this;
		$.ajax({
			data: {documentId:holder.attr('data-documentid'), tag:value},
			global: false,
			success: callback,
			type: 'POST',
			url: self.urls.createTag.format(name)
		});
	},

	showAllTagged: function(name, displayId) {
		var	self = this;

		if (self.useDocumentSearch) {
			$("#q").val("tagged " + name);
			$("#searchForm").submit();
			return;
		}

		var $displayId = $(displayId);
		var spinnerTimeout = null;

		$.ajax({
			beforeSend: function() {
				// If the target element is empty, add the spinner now
				if (!$.trim($displayId.html())) {
					$displayId.html($('#spinner').html());
				} else {
					// If the target is not empty, wait a second then add the spinner after the last draggable element
					spinnerTimeout = setTimeout(function() {
						$displayId.fadeIn('fast');
						$displayId.find('.draggable:last').after($('#spinner').html());
					}, 500);
				}
			},
			data: {
				name: name
			},
			error: function(jqXHR, textStatus, errorThrown) { $displayId.html(errorThrown); },
			success: function(data) {
				clearTimeout(spinnerTimeout);
				$displayId.fadeOut('fast', function() {
					$displayId.html(data);
					self.initDragAndDrop();
					$displayId.fadeIn(100, function() { $("li[data-tag='" + name + "']").addClass("on").siblings().removeClass("on"); });
				});
			},
			type: 'POST',
			url: self.urls.allTagged
		});
	},

	showTagbox: function(boxId, button) {
		var self = this;
		var $boxId = $(boxId);
		// Delay the initialization of the tagbox until the first time it is shown.
		if (!$boxId.is('.tagit')) {
			$boxId.tagit({
				tagSource: self.urls.list,
				triggerKeys: ['enter', 'comma', 'tab'],
				initialTags: function(holder) {
					return self.urls.documentList.format(holder.attr('data-documentId'));
				},
				onAdd: function(holder, value) {
					self.addTag(holder.attr('data-documentId'), value);
				},
				onRemove: function(holder, value) {
					self.removeTag(holder.attr('data-documentId'), value);
				}
			}).find('input').attr('placeholder', '+');
		}

		$(button).toggleClass('ui-state-highlight');
		$boxId.parent().toggleClass('hidden');
		return false;
	},

	removeTag: function(documentId, tag, callback) {
		var self = this;
		callback = callback || function() {};
		$.ajax({
			data: {documentId:documentId, tag:tag},
			error: function() { },
			global: false,
			success: callback,
			type: 'POST',
			url: self.urls.removeTag
		});
	},

	initDragAndDrop: function() {
		var self = this;

		$('.draggable').draggable('destroy');
		$('.droppable').droppable('destroy');

		$('.draggable').draggable({
			addClasses: false,
			containment: 'document',
			cursor: 'move',
			cursorAt: { left: 10, top: 50 },
			distance: 20,
			helper: 'clone',
			opacity: 0.75,
			revert: 'invalid',
			revertDuration: 0,
			zIndex: 100
		});

		$('.droppable', '#tag-results').droppable({
			accept: '.draggable',
			activeClass: 'drop',
			hoverClass: 'active',
			tolerance: 'pointer',
			drop: function(event, ui) {
				self.addTag(ui.draggable.data('documentid'), $(event.target).data('tag'), function() {
					if (ui.draggable.is('.remove')) {
						ui.draggable.parent(".document").fadeOut('fast', function() { $(this).remove(); });
					}

					self.showAllTagged($(event.target).data('tag'), '#all-tagged');
				});
			}
		});

		$('.droppable', '#all-tagged').droppable({
			accept: '#all-untagged .draggable',
			activeClass: 'drop',
			addClasses: false,
			hoverClass: 'active',
			tolerance: 'pointer',
			drop: function(event, ui) {
				var data = ui.draggable.data();
				self.addTag(data.documentid, $(event.target).data('tag'), function() {
					if (ui.draggable.is('.remove')) {
						ui.draggable.parent(".document").fadeOut('fast', function() { $(this).remove(); });
					}
					
					self.showAllTagged($(event.target).data('tag'), '#all-tagged');
				});
			}
		});
		
		$('.droppable.tag-remove').droppable({
			accept: '#all-tagged .draggable',
			activeClass: 'drop',
			addClasses: false,
			hoverClass: 'active',
			tolerance: 'pointer',
			drop: function(event, ui) {
				var data = ui.draggable.data();
				self.removeTag(data.documentid, data.tag, function() {
					self.showAllTagged(data.tag, '#all-tagged');
					self.showAllTagged('', '#all-untagged');
				});
			}
		});
	},
	
	initTagResults: function() {
		this.initDragAndDrop();
		$('#tag-search-results ul').jcarousel({});
	},

	init: function(urls, useDocumentSearch) {
		this.urls = urls;
		this.useDocumentSearch = useDocumentSearch || false;

		$('#tag-search-submit').button();
	}
};
