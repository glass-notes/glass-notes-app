package io.p13i.glassnotes.utilities;

import org.junit.Test;

public class FileIOTest {

    @Test
    public void testBasename() {
        junit.framework.Assert.assertEquals("d", FileIO.basename("/a/b/c/d"));
    }

}
