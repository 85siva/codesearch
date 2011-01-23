/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codesearch.commons.plugins.javacodeanalyzerplugin.ast;

import java.util.List;
import org.codesearch.commons.database.DBAccess;
import org.codesearch.commons.database.DatabaseAccessException;
import org.codesearch.commons.plugins.codeanalyzing.ast.ExternalUsage;
import org.codesearch.commons.plugins.javacodeanalyzerplugin.JavaCodeAnalyzerPlugin;

/**
 *
 * @author David Froehlich
 */
public class ExternalClassUsage extends ExternalUsage {

    /** {@inheritDoc } */
    public ExternalClassUsage(int startPositionInLine, int startLine, int length, String replacedString, String targetClassName) {
        super(startPositionInLine, startLine, length, replacedString, targetClassName);
    }

    /** {@inheritDoc} */
    @Override
    public void resolveLink(String originFilePath, String repository) throws DatabaseAccessException{
        JavaCodeAnalyzerPlugin plugin = new JavaCodeAnalyzerPlugin();
        List<String> imports = DBAccess.getImportsForFile(originFilePath, repository);
        plugin.parseLineNumberAndFileNameOfUsage(this, repository, imports, originFilePath);
    }
}
