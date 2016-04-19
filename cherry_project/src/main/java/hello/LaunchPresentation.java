package hello;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
//LOG
import org.apache.log4j.Logger;

public class LaunchPresentation {

	//private static String url_to_website= PoppyController.url_to_website;
	private static int stop = 0; 
	private static Logger logger = Logger.getLogger(LaunchPresentation.class);
	
	public static void start( String excelFilePath) throws InterruptedException, IOException {
		
		// Listen signal "off"
		ToWebsite.setListeningSignal("off");
		
		// Go into presentation state => enable stop while speaking
		//LaunchPrimitive.setListenStateParameter("presentation");
		
		
		// Start Listen Primitive
		//LaunchPrimitive.ListenPrimitive();
		
		// Stop head, start torso if not done
		/*String current_primitive = LaunchPrimitive.getRunningPrimitiveList();
		
		int index_torso = current_primitive.indexOf("torso_idle_motion");
		int index_head = current_primitive.indexOf("head_idle_motion");
		
		if(index_torso == -1){
			LaunchPrimitive.playBehaviorPrimitive("torso_idle_motion");
		}
		if(index_head != -1){
			LaunchPrimitive.stopPrimitive("head_idle_motion");
		}*/
		
		
		// Declare Arrays
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> list_text = new ArrayList<String>();
		ArrayList<String> list_img = new ArrayList<String>();
		
		// Get list of primitive into Excel file
		try {
			list = SimpleExcelReaderExample.getExcelField(excelFilePath,"Behave");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		list.add(0,"Start");
		System.out.println("\nList of " + "Behave: " + list );
		
		// Get list of TTS into Excel file
		try {
			list_text = SimpleExcelReaderExample.getExcelField(excelFilePath,"Text");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		list_text.add(0,"Start");
		System.out.println("\nList of " + "Text: " + list_text );
		
		//Get list of picture to display
		try {
			list_img = SimpleExcelReaderExample.getExcelField(excelFilePath,"Slide");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		list_img.add(0,"Start");
		System.out.println("\nList of " + "diapo: " + list_img );
		
		play(list,list_text,list_img);
	}
	
	public static void playFromJson(JSONObject my_json) throws InterruptedException, IOException {
	
		int step_nb = my_json.length();
		System.out.println("\n Nombre d'etapes: " + step_nb);
		
		// Declare Arrays
		ArrayList<String> list = new ArrayList<String>();
		list.add(0,"Start");
		ArrayList<String> list_text = new ArrayList<String>();
		list_text.add(0,"Start");
		ArrayList<String> list_img = new ArrayList<String>();
		list_img.add(0,"Start");
		
		for(int i = 0; i < step_nb; i++ ) 
		{
			JSONObject loop_json = my_json.getJSONObject("_" + Integer.toString(i));
			System.out.println("\n" + loop_json.toString() );
			
			
			String behave = loop_json.getString("Behave"); 
			String text = loop_json.getString("Text"); 
			String slide = loop_json.getString("Slide"); 
			
			list.add(behave.trim());
			list_text.add(text);
			list_img.add(slide.trim());
			
		}
		
		System.out.println("\n Behave: " + list);
		System.out.println("\n Text: " + list_text);
		System.out.println("\n Slide: " + list_img);
	
		try{
			play(list, list_text, list_img);
		}
		catch(Exception e)
		{
			System.out.println("\n Erreur" + e);
		}
	
	
	}
		// Play presentation
	public static void play(ArrayList<String> list, ArrayList<String> list_text, ArrayList<String> list_img ) throws InterruptedException, IOException {
		
		int index_behave = -1;
		int index_speak = -1;
		int index_img = -1;
		
		// Stop head, start torso if not done
		String current_primitive = LaunchPrimitive.getRunningPrimitiveList();
		
		int index_torso = current_primitive.indexOf("torso_idle_motion");
		int index_head = current_primitive.indexOf("head_idle_motion");
		
		if(index_torso == -1){
			LaunchPrimitive.playBehaviorPrimitive("torso_idle_motion");
		}
		if(index_head != -1){
			LaunchPrimitive.stopPrimitive("head_idle_motion");
		}
		
		for(int i=1; i< list.size(); i++){
			
			if (stop == 1){
				LaunchPrimitive.playSpeakPrimitive("D'accord, j'arr\u00eate la pr\u00e9sentation");
				LaunchPrimitive.playBehaviorPrimitive("rest_open_behave");
				stop = 0;
				break;
			}
			// Check currently running primitive
			 
			current_primitive = LaunchPrimitive.getRunningPrimitiveList();
			System.out.println("\nStr " + current_primitive );
			
			index_behave = current_primitive.indexOf(list.get(i-1));
			index_speak = current_primitive.indexOf("speak");
			
			System.out.println("\nIndex: " + index_behave +"of :" + list.get(i-1) + " speak: " + index_speak);
			
			// Check wether speak or behave are still running
			//while( index_speak != -1 && index_behave != -1)
			while(index_behave != -1)
			{
				Thread.sleep(1000);
				
				System.out.println("\n			Wainting for " + list.get(i-1) + " to stop" );
				
				current_primitive = LaunchPrimitive.getRunningPrimitiveList();
				System.out.println("\n			Str " + current_primitive );
				
				index_behave = current_primitive.indexOf(list.get(i-1));
    			index_speak = current_primitive.indexOf("speak");
				
    			System.out.println("\n			Index: " + index_behave +" of behave: " + list.get(i-1) + " of speak: " + index_speak);
    			
			}
			
			// Picture			
			if ( !list_img.get(i).equals(list_img.get(i-1)))
			{	
				System.out.println("\n Old: " + list_img.get(i-1) + " New: " + list_img.get(i));
				// Shut down previous img (exept "start)
  				if (list_img.get(i-1) != "Start"){
					
  					ToWebsite.deletePicture(list_img.get(i-1));
  					/*try {
						HttpURLConnectionExample.sendGet( url_to_website + "/PhpProject_test/WS_video.php?name=" + list_img.get(i-1) + "&owner=admin_off");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println("\n Erreur" + e);
						e.printStackTrace();
					}*/
				}
				// set the new one
  				index_img = i;
  				ToWebsite.displayPicture(list_img.get(index_img));
  				
  				/*try {
					
					HttpURLConnectionExample.sendGet(url_to_website + "/PhpProject_test/WS_video.php?name=" + list_img.get(index_img));
					System.out.println("\n" + url_to_website + "/PhpProject_test/WS_video.php?name=" + list_img.get(index_img));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("\n Erreur" + e);
					e.printStackTrace();
				}*/
				Thread.sleep(1000);

			}
			
			// text
			//LaunchPrimitive.playSpeakPrimitive(list_text.get(i));
			String i_str = Integer.toString(i);
			LaunchPrimitive.playSpeakPrimitive(list_text.get(i));
			System.out.println("\n Speak: " + list_text.get(i));
			
			// behavior
			LaunchPrimitive.playBehaviorPrimitive(list.get(i));
			System.out.println("\n Play behavior: " + list.get(i));
				
			
		}
		
		Thread.sleep(4000);
		// Kill the last diapo
		ToWebsite.deletePicture(list_img.get(index_img));
		
		/*
		 try {
			HttpURLConnectionExample.sendGet(url_to_website + "/WS_video.php?name="  + list_img.get(index_img) +"&owner=admin_off");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("\n Erreur" + e);
			e.printStackTrace();
		}	// TODO Auto-generated method stub
		*/
		LaunchPrimitive.stopPrimitive("torso_idle_motion");
		System.out.println("\n Stop behavior: " + "torso_idle_motion");
		
		// check listen state
    	//String listen_state = LaunchPrimitive.getListenStateParameter();
    	//int is_state = listen_state.indexOf("stop");
    	
    	// Si pas en stop
    	//if(is_state == -1){
		// Go into normal state => disable stop while speaking
    	//	LaunchPrimitive.setListenStateParameter("normal");
    	//}
		// Set stop to 0
		stop = 0;
		
		// Back to listen
		//LaunchPrimitive.ListenPrimitive();*/

	}
	public static void stop() throws InterruptedException, IOException {
		
		stop = 1;
		System.out.println("\n Set stop to 1");
		//LaunchPrimitive.setListenStateParameter("normal");
		
	}
}
