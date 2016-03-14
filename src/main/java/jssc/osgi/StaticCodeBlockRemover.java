package jssc.osgi;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
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

		@Override
		public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
			if ("<clinit>".equals(name)) {
				return new EmptyVisitor();
			}
			return cv.visitMethod(access, name, desc, signature, exceptions);
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			if ("osType".equals(name)) {
				final int OS_LINUX = 0;
				final int OS_WINDOWS = 1;
				final int OS_SOLARIS = 2;
				final int OS_MAC_OS_X = 3;

				int val = -1;
				String osName = System.getProperty("os.name");
				if (osName.equals("Linux")) {
					val = OS_LINUX;
				} else if (osName.startsWith("Win")) {
					val = OS_WINDOWS;
				} else if (osName.equals("SunOS")) {
					val = OS_SOLARIS;
				} else if (osName.equals("Mac OS X") || osName.equals("Darwin")) {
					val = OS_MAC_OS_X;
				}

				return super.visitField(access, name, desc, signature, val);
			}

			return super.visitField(access, name, desc, signature, value);
		}
	}
}
