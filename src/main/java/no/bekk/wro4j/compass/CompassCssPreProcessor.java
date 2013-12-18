package no.bekk.wro4j.compass;

import org.apache.commons.io.IOUtils;
import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig;
import org.jruby.embed.ScriptingContainer;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.metadata.MetaDataFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

@SupportedResourceType(ResourceType.CSS)
public class CompassCssPreProcessor implements ResourcePreProcessor {

	public static final String ALIAS = "compassCss";

    @Inject
    private MetaDataFactory metaDataFactory;


    private final static CompassCompiler compiler;
    /**
     * this is all done in a static block because the constructor is also called multiple times. and we only ever want
     * to initialize the compiler once. Causes race condition when used with a filter. see the readme file for more details.
     */
    static {

        ScriptingContainer container = new ScriptingContainer();
        container.setCompileMode(RubyInstanceConfig.CompileMode.JIT);
        container.setCompatVersion(CompatVersion.RUBY1_9);

        Object reciver;
        try {
            reciver = container.runScriptlet(IOUtils.toString(CompassCssPreProcessor.class.getResource("/wro4j_compass.rb")));
        } catch (IOException e) {
            e.printStackTrace();
            throw new WroRuntimeException(e.getMessage(), e);
        }
        compiler = container.getInstance(reciver, CompassCompiler.class);
    }

    @Override
	public void process(Resource resource, Reader reader, Writer writer) throws IOException {

        Map<String, Object> props = metaDataFactory.create();
        CompassSettings compassSettings = (CompassSettings) props.get("compassSettings");
        File projectBaseDir = compassSettings.getProjectBaseDir();
        StandaloneContext context = compassSettings.getStandaloneContext();
        String compassBaseDir = compassSettings.getCompassBaseDir();

		final String content = IOUtils.toString(reader);
		
		try {
			String result =  new CompassEngine(compiler, computePath(projectBaseDir, compassBaseDir)).process(content, computeResourcePath(resource, context, projectBaseDir));
			writer.write(result);
		} catch (final WroRuntimeException e) {
			onException(e);
//		  final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
//		  LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
//			  + " resource, no processing applied...", e);
		} finally {
			reader.close();
			writer.close();
		}

	}

	private void onException(WroRuntimeException e) {
		e.printStackTrace();
	}

    private String computeResourcePath(Resource resource, StandaloneContext standaloneContext, File projectBaseDir) {
        return new File(standaloneContext != null ? standaloneContext.getContextFolder() : projectBaseDir, resource.getUri()).getAbsolutePath();
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
