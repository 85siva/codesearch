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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codesearch.commons.plugins.vcs;

import org.codesearch.commons.configuration.dto.NoAuthentication;
import org.codesearch.commons.configuration.dto.RepositoryDto;

import java.net.URISyntaxException;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class tests the functionality of the BZRPlugin
 * @author Stephan Stiboller
 * @author David Froehlich
 *
 */
public class BazaarPluginTest {

    BazaarPlugin plugin = new BazaarPlugin();
    private String path = "lp:codesearch";
     /* Logger */
    private static final Logger LOG = Logger.getLogger(BazaarPluginTest.class);

    public BazaarPluginTest() {
    }

    @Before
    public void setUp() throws URISyntaxException, VersionControlPluginException {
       // plugin.setRepository(new RepositoryDto("code", path, "", "", false,"BZR", null, null, null));
    }

    /**
     * Test of the remote checkout
     */
    @Test
    public void testCheckout()
    {
        try {
            plugin.setRepository(new RepositoryDto("code", path, new NoAuthentication(), false, "BZR", null, null, null));

            System.out.println("LOL" + plugin.getRepositoryRevision());

        } catch (VersionControlPluginException ex) {
            java.util.logging.Logger.getLogger(BazaarPluginTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Test of getFileContentForFilePath method, of class BazaarPlugin.
     */
    //@Test
    public void testGetFileContentForFilePath() throws Exception {
//TODO
    }

    /**
     * Test of getChangedFilesSinceRevision method, of class BazaarPlugin.
     */
    //@Test
    public void testGetChangedFilesSinceRevision() throws Exception {
        LOG.info("getChangedFilesSinceRevision");
        String revision = "1";
        boolean expResult = false;
//        Set<FileDto> result = plugin.getChangedFilesSinceRevision(revision);
//        if(result != null)
//            expResult=true;
//        assertTrue(expResult);
    }

}
