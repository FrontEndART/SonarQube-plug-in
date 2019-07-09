n=-1;
while (n<0):
	try:
		print 'before'
		n=n+1
		continue
		print 'error'
	except StandardError:
		print 'error'