package scraping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import logic.data.Game;
import logic.data.GraphGame;
import logic.data.Multimedia;
import logic.data.PlatformInfo;

public class util {
	
	//Create a Game consistent with the new game just scraped
	public static Game initializeGameToAdd(JSONObject newGame) {
		System.out.println("-->[util][initializeGameToAdd] Initializing new game. ID:" + newGame.getInt("id"));
		Game gameToAdd = new Game();
		//Id
		gameToAdd.setId(newGame.getInt("id")); 
		
		//FavoriteCount
		gameToAdd.setFavouritesCount(0);
		
		//Title
		gameToAdd.setTitle(newGame.getString("name"));

		//Background_image		
		gameToAdd.setBackground_image(null);
		extractBackgroundImage(newGame, gameToAdd);
		
		//Rating
		gameToAdd.setRating(0.0);
				
		//Rating Count
		gameToAdd.setRatingCount(0);
				
		//Metacritic
		extractMetacritic(newGame, gameToAdd);
	
		//ViewsCount
		gameToAdd.setViewsCount(0);
			
		//Description
		gameToAdd.setDescription("Game description not available");
		extractDescription(newGame, gameToAdd);

		//Released
		extractReleaseDate(newGame, gameToAdd);
			
		//Genres and subgenres
		extractGenres(newGame, gameToAdd);
			
		//Releases
		extractPlatforms(newGame, gameToAdd);

		//Sales
		extractStores(newGame, gameToAdd);
			
		//Multimedia
		extractMultimedia(newGame, gameToAdd);

		System.out.println("-->[util][initializeGameToAdd] Created new game");
		return gameToAdd;
	}


