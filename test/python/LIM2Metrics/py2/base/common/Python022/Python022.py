username = ""
while not username:
    username = raw_input("Username: ")

password = ""
while not password:
    password = raw_input("Password: ")

if username == "name1" and password == "pass1":
    print "Hi, Name1."
elif username == "name2" and password == "pass2":
    print "Hey, Name2."
else:
    print "Login failed.  You're not so exclusive.\n"