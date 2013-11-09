package com.soebes.maven.plugins.iterator;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * @author Karl-Heinz Marbaise <a href="mailto:kama@soebes.de">kama@soebes.de</a>
 */
@Mojo( name = "artifactor", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true )
public class ArtifactorMojo
    extends AbstractArtifactorMojo
{

    /**
     * The project currently being build.
     */
    @Component
    private MavenProject mavenProject;

    /**
     * The current Maven session.
     */
    @Component
    private MavenSession mavenSession;

    @Component
    protected ArtifactRepositoryFactory artifactRepositoryFactory;

    @Component( role = ArtifactRepositoryLayout.class )
    private Map<String, ArtifactRepositoryLayout> repositoryLayouts;

    @Component
    private ArtifactInstaller installer;

    @Component
    protected ArtifactFactory artifactFactory;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        ProjectDependencyGraph projectDependencyGraph = mavenSession.getProjectDependencyGraph();
        List<MavenProject> sortedProjects = projectDependencyGraph.getSortedProjects();

        int counter = sortedProjects.size();

        if ( artifactsHaveBeenGiven() )
        {
            counter = getArtifacts().size();

            if ( !allGivenArtifactsPartOfTheReactor( sortedProjects ) )
            {
                throw new MojoFailureException(
                                                "One or more of the given artifacts are not part of the reactor which is not supported!" );
            }
        }

        for ( MavenProject project : sortedProjects )
        {
            ArtifactorArtifact aa = new ArtifactorArtifact(project);

            getLog().debug(" Checking if " + aa.toString() + " exists in reactor.");
            if ( artifactsHaveBeenGiven() && !getArtifacts().contains( aa ) )
            {
                continue;
            }

            getLog().debug(" The artifact " + aa.toString() + " exists in reactor.");
            try
            {
                installProject( project, counter );
            }
            catch ( ArtifactInstallationException e )
            {
                getLog().error( "Failure during installation of an artifact.", e );
            }
            counter--;
        }
    }

    private boolean allGivenArtifactsPartOfTheReactor( List<MavenProject> sortedProjects )
    {
        boolean result = true;
        for ( ArtifactorArtifact artifact : getArtifacts() )
        {
            getLog().debug( "Checking artifact: " + artifact.toString() );
            if ( !artifactExistsInReactorProjects( sortedProjects, artifact ) )
            {
                result = false;
                getLog().error( "The given artifact " + getArtifactId( artifact ) + " does not reference an artifact in the reactor!" );
            }
        }
        return result;
    }

    private String getArtifactId( ArtifactorArtifact artifact )
    {
        if ( artifact.getGroupId() == null )
        {
            //@TODO: Think about this!
            return mavenProject.getGroupId() + ":" + artifact.getArtifactId();
        }
        else
        {
            return artifact.getGroupId() + ":" + artifact.getArtifactId();
        }
    }

    private String getProjectId( MavenProject project )
    {
        if ( project.getGroupId() == null )
        {
            return project.getArtifactId();
        }
        else
        {
            return project.getGroupId() + ":" + project.getArtifactId();
        }
    }

    private boolean artifactExistsInReactorProjects( List<MavenProject> sortedProjects, ArtifactorArtifact artifact )
    {
        boolean result = false;
        for ( MavenProject project : sortedProjects )
        {
            if ( getProjectId( project ).equals( getArtifactId( artifact ) ) )
            {
                result = true;
            }
        }
        return result;
    }

    private boolean artifactsHaveBeenGiven()
    {
        return getArtifacts() != null;
    }

    private void installProject( MavenProject project, int location )
        throws ArtifactInstallationException
    {

        File pomFile = project.getFile();

        boolean isPomArtifact = "pom".equalsIgnoreCase( project.getPackaging() );

        //@TODO Think about attached artifacts
//        List<Artifact> attachedArtifacts = project.getAttachedArtifacts();
//
//        for ( Artifact artifact : attachedArtifacts )
//        {
//            getLog().info( " *** Attached:" + artifact.getId() );
//        }

        // @TODO: Think about attached artifacts.
        Artifact artifact = project.getArtifact();

        ArtifactRepository targetRepository =
            artifactRepositoryFactory.createDeploymentArtifactRepository( "local", getFolder().toURI().toString() + "/"
                + Integer.valueOf( location ), repositoryLayouts.get( "flat" ), false /* uniqueVersion */);

        if ( isPomArtifact )
        {
            getLog().debug( "Installation as pom file." );
            installer.install( pomFile, artifact, targetRepository );
        }
        else
        {
            File file = artifact.getFile();

            if ( file != null && file.isFile() )
            {
                getLog().debug( "Installation as file." );
                installer.install( file, artifact, targetRepository );
            }
            else
            {
                getLog().debug( "Installation as project file." );
                installer.install( project.getFile(), artifact, targetRepository );
            }

            Artifact pomArtifact =
                artifactFactory.createProjectArtifact( artifact.getGroupId(), artifact.getArtifactId(),
                                                       artifact.getBaseVersion() );
            installer.install( project.getFile(), pomArtifact, targetRepository );
        }
    }

    public MavenProject getMavenProject()
    {
        return mavenProject;
    }

    public void setMavenProject( MavenProject mavenProject )
    {
        this.mavenProject = mavenProject;
    }

    public MavenSession getMavenSession()
    {
        return mavenSession;
    }

    public void setMavenSession( MavenSession mavenSession )
    {
        this.mavenSession = mavenSession;
    }

}
