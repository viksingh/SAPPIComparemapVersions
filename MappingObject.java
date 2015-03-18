package org.saki.maps;

import java.util.Comparator;

public class MappingObject {

	String SID;
	String mapName;
	String version;
	String modifBy;
	String modifAt;	
	String SWCV;
	String namespace;
	
	public String getModifBy() {
		return modifBy;
	}

	public void setModifBy(String modifBy) {
		this.modifBy = modifBy;
	}

	public String getModifAt() {
		return modifAt;
	}

	public void setModifAt(String modifAt) {
		this.modifAt = modifAt;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}


	public String getSWCV() {
		return SWCV;
	}

	public void setSWCV(String sWCV) {
		SWCV = sWCV;
	}

	public String getSID() {
		return SID;
	}

	public void setSID(String sID) {
		SID = sID;
	}

	public String getMapName() {
		return mapName;
	}

	public void setMapName(String mapName) {
		this.mapName = mapName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int compareTo(MappingObject m) {
		int result = 0;
		result = this.mapName.compareTo(m.mapName);
		return result;

	}

	public static Comparator<MappingObject> MapNameComparator = new Comparator<MappingObject>() {

		public int compare(MappingObject map1, MappingObject map2) {

			String mapName1 = map1.getMapName().toUpperCase();
			String mapName2 = map2.getMapName().toUpperCase();
			
			String ns1 = map1.getNamespace();
			String ns2 = map2.getNamespace();
			
			String swcv1 = map1.getSWCV();
			String swcv2 = map2.getSWCV();
			
			String str1 = mapName1.concat(ns1.concat(swcv1));
			String str2 = mapName2.concat(ns2.concat(swcv2));
			

			// ascending order
			return str1.compareTo(str2);

		}

	};

}
