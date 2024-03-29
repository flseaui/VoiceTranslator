import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.PlugInManager;
import javax.media.format.AudioFormat;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.darkprograms.speech.microphone.Microphone;
import com.darkprograms.speech.recognizer.GoogleResponse;
import com.darkprograms.speech.recognizer.Recognizer;
import com.darkprograms.speech.synthesiser.Synthesiser;

import javaFlacEncoder.FLACFileWriter;


/**
 * Jarvis Speech API Tutorial
 * @author Aaron Gokaslan (Skylion)
 *
 */
public class Translator extends JFrame {
	
	Dictionary_Reader reader = new Dictionary_Reader();
	Graphics graphics = new Graphics();
	Debug debugWindow = new Debug();
	boolean google = true, debug = false;
	

	public static void main(String[] args) {
		new Translator();
	}
	
	public Translator() {	

		Format input1 = new AudioFormat(AudioFormat.MPEGLAYER3);
	    Format input2 = new AudioFormat(AudioFormat.MPEG);
	    Format output = new AudioFormat(AudioFormat.LINEAR);
	    PlugInManager.addPlugIn(
	        "com.sun.media.codec.audio.mp3.JavaDecoder",
	        new Format[]{input1, input2},
	        new Format[]{output},
	        PlugInManager.CODEC
	    );
		
		/*System.out.println("hello".subSequence(0, "hello".length()-2)+"test");

		Gson gson = new Gson();

		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("kek.json"), "UTF-8"));
			for (int i = 0; i < 10000; i++) {
				bw.write(gson.toJson(new Test(1, 2, 3)));
			}

		} catch (UnsupportedEncodingException | FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			 TODO Auto-generated catch block
			e.printStackTrace();
		}
		reader.indexer();
		System.out.println(reader.webWordCheck("hello"));
		reader.webRead("how", "en", "es");/*
		 */

		debugUpdate();
		if(debug)
			debugWindow.setVisible(true);
		else
			debugWindow.setVisible(false);

		//Spanish Dictionaries
		reader.spanishCleaner("Eng to Spn New.txt", reader.engSpnMap);
		reader.spnEngMap = reader.dictInverter(reader.engSpnMap);

		//French Dictionaries
		reader.frenchCleaner("Eng to Frn.txt", reader.engFrnMap);
		reader.frnEngMap = reader.dictInverter(reader.engFrnMap);

		//Non English Dictionaries
		reader.frenchCleaner("Spn to Frn.txt", reader.spnFrnMap);
		//reader.spnFrnMap = reader.dictMaker(reader.engSpnMap, reader.engFrnMap);
		//reader.write("Spn to Frn.txt", reader.spnFrnMap);

		reader.frnSpnMap = reader.dictInverter(reader.spnFrnMap);

		//Init window	 stuff
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(500, 500);
		setLayout(new GridLayout(3, 1));
		this.setResizable(true);
		this.add(graphics);

		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		this.setVisible(true);
	}

