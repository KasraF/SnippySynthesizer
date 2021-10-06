import * as monaco from 'monaco-editor';
import * as $ from 'jquery';

self.MonacoEnvironment = {
	getWorkerUrl: function (moduleId, label) {
		if (label === 'json') {
			return './json.worker.bundle.js';
		}
		if (label === 'css') {
			return './css.worker.bundle.js';
		}
		if (label === 'html') {
			return './html.worker.bundle.js';
		}
		if (label === 'typescript' || label === 'javascript') {
			return './ts.worker.bundle.js';
		}
		return './editor.worker.bundle.js';
	}
}

$(function() {
	window.editor = monaco.editor.create(
		document.getElementById('editor'), 
		{
			value: `"""
String Compression:
	Implement a method for basic string
	compression, using the count of
	repeated characters. You can assume
	that the string is non-empty, and
	only contains alphabetic characters.
	>>> task('aabccca')
	'2a1b3c1a'
"""
def task(s):
	rs = ''

	return rs

task('aabccca')

"""
+----------------+
|  Cheat Sheet:  |
+----------------+
variable = ??
Tab				-> Go to next variable/row
Shift + Tab		-> Go to previous variable/row
Shift + Enter	-> Add example, go to next item
Enter			-> Submit examples to LooPy
Esc				-> Exit LooPy
"""`,
			language: 'python',
			theme: 'vs-dark',
			automaticLayout: true,
			fontSize: 15,
            quickSuggestions: false,
            cursorBlinking: 'solid',
            matchBrackets: false,
			minimap: {
				enabled: false
			}
		});

    // Customize the editor for the web version
	editor.getContribution('editor.contrib.rtv')._config.updateValue('rtv.box.showBoxAtLoopStatements', false);
    editor.getContribution('editor.contrib.rtv')._config.updateValue('rtv.box.showBoxAtEmptyLines', false);
    editor.getContribution('editor.contrib.rtv')._config.updateValue('rtv.box.showBoxWhenNotExecuted', false);
});
