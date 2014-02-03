package com.soebes.maven.plugins.artifactor;

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
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

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
    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject mavenProject;

    /**
     * The current Maven session.
     */
    @Parameter( defaultValue = "${session}", readonly = true )
    private MavenSession mavenSession;

    @Component
    protected ArtifactRepositoryFactory artifactRepositoryFactory;

    @Component( role = ArtifactRepositoryLayout.class )
    private Map<String, ArtifactRepositoryLayout> repositoryLayouts;

    @Component
    private ArtifactInstaller installer;

    @Parameter( defaultValue = "Das ist der Default" )
    private String finalName;

    @Component
    protected ArtifactFactory artifactFactory;

    // For Maven 3.3 we need to change the following (http://jira.codehaus.org/browse/MPLUGIN-257):

    @Parameter( defaultValue = "${plugin}", readonly = true, required = true )
    private PluginDescriptor pluginDescriptor;

    @Parameter( defaultValue = "${mojoExecution}", readonly = true, required = true )
    private MojoExecution mojoExecution;

    private PluginExecution getPluginExecution( String executionId )
    {
        PluginExecution execution = null;
        List<PluginExecution> executions = pluginDescriptor.getPlugin().getExecutions();
        for ( PluginExecution pluginExecution : executions )
        {
            if ( executionId.equals( pluginExecution.getId() ) )
            {
                execution = pluginExecution;
            }
        }
        return execution;
    }

    private Xpp3Dom getFinalNameChild( String executionId )
    {
        Xpp3Dom result = null;
        PluginExecution execution = getPluginExecution( executionId );
        if ( execution != null )
        {
            Xpp3Dom configuration = (Xpp3Dom) execution.getConfiguration();
            if ( configuration != null )
            {
                result = configuration.getChild( "finalName" );
            }
        }
        return result;
    }

    private boolean hasFinalNameAValue( String executionId )
    {
        boolean result = false;

        Xpp3Dom child = getFinalNameChild( executionId );
        if ( child != null && child.getValue() != null )
        {
            result = true;
        }

        return result;
    }

    private boolean isFinalNameDefined( String executionId )
    {
        boolean result = false;

        Xpp3Dom child = getFinalNameChild( executionId );
        if ( child != null )
        {
            result = true;
        }

        return result;
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        String executionId = mojoExecution.getExecutionId();
        getLog().info( "mojoExecution id:" + executionId );

        if ( isFinalNameDefined( executionId ) )
        {
            getLog().info( "--------> finalName is defined." );
        }
        else
        {
            getLog().info( "--------> NOT DEFINED." );
        }
        
        if ( hasFinalNameAValue( executionId ) )
        {
            getLog().info( "--------> finalName is defined and has be given a value." );
        }

        ProjectDependencyGraph projectDependencyGraph = mavenSession.getProjectDependencyGraph();
        List<MavenProject> sortedProjects = projectDependencyGraph.getSortedProjects();

        if ( artifactsHaveBeenGiven() )
        {
            if ( !allGivenArtifactsPartOfTheReactor( sortedProjects ) )
            {
                throw new MojoFailureException(
                                                "One or more of the given artifacts are not part of the reactor which is not supported!" );
            }
        }

        int counter = 1;

        for ( MavenProject project : sortedProjects )
        {
            getLog().debug( " Checking if we should install the artifact " + project.getId() );

            if ( projectShouldNotBeingInstalled( project ) )
            {
                continue;
            }

            if ( buildOrderOfArtifactAfterPluginExecution( sortedProjects, project ) )
            {
                throw new MojoFailureException(
                                                "You have defined to use an artifact which is assembled later than the execution of this plugin." );
            }

            // @TODO: Add a check for downstream dependencies in case of a an
            // artifact which is used but one of its dependencies is not being
            // used. In that case the build has to fail!
            // printDeps( projectDependencyGraph, project );
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

    private boolean buildOrderOfArtifactAfterPluginExecution( List<MavenProject> sortedProjects, MavenProject project )
    {
        int pluginExecution = sortedProjects.indexOf( mavenProject );
        int orderOfArtifact = sortedProjects.indexOf( project );

        boolean result = false;
        if ( orderOfArtifact > pluginExecution )
        {
            result = true;
        }
        return result;
    }

    private boolean projectShouldNotBeingInstalled( MavenProject project )
    {
        return artifactsHaveBeenGiven() && !getArtifacts().contains( new ArtifactorArtifact( project ) );
    }

    private void printDeps( ProjectDependencyGraph projectDependencyGraph, MavenProject project )
    {
        List<MavenProject> upstreamProjects = projectDependencyGraph.getUpstreamProjects( project, true );
        getLog().debug( " -> Upstream dependencies: " + project.getId() );
        for ( MavenProject mavenProject : upstreamProjects )
        {
            getLog().debug( "-> DEP: " + mavenProject.getId() );
        }
        List<MavenProject> downstreamProjects = projectDependencyGraph.getDownstreamProjects( project, true );
        getLog().debug( " -> Downstream dependencies:" + project.getId() );
        for ( MavenProject mavenProject : downstreamProjects )
        {
            getLog().debug( "-> DEP: " + mavenProject.getId() );
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

    private void installPom( MavenProject project, ArtifactRepository testRepository )
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

        installPom( project, targetRepository );

        Artifact mainArtifact = project.getArtifact();
        if ( mainArtifact.getFile() != null )
        {
            installArtifact( mainArtifact.getFile(), mainArtifact, targetRepository );
        }

        for ( Artifact attachedArtifact : project.getAttachedArtifacts() )
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