	public void record() {
		Microphone mic = new Microphone(FLACFileWriter.FLAC);
		File file = new File("testfile2.flac");//Name your file whatever you want
		try {
			mic.captureAudioToFile(file);
		} catch (Exception ex) {//Microphone not available or some other error.
			System.out.println("ERROR: Microphone is not availible.");
			ex.printStackTrace();
			//TODO Add your error Handling Here
		}
		/* User records the voice here. Microphone starts a separate thread so do whatever you want
		 * in the mean time. Show a recording icon or whatever.
		 */
		try {
			graphics.status.setText("Recording");
			graphics.repaint();
			System.out.println("Recording...");
			Thread.sleep(5000);//In our case, we'll just wait 5 seconds.
			mic.close();
		} catch (InterruptedException ex) {

			ex.printStackTrace();
		}

		mic.close();//Ends recording and frees the resources
		System.out.println("Recording stopped.");
		graphics.status.setText("Translating");
		graphics.repaint();
		if (google) {
			Recognizer recognizer = new Recognizer(toLang(graphics.curInLang), "AIzaSyD2o9tVprWInnns4lJUaq0NW05EwBf25v8"); //Specify your language here.
			//Although auto-detect is available, it is recommended you select your region for added accuracy.
			try {
				int maxNumOfResponses = 4;
				GoogleResponse response = recognizer.getRecognizedDataForFlac(file, maxNumOfResponses, (int)mic.getAudioFormat().getSampleRate());
				System.out.println("Google Response: " + response.getResponse());
				graphics.inText.setText("Input: " + response.getResponse());
				repaint();
				graphics.responses.removeAllItems();
				if (graphics.curInLang.equals(graphics.curOutLang))
					graphics.outText.setText("Output: " + response.getResponse());
				else
					graphics.outText.setText("Output: " + translate(response.getResponse().toLowerCase(), toISO(graphics.curOutLang)));
				graphics.no.setSelected(false);
				graphics.yes.setSelected(false);
				graphics.question.setVisible(true);
				graphics.yes.setVisible(true);
				graphics.no.setVisible(true);
				System.out.println(translate(response.getResponse(), toISO(graphics.curOutLang)));
				graphics.status.setText("Waiting");
				repaint();
				System.out.println("Google is " + Double.parseDouble(response.getConfidence())*100 + "% confident in"
						+ " the reply");
				System.out.println("Other Possible responses are: ");
				for(String s: response.getAllPossibleResponses()){
					graphics.addResponse(s);
					graphics.repaint();
					System.out.println("\t" + s);
				}
			} catch (Exception ex) {
				// TODO Handle how to respond if Google cannot be contacted
				System.out.println("ERROR: Google cannot be contacted");
				System.out.println("Please try speaking more clearly");
				ex.printStackTrace();
			}

			file.deleteOnExit();//Deletes the file as it is no longer necessary.
		}
	}

	public void talk(String text){
		//String language = "auto";//Uses language autodetection
		//** While the API can detect language by itself, this is reliant on the Google Translate API which is prone to breaking. For maximum stability, please specify the language.**//
		//System.out.println(graphics.curOutLang);
		String language = toISO(graphics.curOutLang);//English (US) language code	 //If you want to specify a language use the ISO code for your country. Ex: en-us
		/*If you are unsure of this code, use the Translator class to automatically detect based off of
		 * Either text from your language or your system settings.
		 */
		Synthesiser synth = new Synthesiser(language);
		try {
			InputStream is = synth.getMP3Data(text);
			final Path destination = Paths.get("audio.mp3");
			Files.copy(is, destination, StandardCopyOption.REPLACE_EXISTING);
			Player p = Manager.createRealizedPlayer(new MediaLocator(new File("audio.mp3").toURI().toURL()));
			p.start();
			Thread.sleep((p.getMediaTime().getNanoseconds()/1000000));
		} catch (Exception e) {
			System.out.println("Error");
			e.printStackTrace();
			return;
		}
	}

