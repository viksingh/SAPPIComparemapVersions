package org.saki.maps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class CompareMapVersions {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		int i = 0;

		// Read the file obtained from SimpleQuery
		File fileSID1 = new File("C:\\temp\\SID1_maps.txt");
		File fileSID2 = new File("C:\\temp\\SID2_maps.txt");

		List<MappingObject> devMaps = new ArrayList<MappingObject>();
		List<MappingObject> PPMaps = new ArrayList<MappingObject>();

		// Get the document links
		Document docSID1 = Jsoup.parse(fileSID1, "UTF-8");
		Elements linksSID1 = docSID1.select("a[href]");

		CloseableHttpClient httpclientSID1 = HttpClients.createDefault();
		CredentialsProvider credentialsProviderSID1 = new BasicCredentialsProvider();
		credentialsProviderSID1.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials("user:password"));
		HttpClientContext localContextSID1 = HttpClientContext.create();
		localContextSID1.setCredentialsProvider(credentialsProviderSID1);

		outerloopSID1: for (Element link : linksSID1) {
			String url = link.attr("abs:href");
			String mapName = url.substring(79, url.indexOf("%"));
			HttpPost httpPost = new HttpPost(url);
			CloseableHttpResponse response = httpclientSID1.execute(httpPost,
					localContextSID1);
			HttpEntity responseEntity = response.getEntity();

			String content = EntityUtils.toString(responseEntity);
			content = content.replace("<p1:", "<");
			content = content.replace("</p1:", "</");
			content = content.replace("<tr:", "<");
			content = content.replace("</tr:", "</");

			Document docResponse = Jsoup.parse(content, "", Parser.xmlParser());

			for (Element e : docResponse.select("xiObj")) {
				MappingObject map = new MappingObject();
				for (Element e1 : e.select("idInfo")) {

					for (Element e2 : e1.select("vc")) {
						String SWCV = e2.attr("caption");
						map.setSWCV(SWCV);
						break;
					}

					for (Element e2 : e1.select("key")) {
						Element e3 = e2.child(1);
						String namesPace = e3.text();
						map.setNamespace(namesPace);
					}
					map.setMapName(mapName);
					map.setSID("SID1");
					map.setVersion(e1.attr("VID"));

					System.out.println("Reading map # " + i + " " + mapName);
					i++;
/*					if (i == 5) {
						break outerloopSID1;
					}*/

				}

				for (Element e1 : docResponse.select("generic")) {
					for (Element e2 : docResponse.select("admInf")) {
						for (Element e4 : docResponse.select("modifBy")) {
							String modifBy = e4.text();
							map.setModifBy(modifBy);
						}
						for (Element e5 : docResponse.select("modifAt")) {
							String modifAt = e5.text();
							map.setModifAt(modifAt);

						}
					}

				}

				devMaps.add(map);

			}

		}

		i = 0;
		Document docSID2 = Jsoup.parse(fileSID2, "UTF-8");
		Elements linksSID2 = docSID2.select("a[href]");
		CloseableHttpClient httpclientSID2 = HttpClients.createDefault();
		CredentialsProvider credentialsProviderSID2 = new BasicCredentialsProvider();
		credentialsProviderSID1.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials("user:password"));
		HttpClientContext localContextSID2 = HttpClientContext.create();
		localContextSID1.setCredentialsProvider(credentialsProviderSID1);

		outerloopSID2: for (Element link : linksSID2) {
			MappingObject mapPP = new MappingObject();
			String url = link.attr("abs:href");
			String mapName = url.substring(79, url.indexOf("%"));
			HttpPost httpPost = new HttpPost(url);
			CloseableHttpResponse response = httpclientSID1.execute(httpPost,
					localContextSID1);
			HttpEntity responseEntity = response.getEntity();

			String content = EntityUtils.toString(responseEntity);
			content = content.replace("<p1:", "<");
			content = content.replace("</p1:", "</");
			content = content.replace("<tr:", "<");
			content = content.replace("</tr:", "</");

			Document docResponse = Jsoup.parse(content, "", Parser.xmlParser());

			for (Element e : docResponse.select("xiObj")) {
				for (Element e1 : e.select("idInfo")) {

					for (Element e2 : e1.select("vc")) {
						String SWCV = e2.attr("caption");
						mapPP.setSWCV(SWCV);
						break;
					}

					for (Element e2 : e1.select("key")) {
						Element e3 = e2.child(1);
						String namesPace = e3.text();
						mapPP.setNamespace(namesPace);
					}

					mapPP.setMapName(mapName);
					mapPP.setSID("SID2");
					mapPP.setVersion(e1.attr("VID"));

					System.out.println("Reading map # " + i + " " + mapName);
					i++;
/*					if (i == 600) {
						break outerloopSID2;
					}
*/				}

				for (Element e1 : docResponse.select("generic")) {
					for (Element e2 : docResponse.select("admInf")) {
						for (Element e4 : docResponse.select("modifBy")) {
							String modifBy = e4.text();
							mapPP.setModifBy(modifBy);

						}
						for (Element e5 : docResponse.select("modifAt")) {
							String modifAt = e5.text();
							mapPP.setModifAt(modifAt);

						}
					}

				}

				PPMaps.add(mapPP);

			}

		}
		int index = -1;
		Collections.sort(PPMaps, MappingObject.MapNameComparator);

		String filename = "C:/temp/MapVersions.xls";
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("FirstSheet");

		HSSFRow rowhead = sheet.createRow((short) 0);
		rowhead.createCell(0).setCellValue("MapName");
		rowhead.createCell(1).setCellValue("SID");
		rowhead.createCell(2).setCellValue("Namespace");
		rowhead.createCell(3).setCellValue("SWCV");
		rowhead.createCell(4).setCellValue("Version");
		rowhead.createCell(5).setCellValue("ModifyBy");
		rowhead.createCell(6).setCellValue("ModifyAt");

		rowhead.createCell(7).setCellValue("Match?");

		rowhead.createCell(8).setCellValue("MapName");
		rowhead.createCell(9).setCellValue("SID");
		rowhead.createCell(10).setCellValue("Namespace");
		rowhead.createCell(11).setCellValue("SWCV");
		rowhead.createCell(12).setCellValue("Version");
		rowhead.createCell(13).setCellValue("ModifyBy");
		rowhead.createCell(14).setCellValue("ModifyAt");

		int j = 0;

		// Now compare version
		for (MappingObject devMap : devMaps) {
			j++;
			MappingObject PPMap = new MappingObject();
			index = Collections.binarySearch(PPMaps, devMap,
					MappingObject.MapNameComparator);

			if (index >= 0) {
				PPMap = PPMaps.get(index);
				System.out.println("Object Found "
						+ PPMaps.get(index).getMapName());
			}

			else {
				System.out.println("Object not Found " + devMap.getMapName());

			}

			HSSFRow row = sheet.createRow((short) j);

			row.createCell(0).setCellValue(devMap.mapName);
			row.createCell(1).setCellValue(devMap.SID);
			row.createCell(2).setCellValue(devMap.namespace);
			row.createCell(3).setCellValue(devMap.SWCV);
			row.createCell(4).setCellValue(devMap.version);
			row.createCell(5).setCellValue(devMap.modifBy);
			row.createCell(6).setCellValue(devMap.modifAt);

			String devVersion = devMap.getVersion();
			String PPVersion = PPMap.getVersion();

			if (devVersion.equals(PPVersion)) {
				row.createCell(7).setCellValue("Yes");
			} else {
				row.createCell(7).setCellValue("No");
			}

			row.createCell(8).setCellValue(PPMap.mapName);
			row.createCell(9).setCellValue(PPMap.SID);
			row.createCell(10).setCellValue(PPMap.namespace);
			row.createCell(11).setCellValue(PPMap.SWCV);
			row.createCell(12).setCellValue(PPMap.version);
			row.createCell(13).setCellValue(PPMap.modifBy);
			row.createCell(14).setCellValue(PPMap.modifAt);

			index = -1;
		}

		FileOutputStream fileOut = new FileOutputStream(filename);
		workbook.write(fileOut);
		fileOut.close();

	}

}
