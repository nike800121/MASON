package sim.app.murmur;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Nest implements Steppable
{

    public Double3D pos;
    public int iNum;
    public Bag starlings;
    public Bag falcons;

    public int numStarlings;
    public int numFalcons;

    public int itter = 0;
    public int count = 0;

    //Variables for calculating Extension
    public double k = 15; //extension collision penalty (> = worse)
    public double ro = 180; //polarization collision penalty (> = worse)

    //Weights for quality metric
    //  0 < sigma, gamma < 1
    //  sigma + gamma = 1
    public double sigma = 0.5; //extension weight for quality
    public double gamma = 0.5; //polarization weight for quality

    public double maxE = 0.0;

    public double runningExtensionSum;
    public double runningPolarizationSum;
    public double runningQualitySum;
    public double quality;
    
    public long startTime, startTime1;
    public long endTime, endTime1;
    public long timeElapsed, timeElapsed1;
    public long convert, convert1;
    

    public Nest(Double3D p, int i, Bag s, Bag f)
    {
        pos = p;
        iNum = i;
        starlings = s;
        falcons = f;
    }

    //Runs every step
    public void step(SimState state)
    {
        Murmur mur = (Murmur) state;
        itter++;

        //Waits for a certain amount of itteration setps to start recording metrics
        if (itter <= iNum) return;

        //Creates an array of Bags
        //  0 = starling not colliding
        //  1 = starling colliding with another starling
        Bag[] sepStar = CalcFlock(starlings, mur);

        numStarlings = starlings.size();
        numFalcons = falcons.size();
        

        //finds the flock centre
        Double3D cent = flockCentre(starlings);
        Double3D fDir = flockDir(starlings, mur);
        
        
        
        //Starts a loop for metric parameters
        double distanceSum = 0;
        //double maxE = 0;
        double deltaAngleSum = 0;
        
        
        for (int i = 0; i < sepStar[0].size(); i++) {
        	
        	
            Starling star = (Starling) sepStar[0].objs[i];

            //EXTENSION
            //Finds distance of all starlings to the flock centre
            double d = Math.abs(cent.distance(star.GetPosition()));
            distanceSum += d;
            
            //updates max extension
            if(d > maxE) { maxE = d; }

            //POLARIZATION
            Double3D starDir = star.GetLastDirection();
            double dotAngle = fDir.x * starDir.x + fDir.y * starDir.y + fDir.z * starDir.z;
            deltaAngleSum += (180 / Math.PI) * Math.acos(dotAngle);

        }
        
        //Use distance of all starlings to the flock centre to measure the separation time, 
        //if the predator gets close to the flock, the values of distances will over 15000.
        //measure the duration while the values of distance keep more than 15000.
        
        if(distanceSum < 15000)
        {
        	startTime = System.nanoTime();
        }
        else
        {
        	endTime = System.nanoTime();
        	timeElapsed = endTime - startTime;
        	convert = TimeUnit.SECONDS.convert(timeElapsed, TimeUnit.NANOSECONDS);
            if(convert > 0 && convert < 30)
            {
            	mur.WriteToResultsFile(itter, convert);
            	count++;
            	//System.out.println(count);
            }
            
            /*if(count == 5000)
            {
            	mur.WriteToResultsFile(itter, 100);
            	System.out.println("Done!!");
            }*/
        }

        //////////////////////////////////
        //Consistency of Extension
        //  How Well boids stick together
        //  normalized between 0 & 1 (closer to 1 is better)
        double cnsExtT = 1 - ((distanceSum + maxE * sepStar[1].size()) / (maxE * numStarlings));
        ///////////////////////////////////
        //Consistency of polarization
        //  How much the boids are going the same direction
        //  normaized between 0 & 1 (closer to 1 is better)
        double cnsPolT = 1 - ((deltaAngleSum + ro * sepStar[1].size()) / (180 * numStarlings));
        

        ///////////////////////////////////
        //quality
        //  weighted sum of polarization and extension
        double qltyT = sigma * cnsExtT + gamma * cnsPolT;
        
        //averaging extension and polarization
        runningExtensionSum += cnsExtT;
        double avgExtension = runningExtensionSum / (itter-iNum);
        runningPolarizationSum += cnsPolT;
        double avgPolarization = runningPolarizationSum / (itter-iNum);
        //calculates the running quality over the entire simulation
        runningQualitySum += qltyT;
        quality = runningQualitySum / (itter-iNum);

        //Write To File
        //mur.WriteToResultsFile(itter, cnsExtT, cnsPolT, qltyT, avgExtension, avgPolarization, quality);

    }

    //Function to return a bag of all living, non-colliding, starlings
    public Bag[] CalcFlock(Bag s, Murmur mur)
    {
        Bag newFlock = new Bag();
        Bag nonCollideFlock = new Bag();
        Bag cFlock = new Bag();

        for (int i = 0; i < s.size(); i++)
        {
            Starling star = (Starling) s.objs[i];
            //checks for starling collision
            Bag c = mur.birds.getNeighborsExactlyWithinDistance(star.GetPosition(), mur.collideDistance);

            if (!star.GetDead())
            {
                newFlock.add(star);

                //Adds the starling to the correct bag depending whether its colliding or not
                if (c.size() > 1)
                    cFlock.add(star);
                else
                    nonCollideFlock.add(star);
            }
        }

        starlings = newFlock;

        //creates the array of Bags and returns them
        Bag[] flockArr = {nonCollideFlock, cFlock};
        return flockArr;

    }

    //Calculates the centre of the flock
    public Double3D flockCentre(Bag s)
    {
        double x = 0;
        double y = 0;
        double z = 0;

        for (int i = 0; i < s.size(); i++)
        {
            Starling star = (Starling) s.objs[i];

            Double3D starPos = star.GetPosition();

            x += starPos.x;
            y += starPos.y;
            z += starPos.z;
        }

        return new Double3D(x/s.size(), y/s.size(), z/s.size());
    }

    //Calculates the average direction of the flock
    public Double3D flockDir(Bag s, Murmur mur)
    {
        Double3D sumDir = new Double3D();

        for (int i = 0; i < s.size(); i++)
        {
            Starling star = (Starling) s.objs[i];
            Double3D starDir = star.GetLastDirection();

            sumDir = new Double3D(starDir.x + sumDir.x, starDir.y + sumDir.y, starDir.z + sumDir.z);
        }

        return mur.SafeNormalize(sumDir);
    }

}