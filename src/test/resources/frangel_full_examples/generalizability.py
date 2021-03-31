import re
import json
import os

def do_program(bench,prog):
    fname = bench + '.allex.json'
    if not os.path.isfile(fname):
        print(bench,'-','-')
        return
    with open(fname) as json_file:
        data = json.load(json_file)
    full_prog = data['loopheader'] + '\n' + '\n'.join(['  ' + l for l in prog.split('\n')])
    #print(full_prog)
    success = 0
    for example in data['examples']:
        if test_example(example,full_prog):
            success += 1
    print(bench,success,len(data['examples']))

def test_example(example,full_prog):
    try:
        globs = {k: eval(example['in'][k]) for k in example['in'] if k != '#'}
        exec(full_prog,globs)
        #print(example['out'])
        for v in example['out']:
            if globs[v] != eval(example['out'][v]):
                return False
        return True
    except:
        return False

def do_compares(results_file):
    with open(results_file) as f:
        lines = f.readlines()
    curr_program = ''
    tab = 0
    bench_name = ''
    for l in lines:
        m = re.match(r'^\(.*?\) \[.\] \[(.*?)\] \[.*?\] \[.*?\] (.*)$',l)
        if m:
            if curr_program: do_program(bench_name,curr_program)
            curr_program = m.group(2)
            bench_name = m.group(1).strip()
            tab = len(m.group(0)) - len(m.group(2))
        elif l.startswith(' ') and curr_program:
            curr_program += '\n' + l[tab:].rstrip()
    if curr_program: do_program(bench_name,curr_program)

