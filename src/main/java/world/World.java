package world;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import general.*;
import general.useful.Paint;
import general.useful.PropertyTypeEnum;
import general.useful.ReportFileEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import theTwo.Agent;
import theTwo.PassiveADPAgent;
import theTwo.PolicyIteration;
import theTwo.TDAgent;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;

/**
 *
 */
public class World {
    private static Logger logger = LogManager.getLogger(World.class.getName());
    private static Environment env;
    private ArrayList<Agent> agents = new ArrayList<>(1);
    private ArrayList<TDAgent> tdAgents = new ArrayList<>(1);
    public static PassiveADPAgent currentAgent;
    public int adpLoop;
    public int piLoop;
    private HashMap<Agent, Thread> threads = new HashMap<>();
    private HashMap<Integer, Boolean> runIsRunning = new HashMap<>();
    private HashMap<State, HashMap<ActionName, Integer>> nSA = new HashMap<>();
    //HashMap<HashMap<currentState, nextState>, HashMap<ActionName, numberOfVisits>>
    private HashMap<Map<State, State>, HashMap<ActionName, Integer>> nSAS = new HashMap<>();
    protected static int columns;
    private static int rows;
    private static int runNumber;
    private static int objectAnalogy;
    private static int numOfSamplePolicies;
    private static List<String> utilityReports = new ArrayList<>();
    protected static Policy policy;
    static State startState;
    private static XYSeriesCollection evaluationDataset = new XYSeriesCollection();
    private static XYSeriesCollection numbersDataset = new XYSeriesCollection();

    int numOfPolicies = 0;
    String time = " ";
    long timeInMillis = 0L;
    int grade = 0;

    //TD AGENT
    private HashMap<State, Double> stateFrequencyVisits = new HashMap<>();
    private HashMap<State, Integer> stateNumberVisits = new HashMap<>();
    private Integer sumOfAllStateVisits = 0;
    private HashMap<State, Double> calculatedU = new HashMap<>();
    private HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> model = new HashMap<>();


    public World() {

    }

    public World(int cols, int rows, int objects, int endStateX, int endStateY) {
        super();
        World.columns = rows;
        World.rows = cols;
        objectAnalogy = objects;
        // I put an amount of obstacles equal to the average of tiles divided by 2.
        int numOfObst = (int) (World.columns * World.rows) / objects;
        // TODO initiate End oldStuff.State (two last parameters of generic.Environment constructor
        World.env = new Environment(rows, cols, numOfObst, endStateX, endStateY);
    }

