import java.awt.Checkbox;
import java.io.*
import java.util.*

import org.apache.maven.model.DistributionManagement;


t = new IntegrationBase()


def getPluginVersion() {
    def pom = new XmlSlurper().parse(new File(basedir, 'dist/pom.xml'))
   
      def allPlugins = pom.build.plugins.plugin;
   
      def configurationMavenPlugin = allPlugins.find {
          item -> item.groupId.equals("com.soebes.maven.plugins") && item.artifactId.equals("artifactor-maven-plugin");
      }
      
      return configurationMavenPlugin.version;
}

def getProjectVersion() {
    def pom = new XmlSlurper().parse(new File(basedir, 'pom.xml'))
   
      def allPlugins = pom.version;
   
      return pom.version;
}
   
def projectVersion = getProjectVersion();
def pluginVersion = getPluginVersion();

println "Project version: ${projectVersion}"
println "Plugin version ${pluginVersion}"
   

def buildLogFile = new File( basedir, "build.log");

t.checkExistenceAndContentOfAFile(buildLogFile, [
    '[ERROR] Failed to execute goal com.soebes.maven.plugins:artifactor-maven-plugin:' + pluginVersion + ':artifactor (HIER) on project dist: You have defined to use an artifact which is assembled later than the execution of this plugin. -> [Help 1]',
    '[INFO] BUILD FAILURE',
  ])
  
return true;
