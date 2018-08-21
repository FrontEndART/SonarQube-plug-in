for n in range(1, 3):
	try:
		print 'before'
		continue
		print 'error'
	except StandardError:
		print 'error'