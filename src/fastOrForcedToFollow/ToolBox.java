package fastOrForcedToFollow;

import java.io.File;

/**
 * @author mpaulsen
 */
public class ToolBox {

	/**
	 * Auxiliary function to create subfolder if needed.
	 * @param folder The folder being checked for existence.
	 */
	public static void createFolderIfNeeded(String folder){
		File file = new File(folder);
		if(!file.exists()){
			file.mkdirs();
		}
	}
	
}