    void initWorld(int numOfSamplePolicies, int numOfParallelAgents, int runNumber, boolean runTD) throws Exception {
        Environment.setCurrentStatesOfAgents(new HashMap<>());
        if (runNumber < 1) {
            Paint pw = new Paint(rows, columns);
            printObstaclesTable(pw.table1, env);
            pw.setTitle("Init");
            pw.setVisible(true);
        }
        runIsRunning.put(runNumber, false);
        World.runNumber = runNumber;
        World.numOfSamplePolicies = numOfSamplePolicies;
        for (int i = 0; i < numOfParallelAgents; i++) {
            Policy policy = randomPolicy(Environment.getModel());
            startState = policy.getInitState();
            Agent ah;
            if (runTD) {
                ah = new TDAgent(policy, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), 0);
                run(ah, numOfSamplePolicies, runNumber);
                stateFrequencyVisits = ((TDAgent) ah).getStateFrequencyVisits();
                stateNumberVisits = ((TDAgent) ah).getStateNumberVisits();
                sumOfAllStateVisits = ((TDAgent) ah).getSumOfAllStateVisits();
            } else {
                ah = new PassiveADPAgent(policy, 1, model, calculatedU, nSA, nSAS);
                run(ah, numOfSamplePolicies, runNumber);
                nSA = ((PassiveADPAgent) ah).getStateActionVisits();
                nSAS = ((PassiveADPAgent) ah).getStateActionStateVisits();
                calculatedU = ((PassiveADPAgent) ah).getUtilities();
                model = ((PassiveADPAgent) ah).getMdp();
            }
            // not useful here
            //     Environment.getCurrentStatesOfAgents().put(ah.getAgent(), policy.getInitState());
            Thread agent = new Thread(ah);
            agent.setDaemon(true);
            agent.setName("Agent-" + i);
            agent.start();
            agents.add(ah);
            threads.put(ah, agent);
        }
        try {
            if (Main.generateReports) printReport();
            Main.playClip(new File(World.class.getClassLoader().getResource("done.wav").getPath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //TODO stop it from running too many times simultaneously
    }

    /**
     * Normalize x.
     *
     * @param x The value to be normalized.
     * @return The result of the normalization.
     */
    @Deprecated
    public double normalize(double x, double dataLow, double dataHigh) {
        return ((x - dataLow)
                / (dataHigh - dataLow))
                * (100);
    }


    private static void printObstaclesTable(JTable table, Environment env) {
        for (Obstacle s : env.getObstacles()) {
            table.setValueAt(s.getId(), /*columns - 1 -*/ s.getStateProperties().get(AxisName.Y), s.getStateProperties().get(AxisName.X));
        }
        table.setValueAt("E", /*columns -1 - */Environment.getEndState().getStateProperties().get(AxisName.Y), Environment.getEndState().getStateProperties().get(AxisName.X));
    }

    private Policy randomPolicy(HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> model) {
        HashMap<State, ActionName> bestActions = new HashMap<>();
        for (State state : model.keySet()) {
            bestActions.put(state, randomActionForState());
            if (Environment.isEndState(state))
                state.setUtility(Main.UtilityForEndState);
        }
        // TODO set init State
        Policy policy = new Policy(new State(0, 0));
        policy.setBestActions(bestActions);
        return policy;
    }

    private ActionName randomActionForState() {
        Random r = new Random();
        return ActionName.getById(r.nextInt(ActionName.getCountOfActions()));
    }

    public void run(Agent agent, int numOfSamplePolicies, int runNumber) throws Exception {
        XYSeries xySeriesStateAction = new XYSeries("State-Action Visits");
        XYSeries xySeriesStateActionState = new XYSeries("State-Action-State Visits");
        XYSeries xySeries = new XYSeries("");

        Policy policy = randomPolicy(Environment.getModel());
        Document doc = null;
        if (Main.generateReports) {
            try {
                doc = createPoliciesReport();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Paint pw1 = new Paint(World.rows, World.columns);
        pw1.setVisible(true);
        pw1.setTitle(Thread.currentThread().getName() + "    " + World.runNumber);
        pw1.printPolicyOnResultTable(pw1.table1, policy);
        int iterations = Integer.parseInt(Main.propertiesFile.getProperties().get(PropertyTypeEnum.POLICY_ACCURACY));
        int adpLoop = 1;
        int piLoop = 1;

        List<String> allCalculatedPolicies = new ArrayList<>();
        List<String> lastCalculatedPolicies = new ArrayList<>();
        for (int i = 0; i < iterations; i++)
            lastCalculatedPolicies.add("");
        LocalDateTime tempDateTime = LocalDateTime.now();
        boolean isChanged;
        int lastRemovedPolicy = 0;
        do {
            do {
                System.out.format("generic.Policy initiating from state: S%s \n", policy.getInitState().getId());
                // TODO change the bellow values to macth the positions of the agents
                logger.info("*********** PASSIVE ADP " + adpLoop + " ***********");
                //             pwAgent.setTitle("*********** PASSIVE ADP " + k + " ***********");
                if (agent instanceof PassiveADPAgent) {
                    PassiveADPAgent adpAgent;
//                    adpAgent = new PassiveADPAgent(policy, loopNumber,  model, calculatedU, nSA, nSAS);
                    adpAgent = new PassiveADPAgent(policy, model, calculatedU, nSA, nSAS);
                    adpAgent.run();
                    calculatedU = adpAgent.getUtilities();
                    model = adpAgent.getMdp();
//                    loopNumber = adpAgent.getLoopNumber();
                    nSA = adpAgent.getStateActionVisits();
                    nSAS = adpAgent.getStateActionStateVisits();
                    if (Main.generateReports)
                        xySeriesStateAction.add(piLoop, nSA.values().stream().mapToDouble(hm -> hm.values().stream().mapToDouble(x -> x + 0d).average().getAsDouble()).average().getAsDouble());
                    if (Main.generateReports)
                        xySeriesStateActionState.add(piLoop, nSAS.values().stream().mapToDouble(hm -> hm.values().stream().mapToDouble(x -> x + 0d).average().getAsDouble()).average().getAsDouble());

                } else {
                    TDAgent tdAgent;
                    tdAgent = new TDAgent(policy, model, calculatedU, stateNumberVisits, stateFrequencyVisits, sumOfAllStateVisits);
                    tdAgent.run();
                    calculatedU = tdAgent.getUtilities();
                    model = tdAgent.getMdp();
                    stateFrequencyVisits = tdAgent.getStateFrequencyVisits();
                    sumOfAllStateVisits = tdAgent.getSumOfAllStateVisits();
                    stateNumberVisits = tdAgent.getStateNumberVisits();
                    //NO MODEL IS CALCULATED FOR TD
                    for (State s : stateNumberVisits.keySet())
                        model.put(s, null);

                    throw new Exception("Invalid Agent");
                }
                adpLoop++;

            } while (adpLoop < numOfSamplePolicies);

            if (Main.generateReports) xySeries.add(piLoop, grade = Environment.evaluatePolicy(policy));
            adpLoop = 0;
            logger.info("*********** POLICY ITERATION " + ++piLoop + "***********");
            System.out.println("*********** POLICY ITERATION " + piLoop + "***********");
            this.piLoop = piLoop;
            PolicyIteration pI = new PolicyIteration(model, calculatedU);
            policy = pI.policyIteration(policy);
            String policyStr = policy.toLineString();
            isChanged = !lastCalculatedPolicies.stream().allMatch(x -> x.equals(policyStr));
            lastCalculatedPolicies.add(policy.toLineString());
            allCalculatedPolicies.add(policy.toLineString());
            if (lastCalculatedPolicies.size() > iterations) {
                lastCalculatedPolicies.remove(lastRemovedPolicy++);
                if (lastRemovedPolicy >= iterations)
                    lastRemovedPolicy = 0;
            }
            World.policy = policy;
            if (Main.generateReports) {
//                World.appendStrToFile(Main.propertiesFile.getProperties().get(PropertyTypeEnum.REPORTING_PATH_FOR_FILES) + ReportFileEnum.UTILITIES.getFilename(),
//                        piLoop + "=" + printUtilities(calculatedU));
                utilityReports.add(piLoop + "=" + printUtilities(calculatedU));
                try {
                    addTableToPoliciesReport(doc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            pw1.printPolicyOnResultTable(pw1.table1, policy);
        } while (isChanged);
        LocalDateTime toDateTime = LocalDateTime.now();
        timeInMillis = tempDateTime.until(toDateTime, ChronoUnit.MILLIS);
        long years = tempDateTime.until(toDateTime, ChronoUnit.YEARS);
        tempDateTime = tempDateTime.plusYears(years);

        long months = tempDateTime.until(toDateTime, ChronoUnit.MONTHS);
        tempDateTime = tempDateTime.plusMonths(months);

        long days = tempDateTime.until(toDateTime, ChronoUnit.DAYS);
        tempDateTime = tempDateTime.plusDays(days);

        long hours = tempDateTime.until(toDateTime, ChronoUnit.HOURS);
        tempDateTime = tempDateTime.plusHours(hours);

        long minutes = tempDateTime.until(toDateTime, ChronoUnit.MINUTES);
        tempDateTime = tempDateTime.plusMinutes(minutes);

        long seconds = tempDateTime.until(toDateTime, ChronoUnit.SECONDS);
        tempDateTime = tempDateTime.plusSeconds(seconds);

        long milis = tempDateTime.until(toDateTime, ChronoUnit.MILLIS);

        if (Main.generateReports) numbersDataset.addSeries(xySeriesStateAction);
        if (Main.generateReports) numbersDataset.addSeries(xySeriesStateActionState);
        if (Main.generateReports) evaluationDataset.addSeries(xySeries);
        numOfPolicies = allCalculatedPolicies.size();

        int policyGrade = Environment.evaluatePolicy(policy);

        time =
//                years + " years " +
//                months + " months " +
                days + " days " +
                        hours + " hours " +
                        minutes + " minutes " +
                        seconds + " seconds " +
                        milis + " milis.";

        logger.info(Thread.currentThread().getName() + " ----> Time elapsed for " + numOfSamplePolicies +
                ": " + time + " nano"
                + "  allCalculatedPolicies.size:" + allCalculatedPolicies.size());
        System.out.println(Thread.currentThread().getName() + " ----> Time elapsed for " + numOfSamplePolicies + ": "
                + time + "  allCalculatedPolicies.size:" + allCalculatedPolicies.size());
        logger.info("This policy's grade: " + policyGrade + " out of " + Environment.MAX_POLICY_GRADE);
        System.out.println("This policy's grade: " + policyGrade + " out of " + Environment.MAX_POLICY_GRADE);
        //----------------------PRINTING----------------------------

        pw1.printPolicyOnResultTable(pw1.table1, policy);
        try {
            closePoliciesReport(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String printUtilities(HashMap<State, Double> utilities) {
        StringBuilder str = new StringBuilder();
        for (State s : utilities.keySet()) {
            str.append(s).append(":").append(utilities.get(s)).append(";");
        }
        str.append("\n");
        return str.toString();
    }

    private Document createPoliciesReport() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("_yyyy-MM-dd_HH.mm.ss");
        DateTimeFormatter formatterTilte = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String DEST = Main.propertiesFile.getProperties().get(PropertyTypeEnum.REPORTING_PATH_FOR_FILES) + ReportFileEnum.POLICIES.getFilename();

        Document document = new Document();

        PdfWriter writer = PdfWriter.getInstance(
                document, new FileOutputStream(DEST));
        document.open();

        writer.setCompressionLevel(0);
        return document;
    }


    private void addTableToPoliciesReport(Document document) throws Exception {
        document.add(new Paragraph("Loop " + piLoop));
        PdfPTable table = createTable();
        table.setPaddingTop(40);
        document.add(table);
        document.add(new Paragraph(""));


    }

    private void closePoliciesReport(Document document) throws Exception {
        document.close();
    }


    private void printReport() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("_yyyy-MM-dd_HH.mm.ss");
        DateTimeFormatter formatterTilte = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String DEST = Main.propertiesFile.getProperties().get(PropertyTypeEnum.REPORTING_PATH_FOR_FILES) + ReportFileEnum.RUN_REPORT.getFilename() + LocalDateTime.now().format(formatter) + ".pdf";

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(
                document, new FileOutputStream(DEST));
        document.open();

        Font titleFont = new Font();
        titleFont.setColor(BaseColor.DARK_GRAY);
        titleFont.setFamily(Font.FontFamily.HELVETICA.name());
        titleFont.setStyle(Font.BOLD);
        titleFont.setSize(20);

        Font textFont = new Font();
        textFont.setColor(BaseColor.DARK_GRAY);
        textFont.setFamily(Font.FontFamily.HELVETICA.name());
        textFont.setSize(12);

        Font textFonLight = new Font(textFont);
        textFonLight.setColor(BaseColor.LIGHT_GRAY);

        document.add(new Paragraph("Report " + LocalDateTime.now().format(formatterTilte), titleFont));

        document.add(new Paragraph("Time elapsed for " + numOfPolicies + " Policy Iterations and " + numOfSamplePolicies + " ADP runs before each Policy Iteration:", textFont));
        document.add(new Paragraph("" + time, textFont));
        document.add(new Paragraph("Height:  " + rows + ", Width:" + columns, textFonLight));

        document.add(new Paragraph("This policy's score: " + grade + " out of " + Environment.MAX_POLICY_GRADE, textFont));
        document.add(new Paragraph("Object Analogy: 1/" + objectAnalogy, textFonLight));

        document.add(new Paragraph(" "));

        PdfPTable table = createTable();
        table.setPaddingTop(40);
        document.add(table);
        int width = 500;
        int height = 250;

        XYSeriesCollection averageUtilityXYSeries = new XYSeriesCollection();
        XYSeries loopUtilityXYSeries = new XYSeries("loop utility");
        HashMap<Integer, HashMap<String, Double>> allutilities = new HashMap<>();
        utilityReports.forEach(line -> {
            appendStrToFile(Main.propertiesFile.getProperties().get(PropertyTypeEnum.REPORTING_PATH_FOR_FILES) + ReportFileEnum.UTILITIES.getFilename(), line);
            HashMap<String, Double> utilitiesOfLine = new HashMap<>();
            Arrays.stream(line.split("=")[1].split(";")).forEach(item -> {
                if (!item.trim().isEmpty())
                    utilitiesOfLine.put(item.split(":")[0], Double.parseDouble(item.split(":")[1]));
            });
            allutilities.put(Integer.parseInt(line.split("=")[0]), utilitiesOfLine);
            loopUtilityXYSeries.add(Integer.parseInt(line.split("=")[0]), utilitiesOfLine.values().stream().mapToDouble(x -> x)
                    .average()
                    .orElse(Double.NaN));

        });


        averageUtilityXYSeries.addSeries(loopUtilityXYSeries);

        // DIAGRAM 1

        JFreeChart chart1 = ChartFactory.createXYLineChart("Average Utilities per Loop", "PI loop number", "Average Utility", averageUtilityXYSeries, PlotOrientation.HORIZONTAL, true, false,false);
        BufferedImage objBufferedImage1 = chart1.createBufferedImage(width, height);
        ByteArrayOutputStream bas1 = new ByteArrayOutputStream();
        try {
            ImageIO.write(objBufferedImage1, "png", bas1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] byteArray1 = bas1.toByteArray();
        document.add(Image.getInstance(byteArray1));

        // DIAGRAM 2
        HashMap<String, HashMap<Integer, Double>> allutilitiesForEachState = new HashMap<>();
        allutilities.forEach((key, value) -> value.forEach((key1, value1) -> {
            allutilitiesForEachState.putIfAbsent(key1, new HashMap<>());
            allutilitiesForEachState.computeIfPresent(key1, (state, util) -> {
                util.put(key, value.get(state));
                return util;
            });
        }));

        XYSeriesCollection averageUtilityXYSeriesStates = new XYSeriesCollection();
        allutilitiesForEachState.forEach((k, v) -> {
            XYSeries stateUtilityXYSeries = new XYSeries("state" + k);
            if (Integer.parseInt(k.substring(1, k.lastIndexOf(","))) == Integer.parseInt(k.substring(k.lastIndexOf(",") + 1, k.length() - 1))) {
                v.forEach(stateUtilityXYSeries::add);
                averageUtilityXYSeriesStates.addSeries(stateUtilityXYSeries);
            }

        });
        JFreeChart chart4 = ChartFactory.createXYLineChart("Utilities of States and their progress per PI loop", "PI loop number", "Utility of State", averageUtilityXYSeriesStates, PlotOrientation.HORIZONTAL, true, false,false);
        NumberAxis rangeAxis4 = new NumberAxis("Utility of State");

        XYPlot plot4 = (XYPlot) chart4.getPlot();
        plot4.setRangeAxis(rangeAxis4);

        ChartPanel cp = new ChartPanel(chart4);

        BufferedImage objBufferedImage4 = new BufferedImage(500 , 500,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = objBufferedImage4.createGraphics();
        chart4.draw(g2, new Rectangle2D.Double(0, 0, 500 , 500), null, null);
       ByteArrayOutputStream bas4 = new ByteArrayOutputStream();
        try {
            ImageIO.write(objBufferedImage4, "png", bas4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] byteArray4 = bas4.toByteArray();
        document.add(Image.getInstance(byteArray4));
        g2.dispose();
        // DIAGRAM 3
        JFreeChart chart = ChartFactory.createXYLineChart("Policy Score per Loop", "PI loop number", "Policy Score", evaluationDataset, PlotOrientation.HORIZONTAL, true, false,false);
        NumberAxis rangeAxis = new NumberAxis("Policy Score");
        rangeAxis.setAutoRange(true);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRangeAxis(rangeAxis);

        chart.getXYPlot().getRangeAxis().setLowerBound(Environment.evaluatePolicy(Environment.randomPolicy()));
        BufferedImage objBufferedImage = chart.createBufferedImage(width, height);
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        try {
            ImageIO.write(objBufferedImage, "png", bas);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] byteArray = bas.toByteArray();
        document.add(Image.getInstance(byteArray));

        // DIAGRAM 4
        JFreeChart chart2 = ChartFactory.createXYLineChart("Visit Rates", "PI run number", "Visits", numbersDataset, PlotOrientation.HORIZONTAL, true, false,false);
        BufferedImage objBufferedImage2 = chart2.createBufferedImage(width, height);
        ByteArrayOutputStream bas2 = new ByteArrayOutputStream();
        try {
            ImageIO.write(objBufferedImage2, "png", bas2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] byteArray2 = bas2.toByteArray();
        document.add(Image.getInstance(byteArray2));
//        document.add(rectangle2d);
        writer.setCompressionLevel(0);
        document.close();

        String reportInLine = columns + ";" + rows + ";" + objectAnalogy + ";"
                + Integer.parseInt(Main.propertiesFile.getProperties().get(PropertyTypeEnum.POLICY_ACCURACY)) + ";"
                + numOfSamplePolicies + ";"
                + timeInMillis + ";"
                + grade + ";"
                + numbersDataset.getSeries(0).getMaxY() + ";"
                + numbersDataset.getSeries(1).getMaxY() + "\n";
        appendStrToFile(Main.propertiesFile.getProperties().get(PropertyTypeEnum.REPORTING_PATH_FOR_FILES) + "..\\" + ReportFileEnum.GENERAL_REPORT.getFilename(), reportInLine);

    }

    private static PdfPTable createTable() throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int finalI = i;
                int finalJ = j;
                State state = policy.getBestActions().keySet().stream().filter(s -> s != null && s.getId().equals("[" + finalI + "," + finalJ + "]")).findFirst().orElse(null);
                PdfPCell cell;
                Image image;
                if (state == null) {
                    image = Image.getInstance(World.class.getClassLoader().getResource("rock.png").getPath());
                } else if (Environment.isEndState(state)) {
                    image = Image.getInstance(World.class.getClassLoader().getResource("gem.png").getPath());
                } else {
                    image = Image.getInstance(World.class.getClassLoader().getResource("upArrow.png").getPath());
                    if (state.getReward() != null && state.getReward() <= 0) {
                        image = Image.getInstance(World.class.getClassLoader().getResource("upArrowRed.png").getPath());
                    }
                    image.setRotationDegrees(policy.getBestActions().get(state).getImageRotation());

                }
                image.scaleToFit(20, 20);
//                Phrase phrase = new Phrase("[" + j + "," + i + "]");
                cell = new PdfPCell(image);
                cell.setVerticalAlignment(Element.ALIGN_CENTER);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorderColor(BaseColor.WHITE);
                table.addCell(cell);

            }
        }
        return table;
    }

    public static void appendStrToFile(String fileName,
                                       String str) {
        try {
            // Open given file in append mode.
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(fileName, true));
            out.write(str);
            out.close();
        } catch (IOException e) {
            System.out.println("exception occoured" + e);
        }
    }
}
