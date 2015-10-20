ANALYZERS = sourcemeter-analyzer-base sourcemeter-analyzer-cpp sourcemeter-analyzer-java sourcemeter-analyzer-python sourcemeter-analyzer-rpg
PACKAGENAME = sourcemeter-sonarqube-plugins-package-7.0.5

all: sonarqube-plugin-package

install-dependencies:
	mvn install:install-file -DgroupId=com.frontendart.columbus -DartifactId=graphsupportlib -Dversion=1.0 -Dpackaging=jar -Dfile=lib/graphsupportlib-1.0.jar
	mvn install:install-file -DgroupId=com.frontendart.columbus -DartifactId=graphlib -Dversion=1.0 -Dpackaging=jar -Dfile=lib/graphlib-1.0.jar

sonarqube-core-plugin: install-dependencies
	mvn -f src/sonarqube-core-plugin/pom.xml clean install

sonarqube-gui-plugin: install-dependencies
	mvn -f src/sonarqube-gui-plugin/pom.xml clean install

$(ANALYZERS): install-dependencies sonarqube-core-plugin
	mvn -f src/sonarqube-analyzers/$@/pom.xml clean install

sonarqube-plugin-package: sonarqube-core-plugin sonarqube-gui-plugin $(ANALYZERS)
	mkdir $(PACKAGENAME)
	mkdir $(PACKAGENAME)/doc
	mkdir $(PACKAGENAME)/plugins
	@cp src/sonarqube-core-plugin/target/*.jar $(PACKAGENAME)/plugins
	@cp src/sonarqube-gui-plugin/target/*.jar $(PACKAGENAME)/plugins
	@cp src/sonarqube-analyzers/sourcemeter-analyzer-cpp/target/*.jar $(PACKAGENAME)/plugins
	@cp src/sonarqube-analyzers/sourcemeter-analyzer-java/target/*.jar $(PACKAGENAME)/plugins
	@cp src/sonarqube-analyzers/sourcemeter-analyzer-python/target/*.jar $(PACKAGENAME)/plugins
	@cp src/sonarqube-analyzers/sourcemeter-analyzer-rpg/target/*.jar $(PACKAGENAME)/plugins
	@cp doc/UG.html $(PACKAGENAME)/doc
	@cp README.md $(PACKAGENAME)
	tar -czf $(PACKAGENAME).tar.gz $(PACKAGENAME)
	rm -rf $(PACKAGENAME)
