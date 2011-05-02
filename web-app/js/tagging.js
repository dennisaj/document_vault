var Tagging = {
	addTag: function(documentId, tag, callback) {
		callback = callback || function() {};
		$.ajax({
			data: {id:documentId, tag:tag},
			global: false,
			success: callback,
			type: 'GET',
			url: '/document_vault/tag/document/add'
		});
	},
	create: function(name, callback) {
		$.ajax({
			data: {id:holder.attr('documentid'), tag:value},
			global: false,
			success: callback,
			type: 'GET',
			url: '/document_vault/tag/create/' + name
		});
	},
	showAllTagged: function(name, displayId) {
		var $displayId = $(displayId)
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
			url: '/document_vault/tag/documents/' + name
		});
	},
	showTagbox: function(boxId) {
		var $boxId = $(boxId);
		// Delay the initialization of the tagbox until the first time it is shown.
		if (!$boxId.is('.tagit')) {
			$boxId.tagit({
				tagSource: '/document_vault/tag/list',
				triggerKeys: ['enter', 'comma', 'tab'],
				initialTags: function(holder) {
					return '/document_vault/tag/document/list/' + holder.attr('documentId')
				},
				onAdd: function(holder, value) {
					Tagging.addTag(holder.attr('documentId'), value);
				},
				onRemove: function(holder, value) {
					Tagging.removeTag(holder.attr('documentId'), value);
				}
			});
		}

		$boxId.toggleClass('hidden');
	},
	removeTag: function(documentId, tag, callback) {
		callback = callback || function() {};
		$.ajax({
			data: {id:documentId, tag:tag},
			error: function() { },
			global: false,
			success: callback,
			type: 'GET',
			url: '/document_vault/tag/document/remove'
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

		$('.droppable', '#results').droppable({
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
	}
};
