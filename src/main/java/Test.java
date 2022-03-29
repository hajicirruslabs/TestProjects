import com.semmle.autobuild.AutoBuild;
import com.semmle.util.exception.ResourceError;
import com.semmle.util.exception.UserError;
import com.semmle.util.logging.Streams;

public class Test {
	public static void main(String args[]) throws Exception {
		try {
			AutoBuild b = new AutoBuild();
			String[] newArgs = new String[args != null ? (args.length + 1) : 1];
			for(int i =0; i < args.length; i ++)
				newArgs[i] = args[i];
			newArgs[newArgs.length - 1] = "--no-indexing";
			try {
				System.exit(b.run(newArgs));
				b.close();
			} catch (Throwable throwable) {
				try {
					b.close();
				} catch (Throwable throwable1) {
					throwable.addSuppressed(throwable1);
				}
				throw throwable;
			}
		} catch (UserError e) {
			Streams.err().println("ERROR: " + e.getMessage());
			System.exit(1);
		} catch (ResourceError e) {
			Streams.err().println("ERROR: " + e.getMessage());
			System.exit(1);
		}
	}
}
