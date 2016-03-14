package jssc.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;

public class Activator implements BundleActivator {

	private ServiceRegistration<?> staticCodeRemoverHook;

	public void start(BundleContext context) throws Exception {
		String processor = context.getProperty(Constants.FRAMEWORK_PROCESSOR);
		processor = processor.replace('-', '_');
		System.loadLibrary("jSSC-2.8_" + processor);
		staticCodeRemoverHook = context.registerService(WeavingHook.class.getName(), new StaticCodeBlockRemover(), null);

	}

	public void stop(BundleContext context) throws Exception {
		staticCodeRemoverHook.unregister();

	}

}
