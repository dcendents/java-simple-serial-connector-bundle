package jssc.osgi;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;

public class Activator implements BundleActivator {

	private ServiceRegistration<?> staticCodeRemoverHook;

	public void start(BundleContext context) throws Exception {
		String processor = context.getProperty(Constants.FRAMEWORK_PROCESSOR);

        if("arm".equals(processor)) {
            String floatStr = "sf";
            String javaLibPath = System.getProperty("java.library.path");
            String abi = System.getProperty("sun.arch.abi");

            boolean javaLibHF = javaLibPath != null && (javaLibPath.toLowerCase().contains("gnueabihf") || javaLibPath.toLowerCase().contains("armhf"));
            boolean abiHF = abi != null && (abi.toLowerCase().contains("gnueabihf") || abi.toLowerCase().contains("armhf"));

            if(javaLibHF || abiHF){
                floatStr = "hf";
            }
            else {
                try {
                    Process readelfProcess =  Runtime.getRuntime().exec("readelf -A /proc/self/exe");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(readelfProcess.getInputStream()));
                    String buffer = "";
                    while((buffer = reader.readLine()) != null && !buffer.isEmpty()){
                        if(buffer.toLowerCase().contains("Tag_ABI_VFP_args".toLowerCase())){
                            floatStr = "hf";
                            break;
                        }
                    }
                    reader.close();
                }
                catch (Exception ex) {
                    //Do nothing
                }
            }
            processor = processor + floatStr;
        }

        processor = processor.replace('-', '_');
		System.loadLibrary("jSSC-2.8_" + processor);
		staticCodeRemoverHook = context.registerService(WeavingHook.class.getName(), new StaticCodeBlockRemover(), null);

	}

	public void stop(BundleContext context) throws Exception {
		staticCodeRemoverHook.unregister();

	}

}
