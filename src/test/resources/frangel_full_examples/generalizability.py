import re
import json

def do_program(bench,prog):
    with open(bench + '.allex.json") as json_file:
        data = json.load(json_file)
    full_prog = data['loopheader'] + '\n' + '\n'.join(['  ' + l for l in curr_program.split('\n')])
    success = 0
    for example in data['examples']:
        if test_example(example,full_prog):
            success += 1
    print(bench,success,len(data['examples']))

def test_example(example,full_prog):
    globs = {k: eval(example['in'][k]) for k in example['in'] if k != '#'}
    exec(full_prog,globs)
    for v in example['out']:
        if globs[v] != example['out'][v]:
            return False
    return True

results_file = None
with open(results_file) as f:
    lines = f.readlines()
curr_program = ''
tab = 0
bench_name = ''
for l in lines:
	m = re.match(r'^\(.*?\) \[.\] \[(.*?)\] \[.*?\] \[.*?\] (.*)$',l)
	if m:
		if curr_program: do_program(bench_name,curr_program)
		curr_program = m[2]
		bench_name = m[1].strip()
		tab = len(m[0]) - len(m[2])
	elif l.startswith(' ') and curr_program:
		curr_program += '\n' + l[tab:].rstrip()
if curr_program: do_program(bench_name,curr_program)

