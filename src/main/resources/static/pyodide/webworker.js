/**
 * This webworker will be spawned once on page load, and will handle queries of the RTVRunPy class.
 */

const RequestType = {
	RUNPY: 1,
	IMGSUM: 2,
	SNIPPY: 3,
}

const ResponseType = {
	// Responses related to the running program
	STDOUT: 1,
	STDERR: 2,
	RESULT: 3,
	EXCEPTION: 4,

	// Responses related to the web worker itself
	ERROR: 5,
	LOADED: 6
}

class RTVRunPy {
	constructor(id, type, msg) {
		this.id = id;
		this.type = type;
		this.msg = msg;
	}
}

self.languagePluginUrl = '/pyodide/';
console.log("Importing Pyodide script...");
importScripts('./pyodide.js');
languagePluginLoader
	.then(() => {
		console.log("Importing Numpy and Pillow...");
		return Promise.all([pyodide.loadPackage('numpy'), pyodide.loadPackage('Pillow')]);
	})
	.then(() => {
		console.log("Importing pyodide and os...");
		return pyodide.runPythonAsync('import pyodide\nimport os\n');
	})
	.then(() => {
		console.log("Loading run.py, img-summary.py, and snippy.py");
		const runpy = pyodide.runPythonAsync(
			'import pyodide\n' +
			'import os\n' +
			'import sys\n' +
			'import io\n' +
			'runpy = open("run.py", "w")\n' +
			'runpy.write(pyodide.open_url("/editor/run.py").getvalue())\n' +
			'runpy.close()');
		const imgSummary = pyodide.runPythonAsync(
			'import pyodide\n' +
			'import os\n' +
			'img_summary = open("img-summary.py", "w")\n' +
			'img_summary.write(pyodide.open_url("/editor/img-summary.py").getvalue())\n' +
			'img_summary.close()');
		const snippy = pyodide.runPythonAsync(
			'import pyodide\n' +
			'import os\n' +
			'snippy = open("snippy.py", "w")\n' +
			'snippy.write(pyodide.open_url("/editor/snippy.py").getvalue())\n' +
			'snippy.close()');
		return Promise.all([runpy, imgSummary, snippy]);
	})
	.then(() => {
		// Store the load environment to use later.
		return pyodide.runPythonAsync(
			"__start_env__ = sys.modules['__main__'].__dict__.copy()");
	})
	.then(() => {
		// Replace the console to forward Pyodide output to Monaco
		self.postMessage(new RTVRunPy(-1, ResponseType.LOADED, ''));
	});

