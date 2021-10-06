#!/usr/bin/env python3
import sys

def validate(expr) -> str:
	try:
		if expr == 'true':
			return 'Did you mean \'True\'?'
		if expr == 'false':
			return 'Did you mean \'False\'?'
		x = eval(expr)
		typ = type(x)
		if typ == str or typ == int or typ == bool:
			return
		if typ == list:
			if len(x) == 0:
				return
			elmtType = type(x[0])
			if not (elmtType == int or elmtType == str or elmtType == bool):
				return 'Only int, string and boolean lists are supported.'
			for e in x[1:]:
				if type(e) != elmtType:
					return 'All elements of a list should have the same type'
		elif typ == dict:
			if len(x) == 0:
				return
			keyType = type(next(iter(x)))
			valType = type(next(iter(x.values())))
			if not (keyType == int or keyType == str):
				return 'Only int and string keys are supported.'
			if not (valType == int or valType == str):
				return 'Only int and string values are supported.'
			for k in x:
				if type(k) != keyType:
					return 'All keys of a dict should have the same type'
				if type(x[k]) != valType:
					return 'All values of a dict should have the same type'
		elif typ == set:
			if len(x) == 0:
				return
			elmtType = type(next(iter(x)))
			if not (elmtType == int or elmtType == str):
				return 'Only int and string sets are supported.'
			for k in x:
				if type(k) != elmtType:
					return 'All elements of a set should have the same type'
		else:
			return 'Type \'%s\' not supported' % str(typ.__name__)
	except Exception as e:
		return str(e)

def main(action, arg):
	# We do many things!
	if action == 'validate':
		msg = validate(arg)
		if msg:
			print(msg)
	else:
		print('Action not recognized: %s' % action)

if __name__ == '__main__':
	main(sys.argv[1], sys.argv[2])
