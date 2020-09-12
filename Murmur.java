package sim.app.murmur;

import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import java.io.*;
import java.text.DecimalFormat;



public class Murmur extends SimState
{

    public static double fieldSize = 100.0;
    public static double neighborDist = 10.0;

    public int ittNumBeforeRecording = 1500;
    public static DecimalFormat df = new DecimalFormat("0.0000");
    public static String resultsFileName = "results.txt";

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //MODEL PARAMETERS
    //MODIFY AGENT PROPERTIES HERE
    public static int numStarlings = 1000;
    public static int numFalcon = 1;

    public static boolean collectResults = false;

    public static double starlingSpeed = 1; //starling's fly at 80km/h (1:200 scale)
    public static double separation = 6.1;
    public static double predAvoid = 100;
    public static double alignment = 6.6;
    public static double cohesion = 5.3;
    public static double nestAttraction = 1.9;
    public static double turningRate = 45.0;

    public static double deltaT = 0.4;

    public static double falconSpeed = 4; //Perigrine Falcon flies at 390km/h (1:200  scale)
    public static double chaseRate = 1.0;
    public static double turningRateFalcon = 9.0;
    public static double collideDistance = 0.05;

    public double nestLocX = 0.0;
    public double nestLocY = 0.0;
    public double nestLocZ = 0.0;
    
    public double clLocX = 0.0;
    public double clLocY = 0.0;
    public double clLocZ = 0.0;
        
    //sea simulation position
    public static final double[][] obstInfo = { {-10, -5, 100}, {0, -5, 100}, {10, -5, 100}, {20, -5, 100}, {30, -5, 100}, {40, -5, 100}, 
    										    {-10, -5, 90}, {0, -5, 90}, {10, -5, 90}, {20, -5, 90}, {30, -5, 90}, {40, -5, 90}, 
    										    {-10, -5, 80}, {0, -5, 80}, {10, -5, 80}, {20, -5, 80}, {30, -5, 80}, {40, -5, 80}, 
    										    {-10, -5, 70}, {0, -5, 70}, {10, -5, 70}, {20, -5, 70}, {30, -5, 70}, {40, -5, 70}, 
    										    {-10, -5, 60}, {0, -5, 60}, {10, -5, 60}, {20, -5, 60}, {30, -5, 60}, {40, -5, 60}, 
    										    {-10, -5, 50}, {0, -5, 50}, {10, -5, 50}, {20, -5, 50}, {30, -5, 50}, {40, -5, 50}, 
    										    {-10, -5, 40}, {0, -5, 40}, {10, -5, 40}, {20, -5, 40}, {30, -5, 40}, {40, -5, 40},
    										    {-10, -5, 30}, {0, -5, 30}, {10, -5, 30}, {20, -5, 30}, {30, -5, 30}, {40, -5, 30},
    										    {-10, -5, 20}, {0, -5, 20}, {10, -5, 20}, {20, -5, 20}, {30, -5, 20}, {40, -5, 20},
    										    {-10, -5, 10}, {0, -5, 10}, {10, -5, 10}, {20, -5, 10}, {30, -5, 10}, {40, -5, 10},
    										    {-10, -5, 0}, {0, -5, 0}, {10, -5, 0}, {20, -5, 0}, {30, -5, 0}, {40, -5, 0}, //Above values are left side from down to up
    										    {50, -5, 100}, {60, -5, 100}, {70, -5, 100}, {80, -5, 100}, {90, -5, 100}, {100, -5, 100}, 
    										    {50, -5, 90}, {60, -5, 90}, {70, -5, 90}, {80, -5, 90}, {90, -5, 90}, {100, -5, 90},
    										    {50, -5, 80}, {60, -5, 80}, {70, -5, 80}, {80, -5, 80}, {90, -5, 80}, {100, -5, 80},
    										    {50, -5, 70}, {60, -5, 70}, {70, -5, 70}, {80, -5, 70}, {90, -5, 70}, {100, -5, 70},
    										    {50, -5, 60}, {60, -5, 60}, {70, -5, 60}, {80, -5, 60}, {90, -5, 60}, {100, -5, 60},
    										    {50, -5, 50}, {60, -5, 50}, {70, -5, 50}, {80, -5, 50}, {90, -5, 50}, {100, -5, 50},
    										    {50, -5, 40}, {60, -5, 40}, {70, -5, 40}, {80, -5, 40}, {90, -5, 40}, {100, -5, 40},
    										    {50, -5, 30}, {60, -5, 30}, {70, -5, 30}, {80, -5, 30}, {90, -5, 30}, {100, -5, 30},
    										    {50, -5, 20}, {60, -5, 20}, {70, -5, 20}, {80, -5, 20}, {90, -5, 20}, {100, -5, 20},
    										    {50, -5, 10}, {60, -5, 10}, {70, -5, 10}, {80, -5, 10}, {90, -5, 10}, {100, -5, 10},
    										    {50, -5, 0}, {60, -5, 0}, {70, -5, 0}, {80, -5, 0}, {90, -5, 0}, {100, -5, 0}}; //Above values are right side from down to up
    											
