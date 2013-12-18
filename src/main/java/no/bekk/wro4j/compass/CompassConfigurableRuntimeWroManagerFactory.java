package no.bekk.wro4j.compass;

import org.apache.commons.io.IOUtils;
import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig;
import org.jruby.embed.ScriptingContainer;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.metadata.MetaDataFactory;
import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CompassConfigurableRuntimeWroManagerFactory extends ConfigurableWroManagerFactory {

    @Override
    protected MetaDataFactory newMetaDataFactory() {
        final MetaDataFactory parent = super.newMetaDataFactory();
        final Map<String, Object> parentProps = parent.create();
        Properties props = newConfigProperties();
        String compassBaseDir = props.getProperty("compassBaseDir");
        String gemHome = props.getProperty("gemHome", (compassBaseDir != null ? compassBaseDir + "./gems" : null));
        File projectBaseDir = computeProjectDir();

        final CompassSettings compassSettings = new CompassSettings();
        compassSettings.setProjectBaseDir(projectBaseDir);
        compassSettings.setGemHome(computePath(projectBaseDir, gemHome));
        compassSettings.setCompassBaseDir(computePath(projectBaseDir, compassBaseDir));
        compassSettings.setCompiler(createCompiler(compassSettings.getGemHome()));
        return new MetaDataFactory() {

            private Map<String, Object> map = new HashMap<String, Object>();

            {
                map.putAll(parentProps);
                map.put("compassSettings", compassSettings);
            }

            @Override
            public Map<String, Object> create() {
                return Collections.unmodifiableMap(map);                                    
            }
        };
    }

    private File computeProjectDir() {
        File candidate = new File(Context.get().getServletContext().getRealPath("./"));
        while (candidate != null && candidate.isDirectory()) {
            File[] files = candidate.listFiles();
            for (File f : files) {
                if (f.getName().equals("pom.xml")) {
                    return candidate;
                }
            }
            candidate = candidate.getParentFile();
        }
        throw new RuntimeException("Cannot location maven base directory");
    }

    private CompassCompiler createCompiler(String gemHome) {
        ScriptingContainer container = new ScriptingContainer();
        container.setCompileMode(RubyInstanceConfig.CompileMode.JIT);
        container.setCompatVersion(CompatVersion.RUBY1_9);
        container.put("$gem_home", gemHome);

        Object reciver;
        try {
            reciver = container.runScriptlet(IOUtils.toString(CompassCssPreProcessor.class.getResource("/wro4j_compass.rb")));
        } catch (IOException e) {
            throw new WroRuntimeException(e.getMessage(), e);
        }
        return container.getInstance(reciver, CompassCompiler.class);
    }

    private String computePath(File projectBaseDir, String relativePath) {
        if(relativePath == null) {
            return projectBaseDir.getAbsolutePath();
        }
        else {
            return new File(projectBaseDir, relativePath).getAbsolutePath();
        }
    }

}
