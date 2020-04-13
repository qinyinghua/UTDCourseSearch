package com.mvcApp.test.mvcApp.rest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

@EnableScheduling
@Controller
public class HelloRestController {

	final boolean FORCENEW = false;
	
	@Scheduled(cron = "0 1 1 * * ?")
	public void scheduleTaskWithFixedRate() {
	    UTDDBUpgrader.main(null);
	    readSearches();
		readHashMaps();
	}
	
	@RequestMapping("/")
	public String index() {
		return "index.html";
	}
	
	@RequestMapping("/home")
	public String home() {
		return "home.jsp";
	}
	
	SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
	
	@RequestMapping("/cc")
	public String cc() {
		return "test.html";
	}
	
	static HashMap<String, String> ratings = new HashMap<String, String>();
	
	static HashMap<String, double[]> recordedGrades = new HashMap<String, double[]>();
	
	static void readRecordedGrades() {
		try {
			FileInputStream fileIn = new FileInputStream("recordedGrades.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			recordedGrades = (HashMap<String, double[]>) in.readObject();
			in.close();
			fileIn.close();
			System.err.println("Read complete! Size = " + ratings.size());
		} catch (IOException i) {
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Classes not found");
			return;
	    }
	}
	
	public static void readHashMaps() {
		try {
			FileInputStream fileIn = new FileInputStream("recordedRatings.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			ratings = (HashMap<String, String>) in.readObject();
			in.close();
			fileIn.close();
			System.err.println("Read complete! Size = " + ratings.size());
		} catch (IOException i) {
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Classes not found");
			return;
	    }
	}
	
	static void saveHashMaps() {
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream("recordedRatings.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(ratings);
			out.close();
			fileOut.close();
			
			System.out.println("Wrote a new professor, new size = " + ratings.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static HashMap<String, String> searches = new HashMap<String, String>();
	
	public static void startup() {
		readSearches();
		readHashMaps();
		readRecordedGrades();
	}
	
	public static void readSearches() {
		try {
			FileInputStream fileIn = new FileInputStream("searches.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			searches = (HashMap<String, String>) in.readObject();
			in.close();
			fileIn.close();
			System.err.println("Read complete! Search size = " + ratings.size());
		} catch (IOException i) {
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Classes not found");
			return;
	    }
	}
	static void saveSearch() {
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream("searches.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(searches);
			out.close();
			fileOut.close();
			
			System.out.println("Wrote a new search, new size = " + searches.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String padRight(String s, int n) {
		return String.format("%-" + n + "s", s);
	}
	
	@RequestMapping("rmp")
	public ModelAndView rmp(@RequestParam String course, @RequestParam String term) {
		
		System.err.println(ratings.size());
		
		long t = System.currentTimeMillis();
		
		String output = "";
		String identifier = course + "%%" + term;
		if(!searches.containsKey(identifier) || FORCENEW) {
			output = newSearch(course, term);
			searches.put(identifier, output);
			saveSearch();
		} else {
			output = searches.get(identifier);
		}
		
		double time = (System.currentTimeMillis() - t);
		System.out.println("Completed Request in " + time/1000.0 + " seconds.");
		
		ModelAndView model = new ModelAndView("/home");
		model.addObject("output", output);
		model.addObject("course", course);
		model.addObject("time", time / 1000.0);
		model.addObject("numProfs", ratings.size());
		return model;
	}
	
	public String newSearch(String course, String term) {
		System.out.println(dateFormatLocal.format(new Date()) + "\tRequested Course: " + course);
		
		String output = "<table style=\"width: 100%;\" id=\"professors\">"
				+ "<colgroup>\r\n" + 
				"       <col span=\"1\" style=\"width: 5%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 20%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 10%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 10%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 15%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 7%;\">\r\n" + 
				"       <col span=\"1\" style=\"width: 33%;\">\r\n" + 
				"  </colgroup>"
				+ "<thead><tr data-sort-method=\"none\"><th>Open Status</th>"
				+ "<th>Name</th>"
				+ "<th>Professor</th>"
				+ "<th>Rating</th>"
				+ "<th>Avg. GPA</th>"
				+ "<th>SG Rank /100</th>"
				+ "<th>Schedule</th></tr></thead>";

		// String term = "term_20f?";

		String searchQuery = course.trim().replace(" ", "/") + "/" + term + "?";

		WebClient client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		
		ratings.put("-Staff-", "Not Found");
		try {
			String searchUrl = "https://coursebook.utdallas.edu/" + searchQuery;
			HtmlPage page = client.getPage(searchUrl);

			List l = page.getByXPath("//*/td[4]");

			for (int section = 1; section <= l.size(); section++) {
				long timeTrack = System.currentTimeMillis();
				
				HtmlTableRow tr = (HtmlTableRow) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]");
				
				HtmlSpan open = (HtmlSpan) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[1]/span");
				String name = tr.getCell(2).asText();
				String prof = tr.getCell(3).asText();
				String time = tr.getCell(4).asText();

				String rating = "Not Found";
				String profLastFirst = "-Staff-";
				
				if(!prof.trim().toLowerCase().contains("staff")) {
					profLastFirst = prof.split(" ")[1] + ", " + prof.split(" ")[0];
				}
				
				if(!ratings.containsKey(profLastFirst)) {
					System.out.println("Uh oh, checking web...:" + profLastFirst);
					if (!prof.toLowerCase().contains("staff")) {
						// Rate My Professor Scan
						String url = "https://www.ratemyprofessors.com/search.jsp?query=" + prof;
						HtmlPage rmp = client.getPage(url);
						// *[@id="searchResultsBox"]/div[2]/ul/li[1]/a/span[2]/span[2]
						// *[@id="searchResultsBox"]/div[2]/ul/li[2]/a/span[2]/span[2]
						HtmlUnorderedList allProfs = (HtmlUnorderedList) rmp
								.getFirstByXPath("//*[@id=\"searchResultsBox\"]/div[2]/ul");
						int index = -1;
						
						if(allProfs != null) {
							for (int i = 1; i <= allProfs.getChildElementCount(); i++) {
								HtmlSpan school = (HtmlSpan) rmp.getFirstByXPath(
										"//*[@id=\"searchResultsBox\"]/div[2]/ul/li[" + i + "]/a/span[2]/span[2]");
								if (school != null && school.asText().contains("The University of Texas at Dallas")) {
									index = i;
									break;
								}
							}

							try {
								HtmlAnchor a = (HtmlAnchor) rmp
										.getFirstByXPath("//*[@id=\"searchResultsBox\"]/div[2]/ul/li[" + index + "]/a");
								
								rmp = a.click();
								rating = ((HtmlDivision) rmp
										.getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[1]/div/div[1]"))
												.asText();
								
								if(!rating.contains("N/A")) {
									rating += " based on " + ((HtmlAnchor) rmp
											.getFirstByXPath("//*[@id=\"root\"]/div/div/div[2]/div[1]/div[1]/div[1]/div[2]/div/a"))
													.asText();
								} else {
									rating = "No Ratings";
								}
								
								ratings.put(profLastFirst, rating);
								saveHashMaps();
							} catch (Exception e) {
								ratings.put(profLastFirst, rating);
								saveHashMaps();
							}
						} else {
							ratings.put(profLastFirst, rating);
							saveHashMaps();
						}
					}
				} else {
					rating = ratings.get(profLastFirst);
				}
				
				String avgGPA = "N/A";
				double[] info = null;
				if(recordedGrades.containsKey(profLastFirst)) {
					info = recordedGrades.get(profLastFirst);
					double avg = (double) Math.round(info[0] * 100d) / 100d;
					avgGPA = avg + " with " + (int) info[1] + " students";
				}
				
				String overallRating = "N/A";
				double gpaWeight = 70;
				if(rating.contains("based on") && !avgGPA.contentEquals("N/A")) {
					double scores = Double.parseDouble(rating.split(" ")[0]) / 5 * (100 - gpaWeight) + info[0] / 4 * gpaWeight ;
					scores = (double) Math.round(1.3 * scores * 100d) / 100d; // adjust scaling !
					overallRating = scores + "";
				}
				
				
				String formatName = name.replaceAll("\\(.*\\)", "").replace("CV Honors", "CV");
				
				// HtmlAnchor a = (HtmlAnchor) page.getFirstByXPath("//*[@id=\"r-" + section + "\"]/td[2]/a");
				String url = "";//a.getAttribute("href").split("https://coursebook.utdallas.edu/search/")[1];
				
				/*if(rating.contains("No")) {
					output += "<tr data-sort-method='none'>";
				}*/
				
				output += "<tr>";
				output += "<td>" + open.asText() + "</td>";
				
				output += "<td><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://coursebook.utdallas.edu/clips/clip-section-v2.zog?id=" + url + "\">";
				if (name.contains("CV Honors"))
					output += "<b>" + formatName + "</b></a></td>";
				else
					output += formatName + "</a></td>";
				output += "<td>" + prof + "</td>";
				
				if(rating.contains("No")) 
					output += "<td data-sort='0'>" + rating + "</td>";
				else {
					String add = "";
					try {
						double r = Double.parseDouble(rating.split(" ")[0]);
						add = " class='";
						if(r <= 2.5) {
							add += "uhoh'";
						} else if(r >= 4.5) {
							add += "ahhh'";
						} else {
							add += "normal'";
						}
					}catch(Exception e) {}
					output += "<td" + add + ">" + rating + "</td>";
				}
				
				output += "<td>" + avgGPA + "</td>";
				
				output += "<td>" + overallRating + "</td>";
				
				output += "<td>" + time + "</td>";
				output += "</tr>";
				
				System.out.println("Processing " + padRight(profLastFirst, 40) + (System.currentTimeMillis() - timeTrack) + "ms");
			}
			
			output += "</table>";
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	
	
	/*
	 <%
    double num = Math.random();
    if (num > 0.95) {
  %>
      <h2>You'll have a luck day!</h2><p>(<%= num %>)</p>
  <%
    } else {
  %>
      <h2>Well, life goes on ... </h2><p>(<%= num %>)</p>
  <%
    }
  %>
  <a href="<%= request.getRequestURI() %>"><h3>Try Again!</h3></a>
	 */

}
