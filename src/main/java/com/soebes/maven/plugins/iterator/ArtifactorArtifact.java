package com.soebes.maven.plugins.iterator;

import org.apache.maven.project.MavenProject;

public class ArtifactorArtifact
{
    private String groupId;

    private String artifactId;

    public ArtifactorArtifact()
    {
    }

    public ArtifactorArtifact( MavenProject project )
    {
        this.groupId = project.getGroupId();
        this.artifactId = project.getArtifactId();
    }

    public String getGroupId()
    {
        return groupId;
    }

    public ArtifactorArtifact setGroupId( String groupId )
    {
        this.groupId = groupId;
        return this;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public ArtifactorArtifact setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
        return this;
    }

    public String toString()
    {
        return groupId == null ? "" : ( groupId + ":" ) + artifactId;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( artifactId == null ) ? 0 : artifactId.hashCode() );
        result = prime * result + ( ( groupId == null ) ? 0 : groupId.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ArtifactorArtifact other = (ArtifactorArtifact) obj;
        if ( artifactId == null )
        {
            if ( other.artifactId != null )
                return false;
        }
        else if ( !artifactId.equals( other.artifactId ) )
            return false;
        if ( groupId == null )
        {
            if ( other.groupId != null )
                return false;
        }
        else if ( !groupId.equals( other.groupId ) )
            return false;
        return true;
    }

}
