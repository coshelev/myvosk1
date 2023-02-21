
import java.io.InputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.vosk.LogLevel;
import org.vosk.Recognizer;
import org.vosk.LibVosk;
import org.vosk.Model;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;

import java.net.URI;
import java.net.http.*;
import com.google.gson.*;
import java.util.*;

public class A {

    public static void main(String[] argv) throws IOException, UnsupportedAudioFileException {
        LibVosk.setLogLevel(LogLevel.DEBUG);


	Gson gson= new Gson();
        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("type",          "vosk");
        inputMap.put("signature",     "");
        inputMap.put("text",   	"");
        String requestBody = gson.toJson(inputMap);

	var client = HttpClient.newHttpClient();

	 try (Model model = new Model("/home/coshelev/vosk/vosk-model-small-ru-0.22");
                 Recognizer recognizer = new Recognizer(model, 16000)) {
		
			File f = new File("/home/coshelev/vosk/");
			String[] list = f.list();
			for (int i  = 0;i < list.length; i++) {
				//System.out.println(list[i]);
				if (list[i].contains(".wav")==false)
					continue;
				String inputFile = list[i];
				System.out.println("**");
				System.out.println(inputFile);

				System.out.println("*****");			
			
			
			 	InputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream("/home/coshelev/vosk/"+inputFile)));	
				int nbytes;
        		   	 byte[] b = new byte[4096];
           		 	while ((nbytes = ais.read(b)) >= 0) {
                			if (recognizer.acceptWaveForm(b, nbytes)) {
                  		 		// System.out.println(recognizer.getResult());
               			 	} else {
                			//System.out.println(recognizer.getPartialResult());
					}
        	    		}

				var fR = recognizer.getFinalResult();
 				System.out.println(fR);

				String outputFile = inputFile.replace(".wav",".txt1");
				System.out.println("outputFile = "+outputFile);
				
				FileWriter fileWriter = new FileWriter("/home/coshelev/vosk/"+outputFile);
    				PrintWriter printWriter = new PrintWriter(fileWriter);
    				printWriter.print(fR);
    				printWriter.close();

               			inputMap.put("signature",  	outputFile);
               			inputMap.put("text",   		fR);
               			requestBody = gson.toJson(inputMap);

               			var request = HttpRequest.newBuilder()
				.uri(URI.create("http://motor-luidor.ru/leads/anypost/anypost.php"))
                  		.POST(HttpRequest.BodyPublishers.ofString(requestBody))
                  		.header("accept", "application/json") 
                  		.build();
              
               			client.sendAsync(request,HttpResponse.BodyHandlers.ofString());
			};//for
	
        }
    }
}
