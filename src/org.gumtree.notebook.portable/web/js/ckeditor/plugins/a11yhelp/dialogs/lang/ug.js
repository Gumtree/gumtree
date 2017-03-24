﻿/**
 * @license Copyright (c) 2003-2015, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

CKEDITOR.plugins.setLang( 'a11yhelp', 'ug', {
	title: 'قوشۇمچە چۈشەندۈرۈش',
	contents: 'ياردەم مەزمۇنى. بۇ سۆزلەشكۈنى ياپماقچى بولسىڭىز ESC نى بېسىڭ.',
	legend: [
		{
		name: 'ئادەتتىكى',
		items: [
			{
			name: 'قورال بالداق تەھرىر',
			legend: '${toolbarFocus} بېسىلسا قورال بالداققا يېتەكلەيدۇ، TAB ياكى SHIFT+TAB ئارقىلىق قورال بالداق گۇرۇپپىسى تاللىنىدۇ، ئوڭ سول يا ئوقتا توپچا تاللىنىدۇ، بوشلۇق ياكى Enter كۇنۇپكىسىدا تاللانغان توپچىنى قوللىنىدۇ.'
		},

			{
			name: 'تەھرىرلىگۈچ سۆزلەشكۈسى',
			legend:
				'Inside a dialog, press TAB to navigate to the next dialog element, press SHIFT+TAB to move to the previous dialog element, press ENTER to submit the dialog, press ESC to cancel the dialog. When a dialog has multiple tabs, the tab list can be reached either with ALT+F10 or with TAB as part of the dialog tabbing order. With tab list focused, move to the next and previous tab with RIGHT and LEFT ARROW, respectively.'  // MISSING
		},

			{
			name: 'تەھرىرلىگۈچ تىل مۇھىت تىزىملىكى',
			legend: '${contextMenu} ياكى ئەپ كۇنۇپكىسىدا تىل مۇھىت تىزىملىكىنى ئاچىدۇ. ئاندىن TAB ياكى ئاستى يا ئوق كۇنۇپكىسىدا كېيىنكى تىزىملىك تۈرىگە يۆتكەيدۇ؛ SHIFT+TAB ياكى ئۈستى يا ئوق كۇنۇپكىسىدا ئالدىنقى تىزىملىك تۈرىگە يۆتكەيدۇ. بوشلۇق ياكى ENTER كۇنۇپكىسىدا تىزىملىك تۈرىنى تاللايدۇ. بوشلۇق، ENTER ياكى ئوڭ يا ئوق كۇنۇپكىسىدا تارماق تىزىملىكنى ئاچىدۇ. قايتىش تىزىملىكىگە ESC ياكى سول يا ئوق كۇنۇپكىسى ئىشلىتىلىدۇ. ESC كۇنۇپكىسىدا تىل مۇھىت تىزىملىكى تاقىلىدۇ.'
		},

			{
			name: 'تەھرىرلىگۈچ تىزىمى',
			legend: 'تىزىم قۇتىسىدا، كېيىنكى تىزىم تۈرىگە يۆتكەشتە TAB ياكى ئاستى يا ئوق كۇنۇپكىسى ئىشلىتىلىدۇ. ئالدىنقى تىزىم تۈرىگە يۆتكەشتە SHIFT+TAB ياكى ئۈستى يا ئوق كۇنۇپكىسى ئىشلىتىلىدۇ. بوشلۇق ياكى ENTER كۇنۇپكىسىدا تىزىم تۈرىنى تاللايدۇ.ESC كۇنۇپكىسىدا تىزىم قۇتىسىنى يىغىدۇ.'
		},

			{
			name: 'تەھرىرلىگۈچ ئېلېمېنت يول بالداق',
			legend: '${elementsPathFocus} بېسىلسا ئېلېمېنت يول بالداققا يېتەكلەيدۇ، TAB ياكى ئوڭ يا ئوقتا كېيىنكى ئېلېمېنت تاللىنىدۇ، SHIFT+TAB ياكى سول يا ئوقتا ئالدىنقى ئېلېمېنت تاللىنىدۇ، بوشلۇق ياكى Enter كۇنۇپكىسىدا تەھرىرلىگۈچتىكى ئېلېمېنت تاللىنىدۇ.'
		}
		]
	},
		{
		name: 'بۇيرۇق',
		items: [
			{
			name: 'بۇيرۇقتىن يېنىۋال',
			legend: '${undo} نى بېسىڭ'
		},
			{
			name: 'قايتىلاش بۇيرۇقى',
			legend: '${redo} نى بېسىڭ'
		},
			{
			name: 'توملىتىش بۇيرۇقى',
			legend: '${bold} نى بېسىڭ'
		},
			{
			name: 'يانتۇ بۇيرۇقى',
			legend: '${italic} نى بېسىڭ'
		},
			{
			name: 'ئاستى سىزىق بۇيرۇقى',
			legend: '${underline} نى بېسىڭ'
		},
			{
			name: 'ئۇلانما بۇيرۇقى',
			legend: '${link} نى بېسىڭ'
		},
			{
			name: 'قورال بالداق قاتلاش بۇيرۇقى',
			legend: '${toolbarCollapse} نى بېسىڭ'
		},
			{
			name: 'ئالدىنقى فوكۇس نۇقتىسىنى زىيارەت قىلىدىغان بۇيرۇق',
			legend: '${accessPreviousSpace} بېسىپ ^ بەلگىسىگە ئەڭ يېقىن زىيارەت قىلغىلى بولمايدىغان فوكۇس نۇقتا رايونىنىڭ ئالدىنى زىيارەت قىلىدۇ، مەسىلەن: ئۆز ئارا قوشنا ئىككى HR ئېلېمېنت. بۇ بىرىكمە كۇنۇپكا تەكرارلانسا يىراقتىكى فوكۇس نۇقتا رايونىغا يەتكىلى بولىدۇ.'
		},
			{
			name: 'كېيىنكى فوكۇس نۇقتىسىنى زىيارەت قىلىدىغان بۇيرۇق',
			legend: '${accessNextSpace} بېسىپ ^ بەلگىسىگە ئەڭ يېقىن زىيارەت قىلغىلى بولمايدىغان فوكۇس نۇقتا رايونىنىڭ كەينىنى زىيارەت قىلىدۇ، مەسىلەن: ئۆز ئارا قوشنا ئىككى HR ئېلېمېنت. بۇ بىرىكمە كۇنۇپكا تەكرارلانسا يىراقتىكى فوكۇس نۇقتا رايونىغا يەتكىلى بولىدۇ.'
		},
			{
			name: 'توسالغۇسىز لايىھە چۈشەندۈرۈشى',
			legend: '${a11yHelp} نى بېسىڭ'
		}
		]
	}
	],
	backspace: 'Backspace', // MISSING
	tab: 'Tab', // MISSING
	enter: 'Enter', // MISSING
	shift: 'Shift', // MISSING
	ctrl: 'Ctrl', // MISSING
	alt: 'Alt', // MISSING
	pause: 'Pause', // MISSING
	capslock: 'Caps Lock', // MISSING
	escape: 'Escape', // MISSING
	pageUp: 'Page Up', // MISSING
	pageDown: 'Page Down', // MISSING
	end: 'End', // MISSING
	home: 'Home', // MISSING
	leftArrow: 'Left Arrow', // MISSING
	upArrow: 'Up Arrow', // MISSING
	rightArrow: 'Right Arrow', // MISSING
	downArrow: 'Down Arrow', // MISSING
	insert: 'Insert', // MISSING
	'delete': 'Delete', // MISSING
	leftWindowKey: 'Left Windows key', // MISSING
	rightWindowKey: 'Right Windows key', // MISSING
	selectKey: 'Select key', // MISSING
	numpad0: 'Numpad 0', // MISSING
	numpad1: 'Numpad 1', // MISSING
	numpad2: 'Numpad 2', // MISSING
	numpad3: 'Numpad 3', // MISSING
	numpad4: 'Numpad 4', // MISSING
	numpad5: 'Numpad 5', // MISSING
	numpad6: 'Numpad 6', // MISSING
	numpad7: 'Numpad 7', // MISSING
	numpad8: 'Numpad 8', // MISSING
	numpad9: 'Numpad 9', // MISSING
	multiply: 'Multiply', // MISSING
	add: 'Add', // MISSING
	subtract: 'Subtract', // MISSING
	decimalPoint: 'Decimal Point', // MISSING
	divide: 'Divide', // MISSING
	f1: 'F1', // MISSING
	f2: 'F2', // MISSING
	f3: 'F3', // MISSING
	f4: 'F4', // MISSING
	f5: 'F5', // MISSING
	f6: 'F6', // MISSING
	f7: 'F7', // MISSING
	f8: 'F8', // MISSING
	f9: 'F9', // MISSING
	f10: 'F10', // MISSING
	f11: 'F11', // MISSING
	f12: 'F12', // MISSING
	numLock: 'Num Lock', // MISSING
	scrollLock: 'Scroll Lock', // MISSING
	semiColon: 'Semicolon', // MISSING
	equalSign: 'Equal Sign', // MISSING
	comma: 'Comma', // MISSING
	dash: 'Dash', // MISSING
	period: 'Period', // MISSING
	forwardSlash: 'Forward Slash', // MISSING
	graveAccent: 'Grave Accent', // MISSING
	openBracket: 'Open Bracket', // MISSING
	backSlash: 'Backslash', // MISSING
	closeBracket: 'Close Bracket', // MISSING
	singleQuote: 'Single Quote' // MISSING
} );
