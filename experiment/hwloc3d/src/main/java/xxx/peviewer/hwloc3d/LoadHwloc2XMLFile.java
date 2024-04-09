package xxx.peviewer.hwloc3d;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import xxx.peviewer.hwloc3d.xjcgenerated.Topology;
//import xxx cannot be resolved? try project clean

public class LoadHwloc2XMLFile {
	public static Topology loadHwloc2XMLFile(File hwloc2XMLFile) {
		try {
			System.setProperty("javax.xml.accessExternalDTD", "all");
			// creating the JAXB context
			JAXBContext jContext = JAXBContext.newInstance(Topology.class);
			// creating the unmarshall object
			Unmarshaller unmarshallerObj = jContext.createUnmarshaller();
			// calling the unmarshall method
			Topology topTop = (Topology) unmarshallerObj.unmarshal(hwloc2XMLFile);

			System.out.println(topTop);
			System.out.println(topTop.getVersion());
			//Breakpoint here
			return topTop;			
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return null;
	}
}
