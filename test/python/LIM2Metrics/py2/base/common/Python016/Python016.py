import commands

lines = commands.getoutput('mount -v').split('\n')
 
points = map(lambda line: line.split()[2], lines)
print points