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

        int counter = 1;

        if ( artifactsHaveBeenGiven() )
        {
            if ( !allGivenArtifactsPartOfTheReactor( sortedProjects ) )
            {
                throw new MojoFailureException(
                                                "One or more of the given artifacts are not part of the reactor which is not supported!" );
            }
        }

        for ( MavenProject project : sortedProjects )
        {
            ArtifactorArtifact aa = new ArtifactorArtifact( project );

            getLog().debug( " Checking if " + aa.toString() + " exists in reactor." );
            if ( artifactsHaveBeenGiven() && !getArtifacts().contains( aa ) )
            {
                continue;
            }

            getLog().debug( " The artifact " + aa.toString() + " exists in reactor." );
            try
            {
                installProject( project, counter );
            }
            catch ( ArtifactInstallationException e )
            {
                getLog().error( "Failure during installation of an artifact.", e );
            }
            counter++;
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
                getLog().error( "The given artifact " + getArtifactId( artifact )
                                    + " does not reference an artifact in the reactor!" );
            }
        }
        return result;
    }

    private String getArtifactId( ArtifactorArtifact artifact )
    {
        if ( artifact.getGroupId() == null )
        {
            // @TODO: Think about this!
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

    @SuppressWarnings( "deprecation" )
    private Artifact createPomArtifact( MavenProject project )
    {
        return artifactFactory.createProjectArtifact( project.getGroupId(), project.getArtifactId(),
                                                      project.getVersion() );
    }

    private void installPom( MavenProject project, ArtifactRepository testRepository)
        throws MojoExecutionException
    {
        try
        {
            Artifact pomArtifact = null;
            if ( "pom".equals( project.getPackaging() ) )
            {
                pomArtifact = project.getArtifact();
            }
            if ( pomArtifact == null )
            {
                pomArtifact = createPomArtifact( project );
            }
            installArtifact( project.getFile(), pomArtifact, testRepository );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to install POM: " + project, e );
        }
    }

    private void installArtifact( File file, Artifact artifact, ArtifactRepository testRepository )
        throws MojoExecutionException
    {
        try
        {
            if ( file == null )
            {
                throw new IllegalStateException( "Artifact has no associated file: " + artifact.getId() );
            }
            if ( !file.isFile() )
            {
                throw new IllegalStateException( "Artifact is not fully assembled: " + file );
            }

            installer.install( file, artifact, testRepository );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to install artifact: " + artifact, e );
        }
    }

    private void installProject( MavenProject project, int counter )
        throws ArtifactInstallationException, MojoExecutionException
    {
        ArtifactRepository targetRepository = createTargetRepository( counter );

        installPom( project, targetRepository);

        Artifact mainArtifact = project.getArtifact();
        if ( mainArtifact.getFile() != null )
        {
            installArtifact( mainArtifact.getFile(), mainArtifact, targetRepository );
        }

        List<Artifact> attachedArtifacts = project.getAttachedArtifacts();
        for ( Artifact attachedArtifact : attachedArtifacts )
        {
            installArtifact( attachedArtifact.getFile(), attachedArtifact, targetRepository );
        }
    }

    private ArtifactRepository createTargetRepository( int location )
    {
        ArtifactRepository targetRepository =
            artifactRepositoryFactory.createDeploymentArtifactRepository( "local", getFolder().toURI().toString() + "/"
                + Integer.valueOf( location ), repositoryLayouts.get( "flat" ), false /* uniqueVersion */);
        return targetRepository;
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