	public String translate(String text, String lang) {
		//output = english
		if (lang == "en-us") {
			String[] words = text.split(" ");
			String[] newWords = new String[words.length];
			if (graphics.curInLang == "Spanish"){
				for(int i = 0; i < words.length; i++) {
					if (reader.spnEngMap.get(words[i]) == null){
						newWords[i] = words[i];
					} else {
						newWords[i] = reader.spnEngMap.get(words[i]);
					}
				}
			} else if (graphics.curInLang == "French"){
				for(int i = 0; i < words.length; i++) {
					if (reader.frnEngMap.get(words[i]) == null){
						newWords[i] = words[i];
					} else { 
						newWords[i] = reader.frnEngMap.get(words[i]);
					}
				}
			}
			String newSentence = "";
			for(String word : newWords) {
				newSentence += word + " ";
			}
			talk(newSentence);
			return newSentence;
			//output = spanish
		} else if (lang == "es-mx") {
			String[] words = text.split(" ");
			String[] newWords = new String[words.length];
			if (graphics.curInLang == "English"){
				for(int i = 0; i < words.length; i++) {
					if (reader.engSpnMap.get(words[i]) == null){
						newWords[i] = words[i];
					} else {
						newWords[i] = reader.engSpnMap.get(words[i]);
					}
				}
			} else if (graphics.curInLang == "French"){
				for(int i = 0; i < words.length; i++) {
					if (reader.frnSpnMap.get(words[i]) == null){
						newWords[i] = words[i];
					} else {
						newWords[i] = reader.frnSpnMap.get(words[i]);
					}
				}
			}
			String newSentence = "";
			for(String word : newWords) {
				newSentence += word + " ";
			}
			talk(newSentence);
			return newSentence;
			//output = french
		} else if (lang == "fr-fr") {
			String[] words = text.split(" ");
			String[] newWords = new String[words.length];
			if (graphics.curInLang == "English"){
				for(int i = 0; i < words.length; i++) {
					if (reader.engFrnMap.get(words[i]) == null){
						newWords[i] = words[i];
					} else if (words[i].substring(words[i].length() - 2).equals("'s")){
						String[] longWords = new String[words.length + 1];
						for (int x = 0; x < longWords.length; x ++){
							if (x == i){
								longWords[x] = (words[i].subSequence(0, words[i].length() - 2)).toString();
								longWords[x + 1] = "is";
								x++;
							}
							longWords[x] = words[x];

						}
					} else if (words[i].substring(words[i].length() - 3).equals("'ve")){
						String[] longWords = new String[words.length + 1];
						for (int x = 0; x < longWords.length; x ++){
							if (x == i){
								longWords[x] = (words[i].subSequence(0, words[i].length() - 3)).toString();
								longWords[x + 1] = "have";
								x++;
							}
							longWords[x] = words[x];

						}
					} else {
						newWords[i] = reader.engFrnMap.get(words[i]);
					}
				}
			} else if (graphics.curInLang == "Spanish"){
				for(int i = 0; i < words.length; i++) {
					if (reader.spnFrnMap.get(words[i]) == null){
						newWords[i] = words[i];
					} else {
						newWords[i] = reader.spnFrnMap.get(words[i]);
					}
				}
			}

			String newSentence = "";
			for(String word : newWords) {
				newSentence += word + " ";
			}
			talk(newSentence);
			return newSentence;
		}
		return text;
	}

	public String toISO(String lang) {
		switch (lang) {
		case "English":
			return "en-us";
		case "Spanish":
			return "es-mx";
		case "French":
			return "fr-fr";
		}
		return lang;
	}

	public Recognizer.Languages toLang(String lang) {
		switch(lang) {
		case "English":
			return Recognizer.Languages.ENGLISH_US;
		case "Spanish":
			return Recognizer.Languages.SPANISH_MEXICO;
		case "French":
			return Recognizer.Languages.FRENCH;
		}
		return null;

	}

	public class Graphics extends JPanel {

		GridBagConstraints gbc = new GridBagConstraints();

		JComboBox<String> inLang = new JComboBox<String>(new String[]{"English", "Spanish", "French"});
		JComboBox<String> outLang = new JComboBox<String>(new String[]{"Spanish", "English", "French"});
		JComboBox<String> responses = new JComboBox<String>(new String[]{});
		JButton record = new JButton("Record");
		JButton play = new JButton("Play");
		JLabel to = new JLabel("to");
		JLabel status = new JLabel("Waiting...");
		JLabel inText = new JLabel("Input: ");
		JLabel outText = new JLabel("Output: ");
		JLabel question = new JLabel("Is this what you said?");
		JLabel followup = new JLabel("Was it one of these?");
		JCheckBox yes = new JCheckBox("yes");
		JCheckBox no = new JCheckBox("no");

		String curOutLang = "Spanish", curInLang = "English";

