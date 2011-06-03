var Tagging = {
	urls: {},
	useDocumentSearch: false,
	addTag: function(documentId, tag, callback) {
		var self = this;
		callback = callback || function() {};
		$.ajax({
			data: {id:documentId, tag:tag},
			global: false,
			success: callback,
			type: 'GET',
			url: self.urls.addTag
		});
	},
	create: function(name, callback) {
		var self = this;
		$.ajax({
			data: {id:holder.attr('documentid'), tag:value},
			global: false,
			success: callback,
			type: 'GET',
			url: self.urls.createTag.format(name)
		});
	},
	showAllTagged: function(name, displayId) {
		var self = this;

		if(self.useDocumentSearch) {
			$("#q").val("tagged " + name);
			$("#searchForm").submit();
			return;
		}

		var $displayId = $(displayId);
		var spinnerTimeout = null;

		$.ajax({
			beforeSend: function() {
				// If the target element is empty, add the spinner now
				if (!$displayId.html().trim()) {
					$displayId.html($('#spinner').html());
				} else {
					// If the target is not empty, wait a second then add the spinner after the last draggable element
					spinnerTimeout = setTimeout(function() {
						$displayId.fadeIn('fast');
						$displayId.find('.draggable:last').after($('#spinner').html());
					}, 500);
				}
			},
			error: function(jqXHR, textStatus, errorThrown) { $displayId.html(errorThrown); },
			global: false,
			success: function(data) {
				clearTimeout(spinnerTimeout);
				$displayId.fadeOut('fast', function() {
					$displayId.html(data);
					Tagging.initDragAndDrop();
					$displayId.fadeIn(100);
				});
			},
			type: 'GET',
			url: self.urls.allTagged.format(name)
		});
	},
	showTagbox: function(boxId) {
		var self = this;
		var $boxId = $(boxId);
		// Delay the initialization of the tagbox until the first time it is shown.
		if (!$boxId.is('.tagit')) {
			$boxId.tagit({
				tagSource: self.urls.list,
				triggerKeys: ['enter', 'comma', 'tab'],
				initialTags: function(holder) {
					return self.urls.documentList.format(holder.attr('documentId'))
				},
				onAdd: function(holder, value) {
					self.addTag(holder.attr('documentId'), value);
				},
				onRemove: function(holder, value) {
					self.removeTag(holder.attr('documentId'), value);
				}
			});
		}

		$boxId.toggleClass('hidden');
	},
	removeTag: function(documentId, tag, callback) {
		var self = this;
		callback = callback || function() {};
		$.ajax({
			data: {id:documentId, tag:tag},
			error: function() { },
			global: false,
			success: callback,
			type: 'GET',
			url: self.urls.removeTag
		});
	},
	initDragAndDrop: function() {
		$('.draggable').draggable('destroy');
		$('.droppable').droppable('destroy');

		$('.draggable').draggable({
			containment: 'document',
			cursor: 'move',
			helper: 'clone',
			revert: 'invalid'
		});

		$('.droppable', '#tag-results').droppable({
			accept: '.draggable',
			hoverClass: 'active',
			drop: function(event, ui) {
				Tagging.addTag(ui.draggable.attr('documentid'), $(event.target).attr('tag'), function(data) {
					if (ui.draggable.is('.remove')) {
						ui.draggable.fadeOut('fast', function() { $(this).remove(); });
					}

					Tagging.showAllTagged($(event.target).attr('tag'), '#allTagged');
				});
			}
		});

		$('.droppable', '#allTagged').droppable({
			accept: '.draggable',
			hoverClass: 'active',
			drop: function(event, ui) {
				Tagging.addTag(ui.draggable.attr('documentid'), $(event.target).attr('tag'), function(data) {
					if (ui.draggable.is('.remove')) {
						ui.draggable.fadeOut('fast', function() { $(this).remove(); });
					}

					Tagging.showAllTagged($(event.target).attr('tag'), '#allTagged');
				});
			}
		});
	},
	init: function(urls, useDocumentSearch) {
		this.urls = urls;
		this.useDocumentSearch = useDocumentSearch || false;
	}
};
