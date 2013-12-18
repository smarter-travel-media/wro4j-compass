package no.bekk.wro4j.compass;

import org.apache.commons.io.IOUtils;
import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.util.StopWatch;

public class CompassEngine {


    private String compassBaseDir;
    private final CompassCompiler compiler;
    private final static Logger LOG = LoggerFactory.getLogger(CompassEngine.class);


    public CompassEngine(CompassCompiler compiler, String compassBaseDir) {
        this.compiler = compiler;
        this.compassBaseDir = compassBaseDir;
    }

    public String process(String content, String realFileName) {
		final StopWatch stopWatch = new StopWatch();
		try {

			stopWatch.start("process compass");

            return compiler.compile(compassBaseDir, content.replace("'", "\""), realFileName);

		} catch (Exception e) {
            e.printStackTrace();
			throw new WroRuntimeException(e.getMessage(), e);

        } finally {

			stopWatch.stop();
            LOG.debug("Finished in: {}", stopWatch.getLastTaskTimeMillis());
		}
	}
}