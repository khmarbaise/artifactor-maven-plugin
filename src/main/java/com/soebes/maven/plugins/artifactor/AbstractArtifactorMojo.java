package com.soebes.maven.plugins.artifactor;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractArtifactorMojo
    extends AbstractMojo
{

    /**
     * Here you can define the artifacts you would like to be stored into the nexus folder.
     * 
     * <pre>
     * {@code 
     *   <artifacts>
     *     <artifact>
     *       <groupId>xyz</groupId>
     *       <artifactId>core</artifactId>
     *     </artifact>
     *     <artifact>
     *       <groupId>xyz</groupId>
     *       <artifactId>core</artifactId>
     *     </artifact>
     *     <artifact>
     *       <artifactId>ear</artifactId>
     *     </artifact>
     *   </artifacts>}
     * </pre>
     */
    @Parameter
    private List<ArtifactorArtifact> artifacts;

    /**
     * The folder where the artifacts will be stored.
     */
    @Parameter( defaultValue = "${project.build.directory}/nexus" )
    private File folder;

    public List<ArtifactorArtifact> getArtifacts()
    {
        return artifacts;
    }

    public void setArtifacts( List<ArtifactorArtifact> artifacts )
    {
        this.artifacts = artifacts;
    }

    public File getFolder()
    {
        return folder;
    }

    public void setFolder( File folder )
    {
        this.folder = folder;
    }

}
