/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codesearch.javacodeanalyzerplugin;

import org.codesearch.commons.plugins.codeanalysis.javacodeanalyzerplugin.JavaCodeAnalyzerPlugin;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.commons.lang.NotImplementedException;
import org.codesearch.commons.configuration.xml.dto.RepositoryDto;

/**
 *
 * @author David Froehlich
 */
public class JavaCodeAnalyzerPluginTest extends TestCase {
    private JavaCodeAnalyzerPlugin plugin = new JavaCodeAnalyzerPlugin();
    public JavaCodeAnalyzerPluginTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getAstForRepository method, of class JavaCodeAnalyzerPlugin.
     */
    public void testGetAstForRepository() throws Exception{
        throw new NotImplementedException();
    }
}