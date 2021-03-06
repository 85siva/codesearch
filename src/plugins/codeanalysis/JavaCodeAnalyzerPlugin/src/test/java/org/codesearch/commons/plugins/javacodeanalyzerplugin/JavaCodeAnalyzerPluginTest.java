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
package org.codesearch.commons.plugins.javacodeanalyzerplugin;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.codesearch.commons.plugins.codeanalyzing.CodeAnalyzerPluginException;
import org.codesearch.commons.plugins.codeanalyzing.ast.AstNode;
import org.codesearch.commons.plugins.codeanalyzing.ast.ExternalUsage;
import org.codesearch.commons.plugins.codeanalyzing.ast.Usage;
import org.codesearch.commons.plugins.codeanalyzing.ast.Visibility;
import org.codesearch.commons.plugins.javacodeanalyzerplugin.ast.ClassNode;
import org.codesearch.commons.plugins.javacodeanalyzerplugin.ast.FileNode;
import org.codesearch.commons.plugins.javacodeanalyzerplugin.ast.MethodNode;
import org.junit.Test;

/**
 *
 * @author david
 */
public class JavaCodeAnalyzerPluginTest {

    private JavaCodeAnalyzerPlugin instance;

    public JavaCodeAnalyzerPluginTest() {
        this.instance = new JavaCodeAnalyzerPlugin();
    }
    
    private String getResourceFileContent(String filename) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(
                filename)));
        String fileContent = "";
        String newLine;
        while ((newLine = reader.readLine()) != null) {
            fileContent += newLine + "\n";
        }
        return fileContent;
    }

    //all analysis tests
    @Test
    public void testBasicAnalysis() throws CodeAnalyzerPluginException {

        //create mock source code
        String content = "";
        content += "package foopackage;\n";
        content += "\n";
        content += "public class Foo {\n";
        content += "  public Foo(){\n";
        content += "  }\n";
        content += "}\n";

        //create expected result
        FileNode expResult = new FileNode();

        ClassNode classNode = new ClassNode();
        classNode.setStartLine(3);
        classNode.setStartPositionInLine(1);
        classNode.setName("Foo");
        classNode.setVisibility(Visibility.PUBLIC);

        MethodNode constructor = new MethodNode();
        constructor.setStartLine(4);
        constructor.setConstructor(true);
        constructor.setName("Foo");
        constructor.setVisibility(Visibility.PUBLIC);
        constructor.setStartPositionInLine(3);
        classNode.getMethods().add(constructor);
        expResult.getClasses().add(classNode);
        instance.analyzeFile(content);
        FileNode result = (FileNode) instance.getAst();
        assertEquals(expResult, result);
    }

    @Test
    public void testMultiLineMethodCall() throws CodeAnalyzerPluginException, IOException {
        String fileContent = this.getResourceFileContent("MultiLineMethodCall.java");
        instance.analyzeFile(fileContent);
        List<Usage> result = instance.getUsages();
        List<Usage> expResult = new LinkedList<Usage>();
        expResult.add(new Usage(9, 11, 3, 9, "foo"));
        assert (result.size() == 1);
        assert (expResult.get(0).equals(result.get(0)));
    }
    
    @Test
    public void testFullyQualifiedName() throws IOException, CodeAnalyzerPluginException {
        String fileContent = this.getResourceFileContent("FullyQualifiedName.java");
        instance.analyzeFile(fileContent);
        AstNode ast = instance.getAst();
        List<Usage> usages = instance.getUsages();
        assert(usages.size() == 1);
        assert(usages.get(0).getStartColumn() == 11);
    }
    
    @Test
    public void testInnerClass() throws IOException, CodeAnalyzerPluginException {
        String fileContent = this.getResourceFileContent("InternalClass.java");
        instance.analyzeFile(fileContent);
        AstNode ast = instance.getAst();
        List<Usage> result = instance.getUsages();
        List<Usage> expected = new LinkedList<Usage>();
        expected.add(new ExternalUsage(37, 28, 10, "InnerClass", "InnerClass"));
        expected.add(new ExternalUsage(9, 28, 10, "InnerClass", "InnerClass"));
        assert(result.equals(expected));
    }
}