self.onmessage = function (msg) {
	const id = msg.data.id;

	switch (msg.data.type) {
		case RequestType.RUNPY:
		{
			const name = msg.data.name;
			let content = msg.data.content;
			let values = msg.data.values;

			console.log('calling run.py with content:');
			console.log(content);
			console.log('and values:');
			console.log(values);

			while (content.includes('"""')) {
				content = content.replace('"""', '\\"\\"\\"');
			}

			const clearEnv =
				"for _k_ in list(sys.modules['__main__'].__dict__.keys()):\n" +
				"\tif _k_ == '__start_env__': continue\n" +
				"\tif _k_ not in __start_env__:\n" +
				"\t\tdel sys.modules['__main__'].__dict__[_k_]\n" +
				"\telse:\n" +
				"\t\tsys.modules['__main__'].__dict__[_k_] = __start_env__[_k_]\n" +
				"import sys\n";

			let runPy =
				`sys.stdout = io.StringIO()\n` +
				`sys.stderr = io.StringIO()\n` +
				`program = """${content}\n"""\n` +
				`code = open("${name}", "w")\n` +
				`code.write(program)\n` +
				`code.close()\n` +
				`run = open("run.py", "r")\n`;

			if (!values) {
				runPy += `pyodide.eval_code(run.read() + "\\nmain(\'${name}\', None)\\n", {})`;
			} else {
				// We need to save the values to a file first.
				const values_file = `values_${name}`;
				runPy +=
					`values = """${values}\n"""\n` +
					`file = open("${values_file}", "w")\n` +
					`file.write(values)\n` +
					`file.close()\n` +
					`pyodide.eval_code(run.read() + "\\nmain(\'${name}\', \'${values_file}\')\\n", {})`;
			}

			const readStd = `sys.stdout.getvalue()`;
			const readErr = `sys.stderr.getvalue()`;
			const readOut =
				`if os.path.exists("${name}.out"):\n` +
				`\tfile = open("${name}.out")\n` +
				`\trs = file.read()\n` +
				`\tos.remove("${name}.out")\n` +
				`else:\n` +
				`\t rs = ""\n` +
				`rs`;
			const cleanup =
				`if os.path.exists("${name}"):\n` +
				`\tos.remove("${name}")`;

			pyodide.runPythonAsync(clearEnv)
				.then(() => {
					return pyodide.runPythonAsync(runPy);
				})
				.then(() => {
					// Read Stdout and Stderr in any order
					return Promise.all([
						pyodide.runPythonAsync(readStd).then((out) => self.postMessage(new RTVRunPy(id, ResponseType.STDOUT, out))),
						pyodide.runPythonAsync(readErr).then((err) => self.postMessage(new RTVRunPy(id, ResponseType.STDERR, err))),
					]);
				})
				.then(() => {
					// Read the output
					return pyodide.runPythonAsync(readOut);
				})
				.then((out) => {
					self.postMessage(new RTVRunPy(id, ResponseType.RESULT, out));
				})
				.catch((err) => {
					// Send the exception first
					self.postMessage(new RTVRunPy(id, ResponseType.EXCEPTION, err.toString()));

					// Then send the outputs
					Promise
						.all([
							pyodide.runPythonAsync(readStd).then((out) => self.postMessage(new RTVRunPy(id, ResponseType.STDOUT, out))),
							pyodide.runPythonAsync(readErr).then((err) => self.postMessage(new RTVRunPy(id, ResponseType.STDERR, err)))])
						.then(() => pyodide.runPythonAsync(readOut))
						.then((out) => self.postMessage(new RTVRunPy(id, ResponseType.RESULT, out)));
				})
				.finally(() => {
					return pyodide.runPythonAsync(cleanup);
				});
			}
			break;
		case RequestType.IMGSUM:
		{
			const name = msg.data.name;
			let content = msg.data.content;
			let line = msg.data.line;
			let varname = msg.data.varname;

			while (content.includes('"""')) {
				content = content.replace('"""', '\\"\\"\\"');
			}

			const clearEnv =
				"for _k_ in list(sys.modules['__main__'].__dict__.keys()):\n" +
				"\tif _k_ == '__start_env__': continue\n" +
				"\tif _k_ not in __start_env__:\n" +
				"\t\tdel sys.modules['__main__'].__dict__[_k_]\n" +
				"\telse:\n" +
				"\t\tsys.modules['__main__'].__dict__[_k_] = __start_env__[_k_]\n" +
				"import sys\n";
			const readStd = `sys.stdout.getvalue()`;
			const readErr = `sys.stderr.getvalue()`;
			const runPy =
				`sys.stdout = io.StringIO()\n` +
				`sys.stderr = io.StringIO()\n` +
				`program = """${content}"""\n` +
				`code = open("${name}", "w")\n` +
				`code.write(program)\n` +
				`code.close()\n` +
				`img_summary = open("img-summary.py", "r")\n` +
				`pyodide.eval_code(img_summary.read() + "\\nmain(\'${name}\', \'${line}\', \'${varname}\')\\n", {})`;
			const readOut = `if os.path.exists("${name}.out"):\n` +
				`\tfile = open("${name}.out")\n` +
				`\trs = file.read()\n` +
				`\tos.remove("${name}.out")\n` +
				`else:\n` +
				`\t rs = ""\n` +
				`rs`;
			const cleanup =
				`if os.path.exists("${name}"):\n` +
				`\tos.remove("${name}")`;

			pyodide.runPythonAsync(clearEnv)
				.then(() => {
					return pyodide.runPythonAsync(runPy);
				})
				.then(() => {
					// Read Stdout and Stderr in any order
					return Promise.all([
						pyodide.runPythonAsync(readStd).then((out) => self.postMessage(new RTVRunPy(id, ResponseType.STDOUT, out))),
						pyodide.runPythonAsync(readErr).then((err) => self.postMessage(new RTVRunPy(id, ResponseType.STDERR, err))),
					]);
				})
				.then(() => {
					// Read output
					return pyodide.runPythonAsync(readOut);
				})
				.then((out) => {
					self.postMessage(new RTVRunPy(id, ResponseType.RESULT, out));
				})
				.catch((err) => {
					// Send the exception first
					self.postMessage(new RTVRunPy(id, ResponseType.EXCEPTION, err.toString()));

					// Then send the outputs
					Promise
						.all([
							pyodide.runPythonAsync(readStd).then((out) => self.postMessage(new RTVRunPy(id, ResponseType.STDOUT, out))),
							pyodide.runPythonAsync(readErr).then((err) => self.postMessage(new RTVRunPy(id, ResponseType.STDERR, err)))])
						.then(() => pyodide.runPythonAsync(readOut))
						.then((out) => self.postMessage(new RTVRunPy(id, ResponseType.RESULT, out)));
				})
				.finally(() => {
					return pyodide.runPythonAsync(cleanup);
				});
			}
			break;
		case RequestType.SNIPPY:
		{
			const action = msg.data.action;
			const parameter = msg.data.parameter;

			console.log('calling snippy.py with action:');
			console.log(action);
			console.log('and parameter:');
			console.log(parameter);

			// while (content.includes('"""')) {
			// 	content = content.replace('"""', '\\"\\"\\"');
			// }

			const clearEnv =
				"for _k_ in list(sys.modules['__main__'].__dict__.keys()):\n" +
				"\tif _k_ == '__start_env__': continue\n" +
				"\tif _k_ not in __start_env__:\n" +
				"\t\tdel sys.modules['__main__'].__dict__[_k_]\n" +
				"\telse:\n" +
				"\t\tsys.modules['__main__'].__dict__[_k_] = __start_env__[_k_]\n" +
				"import sys\n";

			const snippy =
				`sys.stdout = io.StringIO()\n` +
				`sys.stderr = io.StringIO()\n` +
				`run = open("snippy.py", "r")\n` +
				`paramText = """${parameter}"""\n` +
				`param = open('param.txt', 'w')\n` +
				`param.write(paramText)\n` +
				`param.close()\n` +
 				`pyodide.eval_code(run.read() + "\\nmain(\'${action}\', open(\'param.txt\').read())\\n", {})`;

			const readStd = `sys.stdout.getvalue()`;
			const readErr = `sys.stderr.getvalue()`;
			const cleanup =
				`if os.path.exists("${name}"):\n` +
				`\tos.remove("${name}")`;

			pyodide.runPythonAsync(clearEnv)
				.then(() => {
					return pyodide.runPythonAsync(snippy);
				})
				.then(() => {
					// Read Stdout and Stderr in any order
					return Promise.all([
						pyodide.runPythonAsync(readStd).then((out) => self.postMessage(new RTVRunPy(id, ResponseType.STDOUT, out))),
						pyodide.runPythonAsync(readErr).then((err) => self.postMessage(new RTVRunPy(id, ResponseType.STDERR, err))),
					]);
				})
				.then(() => {
					// Read the output
					return pyodide.runPythonAsync(readOut);
				})
				.then((out) => {
					self.postMessage(new RTVRunPy(id, ResponseType.RESULT, out));
				})
				.catch((err) => {
					// Send the exception first
					self.postMessage(new RTVRunPy(id, ResponseType.EXCEPTION, err.toString()));

					// Then send the output
					// Unlike runpy and img-summary, the only output here is the stdout, so we send that twice.
					// In theory, just sending stdout is enough, but the other thread's code uses RESULT to find
					// out when a request has finished.
					Promise
						.all([
							pyodide.runPythonAsync(readStd).then((out) => self.postMessage(new RTVRunPy(id, ResponseType.STDOUT, out))),
							pyodide.runPythonAsync(readErr).then((err) => self.postMessage(new RTVRunPy(id, ResponseType.STDERR, err))),
							pyodide.runPythonAsync(readStd).then((out) => self.postMessage(new RTVRunPy(id, ResponseType.RESULT, out)))]);
				})
				.finally(() => {
					return pyodide.runPythonAsync(cleanup);
				});
			}
			break;
		default:
			self.postMessage(new RTVRunPy(id, ResponseType.ERROR, 'Unrecognized request: ' + msg));
	}
}