    //cliff simulation position
    public static final double[][] cliffInfo = { {80, 55, 50}, {80, 45, 50}, {80, 35, 50}, {80, 25, 50}, {80, 15, 50},{80, 5, 50},
    											 {80, 55, 60}, {80, 45, 60}, {80, 35, 60}, {80, 25, 60}, {80, 15, 60},{80, 5, 60},
    											 {90, 55, 50}, {90, 45, 50}, {90, 35, 50}, {90, 25, 50}, {90, 15, 50},{90, 5, 50},
    											 {90, 55, 60}, {90, 45, 60}, {90, 35, 60}, {90, 25, 60}, {90, 15, 60},{90, 5, 60}};
    
    
    public final static double EXTRA_SPACE = 10;
    public static final double DIAMETER = 1;
    ///////////////////////////////////////////////////////////////////////////////////////////////

    


    public Continuous3D birds;


    public Murmur(long seed)
    {
        super(seed);
        birds = new Continuous3D(neighborDist/1.5, fieldSize, fieldSize, fieldSize);
    }

    public void start()
    {
        super.start();
        birds = new Continuous3D(neighborDist/1.5, fieldSize, fieldSize, fieldSize);

        nestLocX = fieldSize / 2;
        nestLocY = fieldSize / 2;
        nestLocZ = fieldSize / 2;

        Double3D nestLoc = new Double3D(nestLocX, nestLocY, nestLocZ);
        
        for( int i = 0 ; i < cliffInfo.length ; i++ )
        {
        	//Create the objects to simulate the cliff scene in the model 
        	Cliff cli = new Cliff(10);
            birds.setObjectLocation(cli,
                    new Double3D(cliffInfo[i][0],cliffInfo[i][1],cliffInfo[i][2]));
        	schedule.scheduleRepeating(cli, 4, 1.0);
        }
        
        
        for( int i = 0 ; i < obstInfo.length ; i++ )
        {
        	//Create the objects to simulate the sea scene in the model 
        	Obstacle ob = new Obstacle(10);
            birds.setObjectLocation(ob,
                    new Double3D(obstInfo[i][0],obstInfo[i][1],obstInfo[i][2]));
        	schedule.scheduleRepeating(ob, 3, 1.0);
        }

        ////////////////////////////////////////////////////////
        //Seed Starlings
        Bag flock = new Bag();

        for (int i = 0; i < numStarlings; i++) {
            Double3D starLoc = new Double3D(random.nextDouble()*fieldSize/2, random.nextDouble()*fieldSize/2, random.nextDouble()*fieldSize/2);
            Double3D initDir = new Double3D(random.nextDouble(), random.nextDouble(), random.nextDouble());
            Starling s = new Starling(starLoc, nestLoc, initDir, starlingSpeed, false);

            //puts the starling on the grid
            birds.setObjectLocation(s, starLoc);
            s.birds = birds;
            flock.add(s);
            

            //You actually NEED to schedule each steppable for it to run
            //  (agent, ordering, interval[default=1.0])
            schedule.scheduleRepeating(s, 1, 1.0);
        }

        ////////////////////////////////////////////////////////
        //Seed Falcons
        Bag preds = new Bag();

        for (int i = 0; i < numFalcon; i++)
        {
            Double3D falLoc = new Double3D((fieldSize/2) + random.nextDouble()*fieldSize/2, (fieldSize/2) + random.nextDouble()*fieldSize/2, (fieldSize/2) + random.nextDouble()*fieldSize/2);
        	Falcon f =  new Falcon(falLoc, falconSpeed, flock, neighborDist);
            f.birds = birds;

            birds.setObjectLocation(f, falLoc);
            preds.add(f);

            schedule.scheduleRepeating(f, 2, 1.0);
        }
        

    

        ////////////////////////////////////////////////////////
        //Seed Nest Location
        //When collecting results, the nest class is seeded for the nesting location
        //  Otherwise, a Vector3 is given a protrayal and placed at the nesting location
        if (collectResults) {
            Nest n = new Nest(nestLoc, ittNumBeforeRecording, flock, preds);
            birds.setObjectLocation(n, nestLoc);
            schedule.scheduleRepeating(n, 5, 1.0);
            
            System.out.println("!!Results Recording Enabled!!");

            //create results file
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(resultsFileName));
                out.write("Starling and Predator Results:\n");
                out.close();
            }
            catch (IOException e) {
                System.out.println("Exception Occurred" + e);
            }
        }
        else
        {
            birds.setObjectLocation(nestLoc, nestLoc);
            
        }
        
    }
    
    
    //Just a boiler plate main incase you wanna run the simulation without UI from the Command Line
    public static void main(String[] args)
    {
        //doLoop(Murmur.class, args);

        //ARGUMENTS:
        // 0- Itteration Num / int
        // 1- Starling Num / int
        // 2- Falcon Num / int
        // 3- Starling Speed / double
        // 4- Falcon Speed / double
        // 5- Time Rate / double
        // 6- Results File Name / String

        int itterNum = Integer.parseInt(args[0]);
        numStarlings = Integer.parseInt(args[1]);
        numFalcon = Integer.parseInt(args[2]);
        starlingSpeed = Double.parseDouble(args[3]);
        falconSpeed = Double.parseDouble(args[4]);
        deltaT = Double.parseDouble(args[5]);
        resultsFileName = (String) args[6];

        //sets whether to collect flock metrics
        if (resultsFileName == "-")
            collectResults = false;
        else
            collectResults = true;

        SimState state = new Murmur(System.currentTimeMillis());
        state.start();
        do
            if (!state.schedule.step(state)) break;
        while(state.schedule.getSteps() < itterNum);
        state.finish();
        System.out.println("Finished: " + resultsFileName + ";");
        System.exit(0);
    }

    //Utility method for normalization
    public Double3D SafeNormalize(Double3D sep)
    {
        if(sep.x == 0 && sep.y == 0 && sep.z == 0) { return new Double3D(0.0, 0.0, 0.0); }

        return sep.normalize();
    }

    //Writes metrics to file
    public void WriteToResultsFile(int i, long time)
    {
        try {
            /*String str = "T-" + i +
                         ": -Ext(T)= " + df.format(extT) +
                         " -Pol(T)= " + df.format(polT) +
                         " -Qlty(T)= " + df.format(qltyT) +
                         " -Average-Extension= " + df.format(extension) +
                         " -Average-Polarization= " + df.format(polarization) +
                         " -Average-Quality=" + df.format(quality) + ";\n";*/
        	
            String str = "T-" + i +
            ": Time = " +  df.format(time) + ";\n";

            //Open given file in append mode.
            BufferedWriter out = new BufferedWriter(new FileWriter(resultsFileName, true));
            out.write(str);
            out.close();
        }
        catch (IOException e) {
            System.out.println("An Exception Has Occoured" + e);
        }
    }

}