import java.awt.Checkbox;
import java.io.*
import java.util.*

import org.apache.maven.model.DistributionManagement;


t = new IntegrationBase()


def getProjectVersion() {
	def pom = new XmlSlurper().parse(new File(basedir, 'pom.xml'))
   
	  def allPlugins = pom.version;
   
	  return pom.version;
}
   
def projectVersion = getProjectVersion();
   
println "Project version: ${projectVersion}"
   

def buildLogFile = new File( basedir, "build.log");

def distTargetNexus = new File (basedir, "dist/target/nexus");

def files = [ 
    "1/module-one-" + projectVersion + ".pom",
    "1/module-one-" + projectVersion + ".jar",
    "2/module-four-" + projectVersion + ".pom",
    "2/module-four-" + projectVersion + ".jar",
]

files.each {  
   file -> check = new File(distTargetNexus, file)
   println "checking ${check}"
   if (!check.exists()) {
       throw new FileNotFoundException( "Couldn't find " + file );
   }
}

def folders_which_should_not_exist = [
    "3",
    "4",
    "5",
    "6",
]

folders_which_should_not_exist.each {
    file -> check = new File(distTargetNexus, file)
    println "checking ${check}"
    if (check.exists()) {
        throw new IllegalStateException( "The folder " + file + " exists.");
    }
}

return true;
