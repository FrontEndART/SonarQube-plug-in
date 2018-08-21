import subprocess

lines = subprocess.getoutput('mount -v').split('\n')
 
points = [line.split()[2] for line in lines]
print(points)