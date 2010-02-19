package com.izforge.izpack.compiler;

import com.izforge.izpack.AssertionHelper;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.Mergeable;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.internal.verification.AtLeast;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an Izpack compilation
 */
public class CompilationTest {

    private File baseDir = new File(getClass().getClassLoader().getResource("samples").getFile());
    private File installerFile = new File(getClass().getClassLoader().getResource("samples/helloAndFinish.xml").getFile());
    private File out = new File(baseDir, "out.jar");

    private CompilerContainer compilerContainer;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private CompilerData data;

    @Before
    public void cleanFiles() {
        assertThat(baseDir.exists(), Is.is(true));
        out.delete();
        data = new CompilerData(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(), out.getAbsolutePath());
        compilerContainer = new CompilerContainer();
        compilerContainer.initBindings();
        compilerContainer.addComponent(CompilerData.class, data);
    }

    @Test
    public void installerShouldContainInstallerClass() throws Exception {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        AssertionHelper.assertZipContainsMatch(out, StringContains.containsString("Installer.class"));
        AssertionHelper.assertZipContainsMatch(out, StringContains.containsString("HelloPanel.class"));
    }

    @Test
    public void mergeManagerShouldGetTheMergeableFromPanel() throws Exception {
        MergeManager mergeManager = new MergeManager();
        ZipOutputStream outputStream = Mockito.mock(ZipOutputStream.class);
        Mergeable mergeable = mergeManager.getMergeableFromPanelClass("HelloPanel");
        assertThat(mergeable, IsNull.<Object>notNullValue());
        mergeable.merge(outputStream);
        Mockito.verify(outputStream, new AtLeast(2)).putNextEntry(Mockito.<ZipEntry>any());
    }

    @Test
    public void mergeManagerShouldTransformClassNameToPackagePath() throws Exception {
        MergeManager mergeManager = new MergeManager();
        String pathFromClassName = mergeManager.getPackagePathFromClassName("com.test.sora.UneClasse");
        assertThat(pathFromClassName, Is.is("com/test/sora"));
    }

    @Test
    public void mergeManagerShouldReturnDefaultPackagePath() throws Exception {
        MergeManager mergeManager = new MergeManager();
        String pathFromClassName = mergeManager.getPackagePathFromClassName("UneClasse");
        assertThat(pathFromClassName, Is.is("com/izforge/izpack/panels"));
    }

    @Test
    public void installerShouldContainResources() throws Exception {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        AssertionHelper.assertZipContainsMatch(out, StringContains.containsString("resources/vars"));
    }

    @Test
    public void installerShouldContainImages() throws Exception {
        CompilerConfig c = compilerContainer.getComponent(CompilerConfig.class);
        c.executeCompiler();
        AssertionHelper.assertZipContainsMatch(out, Is.is("img/JFrameIcon.png"));
    }


}