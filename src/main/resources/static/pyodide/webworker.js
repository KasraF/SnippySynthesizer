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

class RunResult {
	constructor(id, type, stdout, stderr, exitCode, result) {
		this.id = id;
		this.type = type;
		this.stdout = stdout;
		this.stderr = stderr;
		this.exitCode = exitCode;
		this.result = result;
	}
}


// HACK to get frame update working without ctypes
// https://stackoverflow.com/questions/65586851/modify-variable-values-in-bdb-without-ctypes
function frameLocalsToFast(frame){
   pyodide._module._PyFrame_LocalsToFast(frame.$$.ptr, 0);
   // Hopefully avoid memory leak
   frame.destroy();
}

console.log("Importing Pyodide script...");
importScripts('./pyodide.js');

loadPyodide({ indexURL : "/pyodide" })
	.then(() => {
		console.log('NOT importing Numpy and Pillow.');
		console.log('Adding frame_locals_to_fast function.');
		pyodide.registerJsModule('frame_locals_to_fast', frameLocalsToFast)
	})
	.then(() => {
		console.log("Importing pyodide and os...");
		return pyodide.runPythonAsync('import pyodide\nimport os\n');
	})
	.then(() => {
		console.log("Loading run.py and snippy.py");
		const runpy = pyodide.runPythonAsync(
			'import pyodide\n' +
			'import os\n' +
			'import sys\n' +
			'import io\n' +
			'runpy = open("run.py", "w")\n' +
			'runpy.write(pyodide.open_url("/editor/run.py").getvalue())\n' +
			'runpy.close()');
		const snippy = pyodide.runPythonAsync(
			'import pyodide\n' +
			'import os\n' +
			'snippy = open("snippy.py", "w")\n' +
			'snippy.write(pyodide.open_url("/editor/snippy.py").getvalue())\n' +
			'snippy.close()');
		return Promise.all([runpy, snippy]);
	})
	.then(() => {
		// Store the load environment to use later.
		return pyodide.runPythonAsync(
			"__start_env__ = sys.modules['__main__'].__dict__.copy()");
	})
	.then(() => {
		// Replace the console to forward Pyodide output to Monaco
		self.postMessage(new RunResult(-1, ResponseType.LOADED));
	});

self.onmessage = function (msg) {
	const id = msg.data.id;

	switch (msg.data.type) {
		case RequestType.RUNPY:
		{
			const name = msg.data.name;
			let content = msg.data.program;
			let values = msg.data.values;

			// Sanitize input
			if (content.includes('"""')) {
				content = content.split('"""').join('\\"\\"\\"');
			}
			if (content.includes('\\n')) {
				content = content.split('\\n').join('\\\\n');
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
						pyodide.runPythonAsync(readStd),
						pyodide.runPythonAsync(readErr),
						pyodide.runPythonAsync(readOut),
					]);
				})
				.then(([stdout, stderr, result]) => {
					self.postMessage(new RunResult(id, ResponseType.RESULT, stdout, stderr, 0, result));
				})
				.catch((err) => {
					console.error('Pyodide exception: ');
					console.error(err);

					// Then send the outputs
					Promise
						.all([
							pyodide.runPythonAsync(readStd),
							pyodide.runPythonAsync(readErr),
							pyodide.runPythonAsync(readOut)
						])
						.then(([stdout, stderr, result]) =>
							self.postMessage(new RunResult(id, ResponseType.RESULT, stdout, stderr, 1, result)));
				})
				.finally(() => {
					return pyodide.runPythonAsync(cleanup);
				});
			}
			break;
		case RequestType.IMGSUM:
		{
			// We don't support this
			self.postMessage(new RunResult(id, ResponseType.RESULT, '', 'img-summary not supported', 1, ''));
			break;
		}
		// TODO This needs to be comverted to new output as well.
		case RequestType.SNIPPY:
		{
			const action = msg.data.action;
			const parameter = msg.data.parameter;

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
						pyodide.runPythonAsync(readStd),
						pyodide.runPythonAsync(readErr),
					]);
				})
				.then(([stdout, stderr]) => {
					self.postMessage(new RunResult(id, ResponseType.RESULT, stdout, stderr, 0, stdout));
				})
				.catch((err) => {
					console.error('Pyodide exception: ');
					console.error(err);

					// Then send the outputs
					Promise
						.all([
							pyodide.runPythonAsync(readStd),
							pyodide.runPythonAsync(readErr)
						])
						.then(([stdout, stderr]) =>
							self.postMessage(new RunResult(id, ResponseType.RESULT, stdout, stderr, 1, stdout)));
				})
				.finally(() => {
					return pyodide.runPythonAsync(cleanup);
				});
			}
			break;
		default:
			console.error('Unrecognized request: ' + msg);
	}
}
