
import java.io.InputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.vosk.LogLevel;
import org.vosk.Recognizer;
import org.vosk.LibVosk;
import org.vosk.Model;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

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

	//split file
	WaveFileSplit();

	try (Model model = new Model("/home/coshelev/vosk/vosk-model-small-ru-0.22");
	//try (Model model = new Model("/home/coshelev/vosk/vosk-model-ru-0.22");
                 Recognizer recognizer = new Recognizer(model, 16000)) {
		
			File f = new File("/home/coshelev/vosk/");
			String[] list = f.list();
			for (int i  = 0;i < list.length; i++) {
				//System.out.println(list[i]);

				//exclude non-wave files
				if (list[i].contains(".wav")==false)
					continue;

				String inputFile = list[i];
				System.out.println("**"); System.out.print(inputFile); System.out.println("*****");			
			
			 	InputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream("/home/coshelev/vosk/"+inputFile)));	
				int nbytes;
        		   	 byte[] b = new byte[4096];
           		 	while ((nbytes = ais.read(b)) >= 0) {
                			if (recognizer.acceptWaveForm(b, nbytes)) {
                  		 		//System.out.println(recognizer.getResult());
               			 	} else {
                				//System.out.println(recognizer.getPartialResult());
					}
        	    		}

				var fR = recognizer.getFinalResult();
 				System.out.println(fR);

				String outputFile = inputFile.replace(".wav",".txt");
				//System.out.println("outputFile = "+outputFile);
				
				FileWriter fileWriter = new FileWriter("/home/coshelev/vosk/"+outputFile);
    				PrintWriter printWriter = new PrintWriter(fileWriter);
    				printWriter.print(fR);
    				printWriter.close();

 				inputMap.put("phone",           "vosk");
               			inputMap.put("signature",  	outputFile);
 				inputMap.put("extra",           fR);
               			requestBody = gson.toJson(inputMap);

               			var request = HttpRequest.newBuilder()
				.uri(URI.create("https://motor-luidor.ru/leads/chery-mworx/chery.php"))
                  		.POST(HttpRequest.BodyPublishers.ofString(requestBody))
                  		.header("accept", "application/json") 
                  		.build();
              
               			client.sendAsync(request,HttpResponse.BodyHandlers.ofString());
			};//for
	
        }
    }
	public static void WaveFileSplit() throws FileNotFoundException, IOException {
	
        	File f = new File("/home/coshelev/vosk/");
       		String[] list = f.list();
        	for (int i = 0; i < list.length; i++){
                	if (list[i].contains(".wav")==false)
                        	continue;

			 //String inputFile = list[i];
                         //System.out.println("**");
                         //System.out.println(inputFile);
                         //System.out.println("*****");

                	File input 		= new File("/home/coshelev/vosk/"+list[i]); 			
                	File outputLeft 	= new File("/home/coshelev/vosk/"+"l_"+list[i]); 	// Replace with the desired path for the left channel output file
                	File outputRight 	= new File("/home/coshelev/vosk/"+"r_"+list[i]); 	// Replace with the desired path for the right channel output file

			try (FileInputStream inputStream = new FileInputStream(input)){
				byte[] header = new byte[44]; 					// The header of a wave file is always 44 bytes long
            			inputStream.read(header); 					// Read the header bytes
            
            			int channels = header[22]; 					// Byte 23 of the header specifies the number of channels (1 for mono, 2 for stereo)
            
            			if (channels != 2) {
                			throw new IllegalArgumentException("Input file is not a stereo wave file.");
            			};
            
            			byte[] buffer = new byte[4]; 					// Buffer for reading 2 samples at a time (1 from each channel)

				try (FileOutputStream outputStreamLeft = new FileOutputStream(outputLeft);
					FileOutputStream outputStreamRight = new FileOutputStream(outputRight)){
					outputStreamLeft.write(header);
					outputStreamRight.write(header);

					while (inputStream.read(buffer) != -1) {
                    				outputStreamLeft.write(buffer, 0, 2); 			// Write the left channel sample
                    				outputStreamRight.write(buffer, 2, 2); 			// Write the right channel sample
                			}//while
				}//try
			}//try
        	}//for
	}//WaveFileSplit
}
