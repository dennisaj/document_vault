/*
 * Version 1.7
 *
 * http://mosttw.wordpress.com/
 *
 * Licensed under MIT License: http://en.wikipedia.org/wiki/MIT_License
 *
 * Copyright (c) 2010 MostThingsWeb

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 * Changelog:
 *
 * Version 1.7
 * - Fixed a variable naming issue
 *
 * Version 1.6.1 (Minified version only)
 * - (Deprecated)
 * - Fixed an issue during minifcation
 *
 * Version 1.6
 * - (Deprecated)
 * - Fixed overlay overflow issues in IE
 * - Fixed overlay fadeIn in IE
 *
 *
 * Version 1.5
 * - Release
 *
 */

/*
 *
 * Portions of this software come from jQuery UI Dialog 1.8.4
 * License (below):
 *
 * jQuery UI Dialog 1.8.4
 *
 * Copyright 2010, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Dialog
 *
 * Depends:
 *    jquery.ui.core.js
 *    jquery.ui.widget.js
 *  jquery.ui.button.js
 *    jquery.ui.draggable.js
 *    jquery.ui.mouse.js
 *    jquery.ui.position.js
 *    jquery.ui.resizable.js
 */

(function($) {
    // Get the dialog widget factory prototype
    var proto = $.ui.dialog.prototype;

    // Members need access to these class names
    var uiDialogClasses = 'ui-dialog ' + 'ui-widget ' + 'ui-widget-content ' + 'ui-corner-all ';

    // Internal function used to generate a random ID
    function randomID() {
	var id = "";
	for ( var i = 1; i <= 10; i++)
	    id += (Math.floor(Math.random() * 10) + 1);
	return id;
    }

    // To fade in the overlay without it going crazy, we need to know the opacity level
    // set in the CSS
    var overlayOpacity;
    var overlayTestID = randomID();
    var overlayReg = /Opacity=(\d+)/;

    // Extract the opacity level
    $("body").append("<div id='" + overlayTestID + "' class='ui-widget-overlay' style='display: none;'></div>");

    try {
	var overlayFilter = $("#" + overlayTestID).css("filter");
	if (overlayReg.test(overlayFilter))
	    overlayOpacity = overlayReg.exec(overlayFilter)[1];
    }
    catch (ex){}

    // If no overlay opacity is defined, assume 0 opacity
    if (!overlayOpacity)
	overlayOpacity = 0;

    $("#" + overlayTestID).remove();

    // Store the modified create() method
    var _createMod = function() {
	this.originalTitle = this.element.attr('title');
	// #5742 - .attr() might return a DOMElement
	if (typeof this.originalTitle !== "string")
	    this.originalTitle = "";
	var self = this, options = self.options,

	title = options.title || self.originalTitle || '&#160;', titleId = $.ui.dialog
	.getTitleId(self.element),

	uiDialog = (self.uiDialog = $('<div></div>')).appendTo(document.body)
	.hide().addClass(uiDialogClasses + options.dialogClass).css( {
	    zIndex : options.zIndex
	})
	// setting tabIndex makes the div focusable
	// setting outline to 0 prevents a border on focus in Mozilla
	.attr('tabIndex', -1).css('outline', 0).keydown(
	    function(event) {
		if (options.closeOnEscape && event.keyCode
		    && event.keyCode === $.ui.keyCode.ESCAPE) {

		    self.close(event);
		    event.preventDefault();
		}
	    }).attr( {
	    role : 'dialog',
	    'aria-labelledby' : titleId
	}).mousedown(function(event) {
	    self.moveToTop(false, event);
	}),

	uiDialogContent = self.element.show().removeAttr('title').addClass(
	    'ui-dialog-content ' + 'ui-widget-content').appendTo(uiDialog),

	uiDialogTitlebar = (self.uiDialogTitlebar = $('<div></div>'))
	.addClass(
	    'ui-dialog-titlebar ' + 'ui-widget-header ' + 'ui-corner-all ' + 'ui-helper-clearfix')
	.prependTo(uiDialog);

	// Control the creation of the close 'X'
	if (options.hasClose)
	    var uiDialogTitlebarClose = $('<a href="#"></a>').addClass(
		'ui-dialog-titlebar-close ' + 'ui-corner-all').attr('role',
		'button').hover(function() {
		uiDialogTitlebarClose.addClass('ui-state-hover');
	    }, function() {
		uiDialogTitlebarClose.removeClass('ui-state-hover');
	    }).focus(function() {
		uiDialogTitlebarClose.addClass('ui-state-focus');
	    }).blur(function() {
		uiDialogTitlebarClose.removeClass('ui-state-focus');
	    }).click(function(event) {
		self.close(event);
		return false;
	    }).appendTo(uiDialogTitlebar),

	    uiDialogTitlebarCloseText = (self.uiDialogTitlebarCloseText = $('<span></span>'))
	    .addClass('ui-icon ' + 'ui-icon-closethick').text(
		options.closeText).appendTo(uiDialogTitlebarClose);

	var uiDialogTitle = $('<span></span>').addClass('ui-dialog-title')
	.attr('id', titleId).html(title).prependTo(uiDialogTitlebar);

	// handling of deprecated beforeclose (vs beforeClose) option
	// Ticket #4669 http://dev.jqueryui.com/ticket/4669
	// TODO: remove in 1.9pre
	if ($.isFunction(options.beforeclose)
	    && !$.isFunction(options.beforeClose))
	    options.beforeClose = options.beforeclose;

	uiDialogTitlebar.find("*").add(uiDialogTitlebar).disableSelection();

	if (options.draggable && $.fn.draggable)
	    self._makeDraggable();
	if (options.resizable && $.fn.resizable)
	    self._makeResizable();
	self._createButtons(options.buttons);
	self._isOpen = false;
	if ($.fn.bgiframe)
	    uiDialog.bgiframe();
    };

    // Override the destroy method to control the overlay
    proto.destroy = function() {
	var self = this;
	self.uiDialog.hide();
	self.element.unbind('.dialog').removeData('dialog').removeClass(
	    'ui-dialog-content ui-widget-content').hide().appendTo('body');
	self.uiDialog.remove();
	if (self.originalTitle)
	    self.element.attr('title', self.originalTitle);
	return self;
    };

    proto.close = function() {
	$.hideDialog();
    };

    // Override the open method to add fadeIn effect
    proto.open = function(){
	if (this._isOpen)
	    return;

	var self = this, options = self.options, uiDialog = self.uiDialog;

	// If an overlay is open, remember that
	var overlayOpen = $(".ui-widget-overlay:visible").size() != 0;

	// Open overlay
	self.overlay = options.modal ? new $.ui.dialog.overlay(self) : null;

	if (uiDialog.next().length)
	    uiDialog.appendTo('body');

	self._size();
	self._position(options.position);

	var $dialog = uiDialog.show(options.show);

	// Fix the modal positioning in IE
	if (options.modal)
	    $(".ui-widget-overlay").css("position", "fixed")

	// Add fadeIn effect
	if (options.fadeIn) {
	    if (!overlayOpen && options.modal){
		// IE needs to have the filter attribute applied before it fades in
		$(".ui-widget-overlay").hide().css('filter', 'alpha(opacity=' + overlayOpacity + ')').fadeIn("normal");
	    }
	    $dialog.hide().fadeIn("normal");
	}

	self.moveToTop(true);

	// prevent tabbing out of modal dialogs
	if (options.modal) {
	    uiDialog.bind('keypress.ui-dialog', function(event) {
		if (event.keyCode !== $.ui.keyCode.TAB)
		    return;

		var tabbables = $(':tabbable', this), first = tabbables
		.filter(':first'), last = tabbables.filter(':last');

		if (event.target === last[0] && !event.shiftKey) {
		    first.focus(1);
		    return false;
		} else if (event.target === first[0] && event.shiftKey) {
		    last.focus(1);
		    return false;
		}
	    });
	}

	// set focus to the first tabbable element in the content area or the
	// first button
	// if there are no tabbable elements, set focus on the dialog itself
	$(
	    self.element.find(':tabbable').get().concat(
		uiDialog.find('.ui-dialog-buttonpane :tabbable').get()
		.concat(uiDialog.get()))).eq(0).focus();

	self._trigger('open');
	self._isOpen = true;

	return self;
    };

    // Internal function used for getting the element on top
    function getTopElement(elems) {
	// Store the greates z-index that has been seen so far
	var maxZ = 0;
	// Stores a reference to the element that has the greatest z-index so
	// far
	var maxElem;
	// Check each element's z-index
	elems.each(function() {
	    // If it's bigger than the currently biggest one, store the value
	    // and reference
	    if ($(this).css("z-index") > maxZ) {
		maxElem = $(this);
		maxZ = $(this).css("z-index");
	    }
	});
	// Finally, return the reference to the element on top
	return maxElem;
    }

    $.showDialog = function(title, prompt, args) {
	var options = {
	    resizable : false,
	    draggable : true,
	    closeOnEscape : false,
	    moveToTop : true,
	    title : title,
	    hasClose : true,
	    fadeIn : true
	};

	$.extend(options, args);

	// Add some custom options
	if (!options.hasClose)
	    proto._create = _createMod;

	var id = randomID();

	if ($("#dialogContainer").size() == 0)
	    $("body").append("<div id='dialogContainer'></div>");

	// Add a div for the dialog after the special dialogContainer target div
	$("#dialogContainer").after(
	    "<div id='m" + id + "'><p><div id='mp" + id + "'>" + prompt
	    + "</div></p></div>");

	$("#m" + id).dialog(options);

	// Remove duplicate overlays
	if ($(".ui-widget-overlay").size() > 1)
	    $(".ui-widget-overlay:first").remove();

	return id;
    };

    $.hideDialog = function(id, fadeOut) {
	// If an ID was not supplied, get the ID of the dialog currently on top
	id = id
	|| "#"
	+ getTopElement($(".ui-dialog")).find(".ui-dialog-content")
	.attr("id");

	fadeOut = fadeOut || true;

	// Remove the dialog
	$(id).parent().andSelf().remove();

	// If no dialogs are currently visible, remove the overlay
	if ($(".ui-dialog:visible").size() === 0) {
	    if (fadeOut)
		$(".ui-widget-overlay").fadeOut("normal");
	    else
		$(".ui-widget-overlay").hide();
	} else {
	    // If one or more overlays exist, change the z-index of the overlay
	    // so it is below the top-most dialog
	    $(".ui-widget-overlay")
	    .css(
	    {
		"z-index" : parseInt(
		    getTopElement($(".ui-dialog:visible"))
		    .css("z-index"), 10) - 1
	    });
	}
	// Remove event blocking left over from the overlay
	$.map('focus,mousedown,mouseup,keydown,keypress,click'.split(','),
	    function(event) {
		$(document).unbind(event + '.dialog-overlay');
	    });
    };
    $.clearDialogs = function(fadeOut) {
	fadeOut = fadeOut || true;
	// Find all the dialogs
	$(".ui-dialog").each(function() {
	    // Remove them
	    $(this).find(".ui-dialog-content").parent().andSelf().remove();
	});
	// Remove the overlay
	if (fadeOut)
	    $(".ui-widget-overlay").fadeOut("normal");
	else
	    $(".ui-widget-overlay").hide();
	// Remove event blocking left over from the overlay
	$.map('focus,mousedown,mouseup,keydown,keypress,click'.split(','),
	    function(event) {
		$(document).unbind(event + '.dialog-overlay');
	    });
    };

    $.alert = function(prompt, arg) {
	var args;
	args = $.extend(args, arg, {
	    buttons : {
		"Ok" : function() {
		    // When the Ok button is clicked, just hide this dialog
		    $.hideDialog(this);
		}
	    }
	} );
	return $.showDialog("Info", prompt, args);
    };

    $.confirm = function(prompt, yes, no, arg) {
	var args;
	args = $.extend(args, arg, {
	    buttons : {
		"No" : function() {
		    (no || $.noop).call();
		    $.hideDialog(this);
		},
		"Yes" : function() {
		    (yes || $.noop).call();
		    $.hideDialog(this);
		}
	    }
	});
	return $.showDialog("Confirm", prompt, args);
    };

})(jQuery);