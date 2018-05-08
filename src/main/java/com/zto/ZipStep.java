package com.zto;

import hudson.Extension;
import hudson.model.Descriptor;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * @author liming 2018/4/26
 *
 */
public class ZipStep extends AbstractStepImpl {
    private final String source;
    private String excludes;

    @DataBoundConstructor
    public ZipStep(String source) throws Descriptor.FormException {
        if (StringUtils.isBlank(source)) {
            throw new Descriptor.FormException("不能为空", "source");
        }
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public String getExcludes() {
        return excludes;
    }

    @DataBoundSetter
    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(ZipExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "zipFile";
        }

        @Override
        public String getDisplayName() {
            return "将dotnet的构建输出打包存档";
        }
    }
}
