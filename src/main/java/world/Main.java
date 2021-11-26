package world;

import general.useful.PropertiesFile;
import general.useful.PropertyTypeEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Main {
    /*in case of error remove all
        log4j-to-slf4j-2.0.2.jar
        log4j-to-slf4j-2.0.2-sources.jar
        log4j-slf4j-impl-2.0.2.jar
        log4j-slf4j-impl-2.0.2-sources.jar
    */
    private static final Logger logger = LogManager.getLogger(Main.class);
    public final static double GAMA = 0.95;
    public static double UtilityForEndState ;
    public static double TOLERANCE ;
    public static PropertiesFile propertiesFile;
    public static boolean generateReports;

    /**
     * 1. Go to generic.world.Main see the TODOs.
     * 2. Go to generic.World see the TODOs .
     * 3. Go to generic.Environment see the TODOs located at the freeSpot method.
     *
     * @see Main
     * @see World generic.World()
     * @see Environment freeSpot()
     */
    public static void main(String[] args) throws Exception{

        String pathToProperties = "config.properties";
        JFileChooser fc = new JFileChooser();
        if(!(new File(pathToProperties)).exists())
            switch (fc.showOpenDialog(null)) {
                case JFileChooser.APPROVE_OPTION:
                    try {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                            | UnsupportedLookAndFeelException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
                    pathToProperties = fc.getSelectedFile().getAbsolutePath();
                    break;
            }
        // We need to provide file path as the parameter:
        // double backquote is to avoid compiler interpret words
        // like \test as \t (ie. as a escape sequence)
        File file = new File(pathToProperties);

        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;
        propertiesFile= new PropertiesFile();
        while ((st = br.readLine()) != null) {
            String[] property = st.split("=");
            System.out.println(property[0]);
            System.out.println(PropertyTypeEnum.NUMBER_OF_ITERATIONS_BEFORE_PI.name());
            if (property[0].equals(PropertyTypeEnum.NUMBER_OF_ITERATIONS_BEFORE_PI.name())){
                System.out.println(PropertyTypeEnum.NUMBER_OF_ITERATIONS_BEFORE_PI.name());
                propertiesFile.getProperties().put(PropertyTypeEnum.NUMBER_OF_ITERATIONS_BEFORE_PI, property[1].trim());
            }else if(property[0].equals(PropertyTypeEnum.WORLD_HEIGHT.name())){
                propertiesFile.getProperties().put(PropertyTypeEnum.WORLD_HEIGHT, property[1].trim());
            }else if (property[0].equals(PropertyTypeEnum.WORLD_WIDTH.name())){
                propertiesFile.getProperties().put(PropertyTypeEnum.WORLD_WIDTH, property[1].trim());
            }else if (property[0].equals(PropertyTypeEnum.NUMBER_OF_ITERATIONS_FOR_SAME_WORLD.name())){
                propertiesFile.getProperties().put(PropertyTypeEnum.NUMBER_OF_ITERATIONS_FOR_SAME_WORLD, property[1].trim());
            }else if (property[0].equals(PropertyTypeEnum.OBJECT_ANALOGY.name())){
                propertiesFile.getProperties().put(PropertyTypeEnum.OBJECT_ANALOGY, property[1].trim());
            }else if (property[0].equals(PropertyTypeEnum.NUMBER_OF_PARALLEL_AGENTS.name())){
                propertiesFile.getProperties().put(PropertyTypeEnum.NUMBER_OF_PARALLEL_AGENTS, property[1].trim());
            }else if (property[0].equals(PropertyTypeEnum.END_STATE_X.name())){
                propertiesFile.getProperties().put(PropertyTypeEnum.END_STATE_X, property[1].trim());
            }else if (property[0].equals(PropertyTypeEnum.END_STATE_Y.name())){
                propertiesFile.getProperties().put(PropertyTypeEnum.END_STATE_Y, property[1].trim());
            }else if (property[0].equals(PropertyTypeEnum.POLICY_ACCURACY.name())){
                propertiesFile.getProperties().put(PropertyTypeEnum.POLICY_ACCURACY, property[1].trim());
            }else if (property[0].equals(PropertyTypeEnum.REPORTING_PATH_FOR_FILES.name())){
                generateReports = !property[1].trim().isEmpty();
                propertiesFile.getProperties().put(PropertyTypeEnum.REPORTING_PATH_FOR_FILES, property[1].trim() + "\\Run"+  new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+"\\");
            }else if (property[0].equals(PropertyTypeEnum.REPORTING_ON.name())){
                propertiesFile.getProperties().put(PropertyTypeEnum.REPORTING_ON, property[1].trim());
            }
        }

        if (generateReports) {
            Path path = Paths.get(propertiesFile.getProperties().get(PropertyTypeEnum.REPORTING_PATH_FOR_FILES));
            //if directory exists?
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    //fail to create directory
                    e.printStackTrace();
                }
            }
        }



        TOLERANCE = 2;
        UtilityForEndState = (Integer.parseInt(propertiesFile.getProperties().get(PropertyTypeEnum.WORLD_HEIGHT))+
                Integer.parseInt(propertiesFile.getProperties().get(PropertyTypeEnum.WORLD_WIDTH))) * 100 ;

        World world = new World(Integer.parseInt(propertiesFile.getProperties().get(PropertyTypeEnum.WORLD_WIDTH)),
                Integer.parseInt(propertiesFile.getProperties().get(PropertyTypeEnum.WORLD_HEIGHT)),
                Integer.parseInt(propertiesFile.getProperties().get(PropertyTypeEnum.OBJECT_ANALOGY)),
                Integer.parseInt(propertiesFile.getProperties().get(PropertyTypeEnum.END_STATE_X)),
                Integer.parseInt(propertiesFile.getProperties().get(PropertyTypeEnum.END_STATE_Y)) );
//        ObjectMapper mapper = new ObjectMapper();
        int i = 0;
        //TODO this by default runs ADP agent because TD agent not working
        while (i < Integer.parseInt(propertiesFile.getProperties().get(PropertyTypeEnum.NUMBER_OF_ITERATIONS_FOR_SAME_WORLD))) {
            world.initWorld(Integer.parseInt(propertiesFile.getProperties().get(PropertyTypeEnum.NUMBER_OF_ITERATIONS_BEFORE_PI)),
                    Integer.parseInt(propertiesFile.getProperties().get(PropertyTypeEnum.NUMBER_OF_PARALLEL_AGENTS)),i++, false);
//               try {
//                // Convert object to JSON string
//                String jsonInString = mapper.writeValueAsString(world);
//                System.out.println(jsonInString);
//                DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
//                Date dat = new Date();
//                String d = dateFormat.format(dat)+"_"+i+"_";
//                mapper.writeValue(new File("./src/html/runs/"+ d+"bestActions.json"), World.policy);
//                mapper.writeValue(new File("./src/html/runs/"+ d+"properties.json"), propertiesFile);
//                mapper.writeValue(new File("./src/html/runs/"+ d+"env.json"), World.env);
//                mapper.writeValue(new File("./src/html/runs/"+ d+"model.json"), Environment.getModel());
////                mapper.writeValue(new File("./src/view/runs/"+ d+"nSA.json"), world.nSA);
////                mapper.writeValue(new File("./src/view/runs/"+ d+"nSAS.json"), world.nSAS);
//                // Convert object to JSON string and pretty print
//                jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(world);
////                System.out.println(jsonInString);
//                Path path = Paths.get("./src/html/assets/js/initmyjs.js");
//                Charset charset = StandardCharsets.UTF_8;
////
//                String content = new String(Files.readAllBytes(path), charset);
//                //  	content = content.replace("'put text here'", gson.toJson(profsPubl));
//                content = content.replace("textGoesHere", "./runs/"+ d);
//                Path jsPath = Paths.get("./src/html/assets/js/myjs.js");
//                Files.write(jsPath, content.getBytes());
//            } catch (JsonGenerationException e) {
//                e.printStackTrace();
//            } catch (JsonMappingException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
        logger.info("Hi How r u?");

    }

    static void playClip(File clipFile) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
        class AudioListener implements LineListener {
            private boolean done = false;
            @Override public synchronized void update(LineEvent event) {
                LineEvent.Type eventType = event.getType();
                if (eventType == LineEvent.Type.STOP || eventType == LineEvent.Type.CLOSE) {
                    done = true;
                    notifyAll();
                }
            }
            public synchronized void waitUntilDone() throws InterruptedException {
                while (!done) { wait(); }
            }
        }
        AudioListener listener = new AudioListener();
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(clipFile);
        try {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(listener);
            clip.open(audioInputStream);
            try {
                clip.start();
                listener.waitUntilDone();
            } finally {
                clip.close();
            }
        } finally {
            audioInputStream.close();
        }
        System.exit(0);
    }

}
