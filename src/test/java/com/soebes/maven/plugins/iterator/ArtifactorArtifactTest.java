package com.soebes.maven.plugins.iterator;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

public class ArtifactorArtifactTest
{

    @Test
    public void firstTest() {
        List<ArtifactorArtifact> list = new ArrayList<ArtifactorArtifact> ();
        ArtifactorArtifact aa = new ArtifactorArtifact();
        aa.setArtifactId( "module-one" );
        list.add( aa );
        
        assertThat( list.contains( new ArtifactorArtifact().setArtifactId( "module-one" )) ).isTrue();
    }
}
