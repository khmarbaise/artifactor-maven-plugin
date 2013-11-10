Artifactor Maven Plugin
=======================

[![Build Status](https://buildhive.cloudbees.com/job/khmarbaise/job/artifactor-maven-plugin/badge/icon)](https://buildhive.cloudbees.com/job/khmarbaise/job/artifactor-maven-plugin/)
Overview
--------

 The idea of the plugin is based on a customer contact which has really special
 requirements. 
 
 The plugin will analyze the reactor build order and will create a folder
 structure which contains one or more artifacts from within the reactor
 in the correct order of their dependencies. Currently
 the folder are simply numbers like 1, 2, 3 etc. which means
 you can install the artifacts exactly in the given order 1, 2, 3. 
 
 
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

 If you have more than one module within your multi-module build
 which should be installed in the given repo you have to define the
 artifacts in your plugin configuration like this:

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
	            <artifact>
	              <groupId>${project.groupId}</groupId>
	              <artifactId>client</artifactId>
	            </artifact>
	            <artifact>
	              <groupId>${project.groupId}</groupId>
	              <artifactId>ear</artifactId>
	            </artifact>
	          </artifacts>
	        </configuration>
	      </plugin>
	      ..
	    </plugins>
	    ...
	 </build>

 This will create the following structure in your dist module which 
 is responsible for creating the appropriate structure.

    dist
     +-- pom.xml
     +-- target
           +-- nexus
                 +--- 1
                      +--- module-core-1.0-SNAPSHOT.jar
                      +--- module-core-1.0-SNAPSHOT.pom
                 +--- 2
                      +--- module-client-1.0-SNAPSHOT.jar
                      +--- module-client-1.0-SNAPSHOT.pom
                 +--- 3
                      +--- module-ear-1.0-SNAPSHOT.jar
                      +--- module-ear-1.0-SNAPSHOT.pom


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
