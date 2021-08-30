ANALYZERS = sourcemeter-analyzer-cpp sourcemeter-analyzer-csharp sourcemeter-analyzer-java sourcemeter-analyzer-javascript sourcemeter-analyzer-python sourcemeter-analyzer-rpg
PACKAGENAME = sourcemeter-10.0-plugins-for-sonarqube-8.9.2

all: sonarqube-plugin-package

install-dependencies:
	mvn install:install-file -DgroupId=com.frontendart.columbus -DartifactId=graphsupportlib -Dversion=1.0 -Dpackaging=jar -Dfile=lib/graphsupportlib-1.0.jar
	mvn install:install-file -DgroupId=com.frontendart.columbus -DartifactId=graphlib -Dversion=1.0 -Dpackaging=jar -Dfile=lib/graphlib-1.0.jar

sourcemeter-analyzer-base: install-dependencies sonarqube-core-plugin
	mvn -f src/sonarqube-analyzers/$@/pom.xml clean install

sonarqube-core-plugin:
	mvn -f src/sonarqube-core-plugin/pom.xml clean install

sonarqube-gui-plugin: install-dependencies sourcemeter-analyzer-base usersguide
	@cp doc/UG.html src/sonarqube-gui-plugin/src/main/resources/static/help/usersguide.html
	mvn -f src/sonarqube-gui-plugin/pom.xml clean install

$(ANALYZERS): install-dependencies sonarqube-core-plugin sourcemeter-analyzer-base
	mvn -f src/sonarqube-analyzers/$@/pom.xml clean install

usersguide:
	@cd doc/usersguide && python3 generatedoc.py -css style/SourceMeter.css -html
	@cp doc/usersguide/results/UG.html doc/UG.html

sonarqube-plugin-package: usersguide sonarqube-core-plugin sonarqube-gui-plugin $(ANALYZERS)
	mkdir $(PACKAGENAME)
	mkdir $(PACKAGENAME)/doc
	mkdir $(PACKAGENAME)/plugins
	@cp src/sonarqube-core-plugin/target/*.jar $(PACKAGENAME)/plugins
	@cp src/sonarqube-gui-plugin/target/*.jar $(PACKAGENAME)/plugins
	@cp src/sonarqube-analyzers/sourcemeter-analyzer-cpp/target/*.jar $(PACKAGENAME)/plugins
	@cp src/sonarqube-analyzers/sourcemeter-analyzer-csharp/target/*.jar $(PACKAGENAME)/plugins
	@cp src/sonarqube-analyzers/sourcemeter-analyzer-java/target/*.jar $(PACKAGENAME)/plugins
	@cp src/sonarqube-analyzers/sourcemeter-analyzer-javascript/target/*.jar $(PACKAGENAME)/plugins
	@cp src/sonarqube-analyzers/sourcemeter-analyzer-python/target/*.jar $(PACKAGENAME)/plugins
	@cp src/sonarqube-analyzers/sourcemeter-analyzer-rpg/target/*.jar $(PACKAGENAME)/plugins
	@cp doc/UG.html $(PACKAGENAME)/doc
	@cp README.md $(PACKAGENAME)
	tar -czf $(PACKAGENAME).tar.gz $(PACKAGENAME)
	rm -rf $(PACKAGENAME)

clean:
	rm -rf src/sonarqube-core-plugin/target
	rm -rf src/sonarqube-gui-plugin/target
	rm -rf src/sonarqube-analyzers/sourcemeter-analyzer-base/target
	rm -rf src/sonarqube-analyzers/sourcemeter-analyzer-cpp/target
	rm -rf src/sonarqube-analyzers/sourcemeter-analyzer-csharp/target
	rm -rf src/sonarqube-analyzers/sourcemeter-analyzer-java/target
	rm -rf src/sonarqube-analyzers/sourcemeter-analyzer-javascript/target
	rm -rf src/sonarqube-analyzers/sourcemeter-analyzer-python/target
	rm -rf src/sonarqube-analyzers/sourcemeter-analyzer-rpg/target
