Artifactor Maven Plugin
=======================

Overview
--------

 The idea of the plugin is based on a customer contact which has really special
 requirements. 
 
 The plugin will analyze the reactor build order and will create a folder
 structure which contains one or more artifacts from within the reactor
 in the correct order of their dependencies. Currently
 the folder are simply numbers like 1, 2, 3 etc. which means
 you can install the artifacts exactly in the order 1, 2, 3. 
 
 
 Plugin Configuration in the build 

	 <build>
	    <plugins>
	      <plugin>
	        <groupId>com.soebes.maven.plugins</groupId>
	        <artifactId>artifactor-maven-plugin</artifactId>
	        <configuration>
	          <artifacts>
	            <artifact>
	              <groupId>${project.groupId}</groupId>
	              <artifactId>core</artifactId>
	            </artifact>
	          </artifacts>
	        </configuration>
	      </plugin>
	      ..
	    </plugins>
	    ...
	 </build>


 This will extract the above artifacts from the current build reactor 
 based on their order in the reactor and will copy them to a particular 
 location (with appropriate folder naming).
 
 The resulting structure will look like the following:
 
    dist
     +-- pom.xml
     +-- target
           +-- nexus
                 +--- 1
                      +--- module-one-1.0-SNAPSHOT.jar
                      +--- module-one-1.0-SNAPSHOT.pom


Homepage
--------
[http://khmarbaise.github.com/artifactor-maven-plugin/](http://khmarbaise.github.com/artifactor-maven-plugin/)

- - -

__BY USING THIS PLUGIN YOU ACKNOWLEDGE THAT YOU ARE A BAD CITIZEN OF THE MAVEN ECOSYSTEM.__

- - -

License
-------
[Apache License, Version 2.0, January 2004](http://www.apache.org/licenses/)


Status
------

 * Experimental
 * Not in Central

TODOs
-----

 * Use formatted numbers with three digest least to guarantee ordering with more than ten artifacts.  
 * remove the metadata from the generated folder structure.
 * Publish to Central.

see homepage.
