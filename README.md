Artifactor Maven Plugin
=======================

Overview
--------

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
   based on their order in the reactory
   and will copy them to a particular location (with appropriate folder
   naming).


- - -

__BY USING THIS PLUGIN YOU ACKNOWLEDGE THAT YOU ARE A BAD CITIZEN OF THE MAVEN ECOSYSTEM.__

- - -


see homepage.
