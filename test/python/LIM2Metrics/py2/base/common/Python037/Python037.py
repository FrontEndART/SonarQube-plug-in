def func(a):
	try:
		print 'before break'
		return a 
	except StandardError:
		print 'error'
	