	//Extract background image from a json and puts it in a object Game
	public static void extractBackgroundImage(JSONObject newGame, Game gameToAdd){
		try {
			if(newGame.has("background_image")) {
				if(newGame.get("background_image") instanceof String && !newGame.get("background_image").equals(null)) {
					gameToAdd.setBackground_image(newGame.getString("background_image"));
				}
			}
		} catch (Exception e) {
			System.out.println("-->[util][extractBackgroundImage] No background_image in suitable format available");
		}
	}
	
	
	//Extract metacritic from a json and puts it in a object Game
	public static void extractMetacritic(JSONObject newGame, Game gameToAdd) {
		try {
			if(newGame.has("metacritic")) {
				if(newGame.get("metacritic") instanceof Integer && !newGame.get("metacritic").equals(null)) {
					gameToAdd.setMetacritic(newGame.getInt("metacritic"));
				}
			}	
		} catch (Exception e) {
			System.out.println("-->[util][extractMetacritic] No metacritic in suitable format  available");
		}
	}
	
	
	//Extract description from a json and puts it in a object Game
	public static void extractDescription(JSONObject newGame, Game gameToAdd) {
		try {
			if(newGame.has("description_raw")) {
				if(newGame.get("description_raw") instanceof String && !newGame.get("description_raw").equals(null)) {
					if(newGame.getString("description_raw").length()!=0) {
						gameToAdd.setDescription(newGame.getString("description_raw"));;
					}
				}
			}	
		} catch (Exception e) {
			System.out.println("-->[util][extractDescription] No description in suitable format available");
		}
	}
		
	
	//Extract release date from a json and puts it in a object Game
	public static void extractReleaseDate(JSONObject newGame, Game gameToAdd) {
		try {
			if(newGame.has("released")) {
				if(newGame.get("released") instanceof String && !newGame.get("released").equals(null)) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date releaseDate = new Date();
					try {
						releaseDate = sdf.parse(newGame.getString("released"));
					} catch (JSONException e) {
						System.out.println("-->[util][extractReleaseDate] No release date in suitable format available");
					} catch (ParseException e) {
						System.out.println("-->[util][extractReleaseDate] No release date in suitable format available");
					}
				
					gameToAdd.setReleaseDate(releaseDate); 
				}
			}	
		} catch (Exception e) {
			System.out.println("-->[util][extractReleaseDate] No release date available");
		}
	}
	
	
	//Extract genres from a json and puts it in a object Game
	public static void extractGenres(JSONObject newGame, Game gameToAdd) {
		try {
			if(newGame.has("genres")) {
				if(newGame.get("genres") instanceof JSONArray && !newGame.get("genres").equals(null) ) {
					JSONArray genres = newGame.getJSONArray("genres");
					ArrayList<String> subgenres = new ArrayList<String>();
					if(!genres.isEmpty()) {
						if (genres.getJSONObject(0).has("name")) {
							gameToAdd.setGenres(genres.getJSONObject(0).getString("name"));
						}
						for (int i = 1; i < genres.length(); i++) {
							if(genres.getJSONObject(i).has("name")) {
								subgenres.add(genres.getJSONObject(i).getString("name"));
							}
						}
						if (subgenres.size() > 0) {
							gameToAdd.setSubGenres(subgenres);
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("-->[util][extractGenres] No genres in suitable format available");
		}
	}
	
	
	//Extract platforms from a json and puts it in a object Game
	public static void extractPlatforms(JSONObject newGame, Game gameToAdd) {
		try {
			if(newGame.has("platforms")) {
				if(newGame.get("platforms") instanceof JSONArray && !newGame.get("platforms").equals(null)) {
					JSONArray platforms = newGame.getJSONArray("platforms");
					ArrayList<String> releases = new ArrayList<String>();
					if(!platforms.isEmpty()) {
						for (int i = 1; i < platforms.length(); i++) {
							if(platforms.getJSONObject(i).has("plaform")) {
								if(platforms.getJSONObject(i).getJSONObject("plaform").has("name")) {
									releases.add(platforms.getJSONObject(i).getJSONObject("platform").getString("name"));
								}	
							}
						}
						if(releases.size() > 0) {
							gameToAdd.setReleases(releases);
						}
					}
				}
			}	
		} catch (Exception e) {
			System.out.println("-->[util][extractPlatforms] No genres in suitable format available");
		}
	}
	

	//Extract stores from a json and puts it in a object Game
	public static void extractStores(JSONObject newGame, Game gameToAdd) {
		try {
			if(newGame.has("stores")) {
				if(newGame.get("stores") instanceof JSONArray && !newGame.get("stores").equals(null)) {
					ArrayList<PlatformInfo> sales = new ArrayList<PlatformInfo>();
					//store, company, url 
					//name, domain, url_en
					JSONArray stores = newGame.getJSONArray("stores");
					if(!stores.isEmpty()) {
						for(int i = 0; i < stores.length(); i++) {
							PlatformInfo PI = new PlatformInfo();
							if(stores.getJSONObject(i).has("store")) {
								if(stores.getJSONObject(i).getJSONObject("store").has("name")) {
									PI.setStore(stores.getJSONObject(i).getJSONObject("store").getString("name"));
								}
								if(stores.getJSONObject(i).getJSONObject("store").has("domain")) {
									PI.setCompany(stores.getJSONObject(i).getJSONObject("store").getString("domain"));
								}
								if(stores.getJSONObject(i).getJSONObject("store").has("url_en")) {
									PI.setSaleUrl(stores.getJSONObject(i).getJSONObject("store").getString("url_en"));
								}
								sales.add(PI);
							}		
						}
						if(sales.size() > 0) {
							gameToAdd.setSales(sales);
						}
					}
				}
			}	
		} catch (Exception e) {
			System.out.println("-->[util][extractStores] No stores in suitable format  available");
		}
	}
	
	
	//Extract multimedia from a json and puts it in a object Game
	public static void extractMultimedia(JSONObject newGame, Game gameToAdd) {
		try {
			//multimedia: images;
			Multimedia multimedia = new Multimedia();
			ArrayList<String> images = new ArrayList<String>();
			ArrayList<String> videos = new ArrayList<String>();
			if(newGame.has("short_screenshots")) {
				if(newGame.get("short_screenshots") instanceof JSONArray && !newGame.get("short_screenshots").equals(null)) {
					JSONArray short_screenshots = newGame.getJSONArray("short_screenshots");
					for(int i = 0; i < short_screenshots.length(); i++) {
						if(short_screenshots.getJSONObject(i).has("image")) {
							images.add(short_screenshots.getJSONObject(i).getString("image"));
						}
					}
				}
			}
			
			//multimedia: video
			if(newGame.has("clip")) {
				if(newGame.get("clip") instanceof JSONObject && !newGame.get("clip").equals(null)) {
					JSONObject clip = newGame.getJSONObject("clip");
					if(clip.has("clips")) {
						JSONObject clips = newGame.getJSONObject("clip").getJSONObject("clips");
						if(clips.has("320")) {
							videos.add(clips.getString("320"));
						}
						if(clips.has("640")) {
							videos.add(clips.getString("640"));
						}
						if(clips.has("full")) {;
							videos.add(clips.getString("full"));
						}
					}
				}
			}
			if(images.size() > 0) {
				multimedia.setImages(images);
			}
			if(videos.size() > 0) {
				multimedia.setVideos(videos);
			}
			if(images.size() > 0 || videos.size() > 0) {
					gameToAdd.setMultimedia(multimedia);
			}
			
		} catch (Exception e) {
			System.out.println("-->[util][extractMultimedia] No multimedia in suitable format available");
		}
	}
		
	
	
	//Create GraphGame consistent with the new game just scraped
	public static GraphGame initializeGraphGameToAdd(Game gameToAdd) {
		System.out.println("-->[util][initializeGraphGameToAdd] Initializing GraphGame for game: " + gameToAdd.getTitle());
			
		/* _id + title + previewImage Constructor */
		GraphGame graphGameToAdd = new GraphGame(gameToAdd.getId().toString(), gameToAdd.getTitle(), gameToAdd.getBackground_image());
		System.out.println("-->[util][initializeGraphGameToAdd] Created GraphGame");
		return graphGameToAdd;
	}
	
	//Debug function: report informations about the database update
	public static void recapUpdate(List<Game> gamesAdded, int numberOfGamesAdded) {
		System.out.println("-->[util][recapUpdate] Recap of database update:");
		System.out.println("-->[util][recapUpdate] Games added: " + numberOfGamesAdded);
		for(int i = 0; i < numberOfGamesAdded; i++) {
			System.out.println("-->[util][recapUpdate] GameId: " + gamesAdded.get(i).getId() + ", title: " + gamesAdded.get(i).getTitle());
		}
		
	}
	
	//Write information about an error in file error.txt
	public static void writeErrorLog(String error) {
		Date date = new Date();
		Timestamp ts = new Timestamp(date.getTime());
		System.out.println("-->[util][writeErrorLog] Writing error log in file error.txt");
		try {
			FileWriter fw = new FileWriter("logs/errors.txt",true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(ts.toString() + ":" + error);
			bw.newLine();
			bw.close();
		} catch( Exception e) {
			
			System.out.println("-->[util][writeErrorLog] Failed to write into errors.txt file.");
		}
	}

}