import com.grinderwolf.swm.plugin.world.importer.WorldImporter;
import com.grinderwolf.swm.nms.CraftSlimeWorld;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImportWorlds {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java ImportWorlds <worldDir> <output.slime>");
            return;
        }

        File worldDir = new File(args[0]);
        File outputFile = new File(args[1]);

        if (!worldDir.isDirectory()) {
            System.out.println("Error: " + worldDir + " is not a directory");
            return;
        }

        System.out.println("Reading world from: " + worldDir.getAbsolutePath());
        CraftSlimeWorld slimeWorld = WorldImporter.readFromDirectory(worldDir);

        System.out.println("Serializing...");
        byte[] data = slimeWorld.serialize();

        System.out.println("Writing " + data.length + " bytes to " + outputFile.getAbsolutePath());
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(data);
        }

        System.out.println("Done!");
    }
}
