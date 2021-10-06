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

// HACK to get frame update working without ctypes
// https://stackoverflow.com/questions/65586851/modify-variable-values-in-bdb-without-ctypes
function frameLocalsToFast(frame){
    pyodide._module._PyFrame_LocalsToFast(frame.$$.ptr, 0);
    // Hopefully avoid memory leak
    // frame.destroy();
}

async function clearEnvs(pyodide) {
    const clearEnv =
        "for _k_ in list(sys.modules['__main__'].__dict__.keys()):\n" +
        "\tif _k_ == '__start_env__': continue\n" +
        "\tif _k_ not in __start_env__:\n" +
        "\t\tdel sys.modules['__main__'].__dict__[_k_]\n" +
        "\telse:\n" +
        "\t\tsys.modules['__main__'].__dict__[_k_] = __start_env__[_k_]\n" +
        "import sys\n";
    return pyodide.runPythonAsync(clearEnv);
}

function randomName() {
    let chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
    let str = '';
    for (let i = 0; i < chars.length; i++) {
        str += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return str;
}

async function handleRunpy(pyodide, msg)
{
    // TODO Move this to vscode
    if (!msg.data.values) {
        msg.data.values = '{}';
    }

    const id = msg.data.id;
    const namespace = randomName();
    pyodide.registerJsModule(namespace, msg.data);

    let runPy =
        `from ${namespace} import content, values\n` +
        'sys.stdout = io.StringIO()\n' +
        'sys.stderr = io.StringIO()\n' +
        'run = open("run.py", "r")\n' +
        'pyodide.eval_code(run.read() + "\\nrunpy(content, json.loads(values))\\n", {"content": content, "values": values})\n' +
        '(sys.stdout.getvalue(), sys.stderr.getvalue())\n';

    try {
        await clearEnvs(pyodide);
        const [out, err] = await pyodide.runPythonAsync(runPy);

        self.postMessage(new RTVRunPy(id, ResponseType.STDOUT, out));
        self.postMessage(new RTVRunPy(id, ResponseType.STDERR, err));
        self.postMessage(new RTVRunPy(id, ResponseType.RESULT, out));

        pyodide.unregisterJsModule(namespace);
    }
    catch(err) {
        console.log('runpy failed!');
        console.error(err);

        // Send the exception first
        self.postMessage(new RTVRunPy(id, ResponseType.EXCEPTION, err.toString()));
        self.postMessage(new RTVRunPy(id, ResponseType.RESULT, ''));
    }
}

async function handleSnippy(pyodide, msg) {
    const id = msg.data.id;
    const namespace = randomName();
    pyodide.registerJsModule(namespace, msg.data);

    let snippy =
        `from ${namespace} import parameter\n` +
        'sys.stdout = io.StringIO()\n' +
        'sys.stderr = io.StringIO()\n' +
        'run = open("snippy.py", "r")\n' +
        'pyodide.eval_code(run.read() + "\\nvalidate(parameter)\\n", {"parameter": parameter})\n' +
        '(sys.stdout.getvalue(), sys.stderr.getvalue())\n';

    try {
        await clearEnvs(pyodide);
        const [out, err] = await pyodide.runPythonAsync(snippy);

        self.postMessage(new RTVRunPy(id, ResponseType.STDOUT, out));
        self.postMessage(new RTVRunPy(id, ResponseType.STDERR, err))
        self.postMessage(new RTVRunPy(id, ResponseType.RESULT, out));

        pyodide.unregisterJsModule(namespace);
    } catch (err) {
        console.log('snippy failed!');
        console.error(err);

        // Send the exception first
        self.postMessage(new RTVRunPy(id, ResponseType.EXCEPTION, err.toString()));
        self.postMessage(new RTVRunPy(id, ResponseType.RESULT, ''));
    }
}

function registerHandlers(pyodide) {
    self.onmessage = function(msg) {
        const id = msg.data.id;

        switch (msg.data.type) {
            case RequestType.RUNPY:
                handleRunpy(pyodide, msg);
                break;
            case RequestType.IMGSUM:
                self.postMessage(new RTVRunPy(id, ResponseType.RESULT, ''));
                break;
            case RequestType.SNIPPY:
                handleSnippy(pyodide, msg);
                break;
            default:
                self.postMessage(new RTVRunPy(id, ResponseType.ERROR, 'Unrecognized request: ' + msg));
        }
    }

}

async function loadPythonFile(pyodide, filename, url) {
    return pyodide.runPythonAsync(
        `pythonFile = open("${filename}", "w")\n` +
        `pythonFile.write(pyodide.open_url("${url}").getvalue())\n` +
        'pythonFile.close()');
}

let pyodide;

async function main() {
    importScripts('pyodide.js');
    pyodide = await loadPyodide({ indexURL: "/pyodide/" });

    // First, load the modules we need
    // await Promise.all([pyodide.loadPackage('numpy'), pyodide.loadPackage('Pillow')]);

    // Then, import the modules
    await pyodide.runPythonAsync('import pyodide\nimport os\nimport sys\nimport io\n');

    // Then add our hack
    pyodide.registerJsModule('frame_locals_to_fast', frameLocalsToFast);

    // Then load our custom files
    await Promise.all([loadPythonFile(pyodide, 'run.py', '/editor/run.py'),
                       loadPythonFile(pyodide, 'img-summary.py', '/editor/img-summary.py'),
                       loadPythonFile(pyodide, 'snippy.py', '/editor/snippy.py')]);

    // Then, store the current environment so we can reset it as needed.
    await pyodide.runPythonAsync("__start_env__ = sys.modules['__main__'].__dict__.copy()");

    // Then, register the handlers
    registerHandlers(pyodide);

    // Finally, let the parent know we are ready.
    self.postMessage(new RTVRunPy(-1, ResponseType.LOADED, ''));
}
main();
