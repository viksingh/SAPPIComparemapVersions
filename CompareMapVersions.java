package org.saki.maps;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

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
import org.apache.log4j.varia.NullAppender;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class CompareMapVersions {

	static String fileName = new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());	
	static final String EXCEL_PATH = "C:/temp/MapVersions_".concat(fileName).concat(".xls");
	
	private static final String SID1 = "SID1";
	static final String SID2 = "SID2";
	public static final String usrSys1 = "http://host1:port1/rep/support/SimpleQuery";
	public static final String urlSys2 = "http://host2:port2/rep/support/SimpleQuery";
	
	
	
	

	public static final String userSys1 = "user";
	public static final String passwordSys1 = "passwd";


	public static final String userSys2 = "user";
	public static final String passwordSys2 = "passwd";

	public static final String combinePasswdSys1 = userSys1.concat(":"
			.concat(passwordSys1));
	public static final String combinePasswdSys2 = userSys2.concat(":"
			.concat(passwordSys2));

	private static String base64Encode(String stringToEncode) {
		return DatatypeConverter.printBase64Binary(stringToEncode.getBytes());
	}

	private static void setCredentials(WebClient _webClient, String _password) {
		String base64encodedUsernameAndPassword = base64Encode(_password);
		_webClient.addRequestHeader("Authorization", "Basic "
				+ base64encodedUsernameAndPassword);
	}

	static String fillSimpleQueryParams(String _url, String _combinedPassword)
			throws IOException, MalformedURLException {
		WebClient webClient = new WebClient();
		setCredentials(webClient, _combinedPassword);

		HtmlPage currentPage = webClient.getPage(_url);
		HtmlForm form = (HtmlForm) currentPage.getByXPath("/html/body/form")
				.get(0);

		// Get SWC
		HtmlRadioButtonInput choseSWC = form
				.getInputByValue("All software components");
		choseSWC.click();

		// Don't do anything , it comes checked by default
		HtmlCheckBoxInput considerUnderLyingSWC = form.getInputByName("underL");

		// Unclick changeList User
		HtmlCheckBoxInput changeListUser = form.getInputByName("changeL");
		changeListUser.click();

		// Select Message Mapping
		HtmlSelect eSRObjType = form.getOneHtmlElementByAttribute("select",
				"name", "types");
		List<HtmlOption> options = eSRObjType.getOptions();
		for (HtmlOption op : options) {
			if (op.getValueAttribute().equals("XI_TRAFO")) {
				op.setSelected(true);
			}
		}

		HtmlSelect resAttr = form.getOneHtmlElementByAttribute("select",
				"name", "result");
		List<HtmlOption> options1 = resAttr.getOptions();
		for (HtmlOption op : options1) {
			if (op.getValueAttribute().equals("RA_XILINK")) {
				op.setSelected(true);
			}
		}

		HtmlSubmitInput submit = form.getInputByValue("Start query");
		HtmlPage resultPage = submit.click();
		String resultPageText = resultPage.asXml().toString();
		return resultPageText;
	}

	public static void main(String[] args) throws IOException {

		// Remove log4j warnings
		org.apache.log4j.BasicConfigurator.configure(new NullAppender());

		List<MappingObject> sys1Maps = new ArrayList<MappingObject>();
		List<MappingObject> sys2Maps = new ArrayList<MappingObject>();

		// Get the document links
		int i = 0;
		String resultPageTextSys1 = fillSimpleQueryParams(usrSys1,
				combinePasswdSys1);

		Document docSys1 = Jsoup.parse(resultPageTextSys1, "UTF-8");
		Elements linksSys1 = docSys1.select("a[href]");

		CloseableHttpClient httpClient1 = HttpClients.createDefault();
		CredentialsProvider credentialsProviderSys1 = new BasicCredentialsProvider();
		credentialsProviderSys1.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(combinePasswdSys1));
		HttpClientContext localContextSys1 = HttpClientContext.create();
		localContextSys1.setCredentialsProvider(credentialsProviderSys1);

		outerloopSys1: for (Element link : linksSys1) {
			String url = link.attr("abs:href");
			String mapName = url.substring(79, url.indexOf("%"));
			HttpPost httpPost = new HttpPost(url);
			CloseableHttpResponse response = httpClient1.execute(httpPost,
					localContextSys1);
			HttpEntity responseEntity = response.getEntity();

			String content = removeExtraTags(responseEntity);

			if (content.toLowerCase().contains("logon".toLowerCase())) {

				content = authAgain(url, combinePasswdSys1);

			}			

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
					map.setSID(CompareMapVersions.SID1);
					map.setVersion(e1.attr("VID"));

					System.out.println("Reading map # " + i + " " + mapName);
					i++;
					
				}

				for (Element e1 : docResponse.select("generic")) {
					for (Element e2 : e1.select("admInf")) {
						for (Element e4 : e2.select("modifBy")) {
							String modifBy = e4.text();
							map.setModifBy(modifBy);
						}
						for (Element e5 : docResponse.select("modifAt")) {
							String modifAt = e5.text();
							map.setModifAt(modifAt);

						}
					}

				}

				sys1Maps.add(map);

			}

		}

		 

		i = 0;

		String resultPageTextSys2 = fillSimpleQueryParams(urlSys2,
				combinePasswdSys2);
		Document docSys2 = Jsoup.parse(resultPageTextSys2, "UTF-8");

		Elements linksSys2 = docSys2.select("a[href]");

		CloseableHttpClient httpclientSys2 = HttpClients.createDefault();
		CredentialsProvider credentialsProviderSys2 = new BasicCredentialsProvider();
		credentialsProviderSys2.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(combinePasswdSys2));
		HttpClientContext localContextSys2 = HttpClientContext.create();
		localContextSys2.setCredentialsProvider(credentialsProviderSys2);

		outerloopSys2: for (Element link : linksSys2) {
			MappingObject mapSys2 = new MappingObject();
			String url = link.attr("abs:href");
			String mapName = url.substring(79, url.indexOf("%"));
			HttpPost httpPost = new HttpPost(url);
			CloseableHttpResponse response = httpclientSys2.execute(httpPost,
					localContextSys2);
			HttpEntity responseEntity = response.getEntity();

			String content = removeExtraTags(responseEntity);

			if (content.toLowerCase().contains("logon".toLowerCase())) {

				content = authAgain(url, combinePasswdSys1);

			}

			Document docResponse = Jsoup.parse(content, "", Parser.xmlParser());

			for (Element e : docResponse.select("xiObj")) {
				for (Element e1 : e.select("idInfo")) {

					for (Element e2 : e1.select("vc")) {
						String SWCV = e2.attr("caption");
						mapSys2.setSWCV(SWCV);
						break;
					}

					for (Element e2 : e1.select("key")) {
						Element e3 = e2.child(1);
						String namesPace = e3.text();
						mapSys2.setNamespace(namesPace);
					}

					mapSys2.setMapName(mapName);
					mapSys2.setSID(CompareMapVersions.SID2);
					mapSys2.setVersion(e1.attr("VID"));

					System.out.println("Reading map # " + i + " " + mapName);
					i++;

	}

				for (Element e1 : docResponse.select("generic")) {
					for (Element e2 : e1.select("admInf")) {
						for (Element e4 : e2.select("modifBy")) {
							String modifBy = e4.text();
							mapSys2.setModifBy(modifBy);

						}
						for (Element e5 : docResponse.select("modifAt")) {
							String modifAt = e5.text();
							mapSys2.setModifAt(modifAt);

						}
					}

				}

				sys2Maps.add(mapSys2);

			}

		}

		int index = -1;
		Collections.sort(sys2Maps, MappingObject.MapNameComparator);

		String filename = CompareMapVersions.EXCEL_PATH;
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

		rowhead.createCell(7).setCellValue("Present?");
		rowhead.createCell(8).setCellValue("Match?");

		rowhead.createCell(9).setCellValue("MapName");
		rowhead.createCell(10).setCellValue("SID");
		rowhead.createCell(11).setCellValue("Namespace");
		rowhead.createCell(12).setCellValue("SWCV");
		rowhead.createCell(13).setCellValue("Version");
		rowhead.createCell(14).setCellValue("ModifyBy");
		rowhead.createCell(15).setCellValue("ModifyAt");

		int j = 0;

		// Now compare version
		for (MappingObject sys1Map : sys1Maps) {
			j++;
			MappingObject sys2Map = new MappingObject();
			index = Collections.binarySearch(sys2Maps, sys1Map,
					MappingObject.MapNameComparator);

			if (index >= 0) {
				sys2Map = sys2Maps.get(index);
				System.out.println("Object Found "
						+ sys2Maps.get(index).getMapName());
			}

			else {
				System.out.println("Object not Found " + sys1Map.getMapName());

			}

			HSSFRow row = sheet.createRow((short) j);

			row.createCell(0).setCellValue(sys1Map.mapName);
			row.createCell(1).setCellValue(sys1Map.SID);
			row.createCell(2).setCellValue(sys1Map.namespace);
			row.createCell(3).setCellValue(sys1Map.SWCV);
			row.createCell(4).setCellValue(sys1Map.version);
			row.createCell(5).setCellValue(sys1Map.modifBy);
			row.createCell(6).setCellValue(sys1Map.modifAt);
			

			
			String devMapName = sys1Map.getMapName();
			String pPMapName = sys2Map.getMapName();
			
			if (devMapName.equals(pPMapName)) {
				row.createCell(7).setCellValue("Yes");
			} else {
				row.createCell(7).setCellValue("No");
			}
			
			

			String devVersion = sys1Map.getVersion();
			String PPVersion = sys2Map.getVersion();

			if (devVersion.equals(PPVersion)) {
				row.createCell(8).setCellValue("Yes");
			} else {
				row.createCell(8).setCellValue("No");
			}

			row.createCell(9).setCellValue(sys2Map.mapName);
			row.createCell(10).setCellValue(sys2Map.SID);
			row.createCell(11).setCellValue(sys2Map.namespace);
			row.createCell(12).setCellValue(sys2Map.SWCV);
			row.createCell(13).setCellValue(sys2Map.version);
			row.createCell(14).setCellValue(sys2Map.modifBy);
			row.createCell(15).setCellValue(sys2Map.modifAt);

			index = -1;
		}

		FileOutputStream fileOut = new FileOutputStream(filename);
		workbook.write(fileOut);
		fileOut.close();

	}

	static String authAgain(String _url, String _combinedPassword)
			throws IOException, MalformedURLException {

		WebClient webClient = new WebClient();
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		setCredentials(webClient, _combinedPassword);

		SgmlPage currentPage = webClient.getPage(_url);

		String content = currentPage.asXml();

		content = content.replace("<p1:", "<");
		content = content.replace("</p1:", "</");
		content = content.replace("<tr:", "<");
		content = content.replace("</tr:", "</");

		return content;
	}

	/**
	 * @param responseEntity
	 * @return
	 * @throws IOException
	 */

	static String removeExtraTags(HttpEntity responseEntity) throws IOException {
		String content = EntityUtils.toString(responseEntity);
		content = content.replace("<p1:", "<");
		content = content.replace("</p1:", "</");
		content = content.replace("<tr:", "<");
		content = content.replace("</tr:", "</");
		return content;
	}

}
