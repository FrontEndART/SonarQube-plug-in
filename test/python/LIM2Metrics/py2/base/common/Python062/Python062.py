
def generateWebsite( name, url = "www.java2s.com",Flash = "no", CGI = "yes" ):
   print "Generating site requested by", name, "using url", url

   if Flash == "yes":
      print "Flash is enabled"

   if CGI == "yes":
      print "CGI scripts are enabled\n"   
   print

generateWebsite( "java2s" )

generateWebsite( "java2s", Flash = "yes",
                  url = "www.java2s.com" )

generateWebsite( CGI = "no", name = "Prentice Hall" )