try:
	raise NameError('HiThere')
except NameError:
	print('An exception flew by!')
	raise
