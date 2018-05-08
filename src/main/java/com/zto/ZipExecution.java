package com.zto;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.io.Archiver;
import hudson.util.io.ArchiverFactory;
import jenkins.MasterToSlaveFileCallable;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 * @author liming 2018/4/26
 *
 */
public class ZipExecution extends AbstractSynchronousNonBlockingStepExecution<String> {
    private static final long serialVersionUID = 1L;

    @StepContextParameter
    private transient TaskListener listener;

    @StepContextParameter
    private transient FilePath ws;

    @StepContextParameter
    private transient Run build;

    @StepContextParameter
    private transient Launcher launcher;

    @StepContextParameter
    private transient EnvVars envVars;

    @Inject
    private transient ZipStep step;

    @Override
    protected String run() throws Exception {
        FilePath source = ws;
        if (!StringUtils.isBlank(step.getSource())) {
            source = ws.child(step.getSource());
            if (!source.exists()) {
                throw new IOException(source.getRemote() + " 目录不存在");
            } else if (!source.isDirectory()) {
                throw new IOException(source.getRemote() + " 不是一个目录");
            }
        }
        String zipFileName = envVars.get("JOB_NAME") + ".zip";
        FilePath destination = ws.child(zipFileName);
        if (destination.exists()) {
            if (!destination.delete()) {
                throw new IOException(source.getRemote() + " 已经存在并且无法删除");
            }
        }
        if (StringUtils.isBlank(step.getExcludes())) {
            listener.getLogger().println("打包文件 " + source.getRemote() + " 到 " + destination.getRemote());
        } else {
            listener.getLogger().println("打包文件 " + source.getRemote()
                    + " 排除 [" + step.getExcludes() + "] 到 " + destination.getRemote());
        }
        int count = source.act(new ZipItFileCallable(destination, step.getExcludes()));
        listener.getLogger().println("共打包 " + count + " 个文件");
        return zipFileName;
    }

    static class ZipItFileCallable extends MasterToSlaveFileCallable<Integer> {
        final FilePath zipFile;
        final String excludes;

        public ZipItFileCallable(FilePath zipFile, String excludes) {
            this.zipFile = zipFile;
            this.excludes = excludes;
        }

        @Override
        public Integer invoke(File dir, VirtualChannel channel) throws IOException, InterruptedException {
            String canonicalZip = new File(zipFile.getRemote()).getCanonicalPath();

            Archiver archiver = ArchiverFactory.ZIP.create(zipFile.write());
            FileSet fs = Util.createFileSet(dir, "**/*",excludes);
            DirectoryScanner scanner = fs.getDirectoryScanner(new org.apache.tools.ant.Project());
            try {
                for (String path : scanner.getIncludedFiles()) {
                    File toArchive = new File(dir, path).getCanonicalFile();
                    if (!toArchive.getPath().equals(canonicalZip)) {
                        archiver.visit(toArchive, path);
                    }
                }
            } finally {
                archiver.close();
            }
            return archiver.countEntries();
        }
    }
}
