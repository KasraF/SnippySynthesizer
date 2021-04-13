import csv

regular = set()
with open('benchmarks_5min_24G.csv') as csvfile:
	reader = csv.DictReader(csvfile)
	for row in reader:
		if row['correct'] == '+' and int(row['time']) < 7000:
			regular.add(row['suite'] + '/' + row['group'] + '/' + row['name'])

sim = set()
with open('benchmarks_sim_7sec_24G.csv') as csvfile:
	reader = csv.DictReader(csvfile)
	for row in reader:
		if row['correct'] == '+':
			sim.add(row['suite'] + '/' + row['group'] + '/' + row['name'])

lst = sorted(name for name in regular.difference(sim))
for name in lst:
	print(name)