		public void addResponse(String s) {
			responses.addItem(s);
		}

		public Graphics() {
			setLayout(new GridBagLayout());

			this.setBorder(BorderFactory.createEmptyBorder());

			inLang.setSelectedIndex(0);
			outLang.setSelectedIndex(0);

			inLang.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					curInLang = inLang.getItemAt(inLang.getSelectedIndex());
					debugUpdate();
				}

			});

			outLang.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					curOutLang = outLang.getItemAt(outLang.getSelectedIndex());
					debugUpdate();
				}

			});

			record.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					record();
				}

			});

			play.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

				}

			});

			yes.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					AbstractButton ab = (AbstractButton) e.getSource();
					ButtonModel bm = ab.getModel();
					if (bm.isSelected()) {
						no.setEnabled(false);
						question.setVisible(false);
						no.setVisible(false);
						yes.setVisible(false);

					} else {
						no.setEnabled(true);
					}
				}

			});

			no.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					AbstractButton ab = (AbstractButton) e.getSource();
					ButtonModel bm = ab.getModel();
					if (bm.isSelected()) {
						yes.setEnabled(false);
						followup.setVisible(true);
						responses.setVisible(true);
						responses.setEnabled(true);
					} else {
						yes.setEnabled(true);
						followup.setVisible(false);
						responses.setVisible(false);
					}
				}

			});

			responses.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					System.out.println("state");
					if (e.getStateChange() == ItemEvent.SELECTED) {
						System.out.println("selected");
						no.setSelected(false);
						yes.setSelected(false);
						inText.setText("Input: " + responses.getItemAt(responses.getSelectedIndex()));
						//graphics.responses.removeAllItems();
						if (curInLang.equals(graphics.curOutLang))
							outText.setText("Output: " + responses.getItemAt(responses.getSelectedIndex()));
						else
							graphics.outText.setText("Output: " + translate(responses.getItemAt(responses.getSelectedIndex()).toLowerCase(), toISO(graphics.curOutLang)));
					}
					/*question.setVisible(false);
					followup.setVisible(false);
					yes.setVisible(false);
					no.setVisible(false);
					responses.setVisible(false);*/
				}	
			});

			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			add(inLang, gbc);
			gbc.fill = GridBagConstraints.CENTER;
			gbc.gridx = 1;
			gbc.gridy = 0;
			add(to, gbc);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 2;
			gbc.gridy = 0;
			add(outLang, gbc);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = 2;
			add(status, gbc);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = 1;
			add(record, gbc);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 3;
			add(inText, gbc);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 4;
			add(outText, gbc);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 5;
			add(question, gbc);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = 5;
			add(yes, gbc);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 2;
			gbc.gridy = 5;
			add(no, gbc);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 6;
			add(followup, gbc);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = 6;
			add(responses, gbc);

			inLang.setVisible(true);
			outLang.setVisible(true);
			to.setVisible(true);
			record.setVisible(true);
			inText.setVisible(true);
			outText.setVisible(true);
			question.setVisible(false);
			yes.setVisible(false);
			no.setVisible(false);
			responses.setVisible(false);
			responses.setEnabled(false);
			followup.setVisible(false);
		}

	}

	public void debugUpdate() {
		debugWindow.text(graphics.curInLang, graphics.curOutLang, toISO(graphics.curInLang), toISO(graphics.curInLang), toLang(graphics.curInLang).toString(), toLang(graphics.curOutLang).toString());
		debugWindow.repaint();
	}

	public class Debug extends JFrame {

		JPanel gfx = new JPanel();

		public Debug() {
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setSize(200, 500);
			setLayout(new GridLayout(10, 1));
			setResizable(true);
			add(gfx);
			setVisible(true);
		}

		public void text(String... vars) {
			for(int i = 0; i < vars.length; i++) {
				JLabel text = new JLabel(vars[i]);
				gfx.add(text);
				text.setVisible(true);
			}
		}

	}

}