for n in range(1, 3):
	try:
		print 'before break'
		break 
	except StandardError:
		print 'error'