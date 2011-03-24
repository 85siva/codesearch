/**
 * Copyright 2010 David Froehlich   <david.froehlich@businesssoftware.at>,
 *                Samuel Kogler     <samuel.kogler@gmail.com>,
 *                Stephan Stiboller <stistc06@htlkaindorf.at>
 *
 * This file is part of Codesearch.
 *
 * Codesearch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Codesearch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Codesearch.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.codesearch.commons.plugins.vcs;

import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author David Froehlich
 */
public class SubversionPluginTest {

    SubversionPlugin sp = new SubversionPlugin();

     /* Logger */
    private static final Logger LOG = Logger.getLogger(SubversionPluginTest.class);

    public SubversionPluginTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
       
        
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getPathsForChangedFilesSinceRevision method, of class SubversionPlugin.
     */
    @Test
    public void testGetPathsForChangedFilesSinceRevision() throws Exception {
        LOG.info("getPathsForChangedFilesSinceRevision");
        String revision = "217";
   //     sp.setRepository(new URI("svn://portal.htl-kaindorf.at/svnsearch"), "feldruebe", "dota!123");
        Set result = sp.getChangedFilesSinceRevision(revision);
        Iterator iter = result.iterator();
        FileDto file = (FileDto) iter.next();
        assert (file.getFilePath().endsWith("testfile.txt"));
    }

    /**
     * Test of getFileContentForFilePath method, of class SubversionPlugin.
     */
    @Test
    public void testGetFileContentForFilePath() throws Exception {
        LOG.info("getFileContentForFilePath");
        String filePath = "/svnsearch/trunk/src/main/java/com/bearingpoint/ta/svnsearch/testfile.txt";
        String result = new String(sp.getFileForFilePath(filePath).getContent());
        assert (result.equals("test"));
    }

    @Test
    public void testGetFilesInDirectory() throws Exception {
        for(String s : sp.getFilesInDirectory(null)){
            LOG.info(s+"\n");
        }
    }
}
