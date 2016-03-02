package jssc.osgi;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;


import org.objectweb.asm.ClassAdapter;

public class StaticCodeBlockRemover implements WeavingHook {

	public void weave(WovenClass wovenClass) {
		if (wovenClass.getClassName().equals("jssc.SerialNativeInterface")) {
			wovenClass.setBytes(transform(wovenClass.getBytes()));
		}
	}

	public byte[] transform(byte[] origClassData) {
		ClassReader cr = new ClassReader(origClassData);
		final ClassWriter cw = new ClassWriter(0);

		// feed the original class to the ClassAdapter
		cr.accept(new ClinitRemoverClassAdapter(cw), 0);

		// produce the modified class
		return cw.toByteArray();
	}

	class ClinitRemoverClassAdapter extends ClassAdapter {
	    public ClinitRemoverClassAdapter(final ClassVisitor cv) {
	        super(cv);
	    }

	    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
	        if (name.equals("<clinit>")) {
	            return new EmptyVisitor();
	        }
	        return cv.visitMethod(access, name, desc, signature, exceptions);
	    }
	}
}
