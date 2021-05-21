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
			value: "\"\"\"\n" +
				"Abbreviate\n" +
				"\tReturn the abbreviation of\n" +
				"\tthe given name:\n" +
				"\t>>> task('Alan Turing') == 'A.T'\n" +
				"\"\"\"\n" +
				"def task(name):\n" +
				"\t# 1. Split the name into words\n" +
				"\t# 2. Get the first letter of each\n" +
				"\t# 3. Put dots between them\n" +
				"\tabbr = ''\n" +
				"\treturn abbr\n" +
				"\n" +
				"task('Augusta Ada King')\n",
			language: 'python',
			theme: 'vs-dark',
			automaticLayout: true,
			quickSuggestions: false,
			fontSize: 15,
			minimap: {
				enabled: false
			}
		});
});
