package sim.app.murmur;
import java.util.concurrent.TimeUnit;

import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;

public class Starling implements Steppable
{

    private Double3D pos =  new Double3D(0.0, 0.0, 0.0);
    private Double3D lastD =  new Double3D(0.0, 0.0, 0.0);
    private Double3D nestLoc =  new Double3D(0.0, 0.0, 0.0);
    private double vel = 0.0;
    


    private boolean dead = false;

    public Continuous3D birds;

    //Getters
    public Double3D GetPosition() { return pos; }
    public Double3D GetLastDirection() { return lastD; }
    public Double3D GetNestLocation() { return nestLoc; }
    public double GetVelocity(){ return vel; }
    public boolean GetDead() { return dead; }

    //Setters
    public void SetPosition(Double3D p) { pos = p; }
    public void SetLastDirection(Double3D l) { lastD = l; }
    public void SetNestLocation(Double3D n) { nestLoc = n; }
    public void SetVelocity(double v) { vel = v; }
    public void SetDead(boolean d) { dead = d; }

    public Starling(Double3D location, Double3D nest, Double3D initD, double velocity, boolean d)
    {
        pos = location;
        nestLoc = nest;
        lastD = initD.normalize();
        vel = velocity;
        dead = d;
    }

    public Starling(double locX, double locY, double locZ, Double3D nest, double velocity, boolean d)
    {
        pos = new Double3D(locX, locY, locZ);
        nestLoc = nest;
        vel = velocity;
        dead = d;
    }

    //Called at every step of the simulation
    public void step(SimState state) {
        Murmur mur = (Murmur) state;

        pos = birds.getObjectLocation(this);

        if (!dead) {

            //Gets neighbors
            Bag neighborStarlings = getNeighbors(mur);

            //Calculates agent rules (separation, alignment, cohesion, nesting)
            //SafeNormalize is a function defined in the Murmur class
            
            Double3D sep = Separation(neighborStarlings, birds);
            sep = mur.SafeNormalize(sep);
            Double3D avd = AvoidPredator(neighborStarlings, birds);
            avd = mur.SafeNormalize(avd);
            Double3D align = Alignment(neighborStarlings, birds);
            align = mur.SafeNormalize(align);
            Double3D coh = Cohesion(neighborStarlings, birds);
            coh = mur.SafeNormalize(coh);
            Double3D nes = Nesting(birds);
            nes = mur.SafeNormalize(nes);

            //Calculates X, Y and Z direction components
            double newMoveDirX = sep.x * mur.separation + avd.x * mur.predAvoid  + align.x * mur.alignment + coh.x * mur.cohesion  + nes.x * mur.nestAttraction + mur.turningRate * lastD.x;
            double newMoveDirY = sep.y * mur.separation + avd.y * mur.predAvoid  + align.y * mur.alignment + coh.y * mur.cohesion  + nes.y * mur.nestAttraction + mur.turningRate * lastD.y;
            double newMoveDirZ = sep.z * mur.separation + avd.z * mur.predAvoid  + align.z * mur.alignment + coh.z * mur.cohesion  + nes.z * mur.nestAttraction + mur.turningRate * lastD.z;

            //System.out.println(sep + "; " + align + "; " + coh + "; " + nes + "-----");
            //System.out.println("sep: " + (sep.x * mur.separation) + ", align: " + (align.x * mur.alignment) + ", coh: " + (coh.x * mur.cohesion) + ", nest: " + (nes.x * mur.nestAttraction) + "----------");
            //System.out.println(newMoveDirX + ", " + newMoveDirY + ", " + newMoveDirZ + "-------");
            //System.out.println(neighborStarlings.toArray() + "--------------");

            //Distance = speed * time
            double moveDist = vel * mur.deltaT;

            //Creates Normalized Direction Vector3 and Moves bird
            if (newMoveDirX != 0 && newMoveDirY != 0 && newMoveDirZ != 0) {

                Double3D newMoveVec = (new Double3D(newMoveDirX, newMoveDirY, newMoveDirZ)).normalize();

                Double3D newPos = new Double3D(newMoveVec.x * moveDist + pos.x, newMoveVec.y * moveDist + pos.y, newMoveVec.z * moveDist + pos.z);
                lastD = newMoveVec;

                birds.setObjectLocation(this, newPos);
            } else {
                Double3D contDir = new Double3D(lastD.x * moveDist, lastD.y * moveDist, lastD.z * moveDist);
                birds.setObjectLocation(this, contDir);
            }

        }

    }
	//Just used for testing visualisation of agents
    private Double3D TempMove()
    {
        double newY = pos.getY() + vel;
        return new Double3D(pos.getX(), newY, pos.getZ());
    }

    //Finds neighboring birds in a radius around the agent
    public Bag getNeighbors(Murmur mur)
    {

        Bag n = mur.birds.getNeighborsExactlyWithinDistance(pos, mur.neighborDist);

        //Makes sure all of the agents are alive
        Bag aliveNeighbors = new Bag();
        for (int i = 0; i < n.size(); i++)
        {
            if (n.objs[i] instanceof Starling)
            {
                Starling other = (Starling) n.objs[i];
                if (!(other.GetDead()))
                {
                    aliveNeighbors.add(other);
                }
            }
            else if (n.objs[i] instanceof Falcon)
            {
            	
                Falcon other = (Falcon) n.objs[i];
                aliveNeighbors.add(other);
            }
            else if (n.objs[i] instanceof Obstacle)
            {
                Obstacle otherObs = (Obstacle) n.objs[i];
                aliveNeighbors.add(otherObs);
            }
            else if (n.objs[i] instanceof Cliff)
            {
                Cliff otherCli = (Cliff) n.objs[i];
                aliveNeighbors.add(otherCli);
            }
        }

        //return VisionNeighbors;
        return aliveNeighbors;

    }
    
    //Steers away from colliding with other starling
    public Double3D Separation(Bag s, Continuous3D birdField)
    {
        //Separation rule:
        //  find the negative distance between two starlings
        //  Multiply it by the reciprocal of the distance between the two
    	
        if (s==null || s.numObjs == 0 || s.size() == 1) return new Double3D(0,0,0);

        Double3D newDir = new Double3D(0,0,0);
        for (int i = 0; i < s.size(); i++)
        {
            //Only looks for starlings
            if (s.objs[i] instanceof Starling) {
                Starling otherBird = (Starling) s.objs[i];

                if (s.objs[i] != this) {

                    Double3D otherPos = otherBird.GetPosition();
                    double otherDistSq = otherPos.distance(pos);
                    otherDistSq = 1 / otherDistSq;

                    //Calculates repulsion vector from other birds
                    double xDir = (otherPos.x - pos.x) * otherDistSq * -1;
                    double yDir = (otherPos.y - pos.y) * otherDistSq * -1;
                    double zDir = (otherPos.z - pos.z) * otherDistSq * -1;
                    newDir = new Double3D(xDir + newDir.x, yDir + newDir.y, zDir + newDir.z);
                    //System.out.println("Sep");
                }
            }

        }
        
        return newDir;

    }
    
    //VERY SIMILAR TO SEPARATION
    //Differences
    //  - Only Looks for predator
    //  - Uses distance instead of square distance for calculations
    //
    //Useful for separate parameters for predator avoidance and starling avoidance
    //Could probably be refactored with separation into one function later
    public Double3D AvoidPredator(Bag s, Continuous3D birdField)
    {
    	
        if (s==null || s.numObjs == 0 || s.size() == 1) return new Double3D(0,0,0);
        
        Double3D newDir = new Double3D(0,0,0);
        for (int i = 0; i < s.size(); i++) {

            if (s.objs[i] instanceof Falcon) {

                Falcon otherBird = (Falcon) s.objs[i];
                
                

                Double3D otherPos = otherBird.GetPosition();
                
                
                double otherDist = otherPos.distance(pos);
                
                
                otherDist = 1 / otherDist;
                

                double xDir = (otherPos.x - pos.x ) * (otherDist) * -1 ;
                double yDir = (otherPos.y - pos.y ) * (otherDist) * -1 ;
                double zDir = (otherPos.z - pos.z ) * (otherDist) * -1 ;
                
                
            	
                newDir = new Double3D(xDir + newDir.x, yDir + newDir.y, zDir + newDir.z);
                

            	
                //System.out.println("Pre");

            }
            else if (s.objs[i] instanceof Obstacle) {
            	
            	Obstacle otherObs = (Obstacle) s.objs[i];
            	Double3D otherObsPos = otherObs.GetPosition();
            	
            	double otherObsDist = otherObsPos.distance(pos);
            	otherObsDist = 1 / otherObsDist;
            	
            	double xDir1 = (otherObsPos.x - pos.x) * otherObsDist * -1;
                double yDir1 = (otherObsPos.y - pos.y) * otherObsDist * -1;
                double zDir1 = (otherObsPos.z - pos.z) * otherObsDist * -1;
                
                newDir = new Double3D(xDir1 + newDir.x, yDir1 + newDir.y, zDir1 + newDir.z);
                
                
            }
            else if (s.objs[i] instanceof Cliff) {
            	
            	Cliff otherCli = (Cliff) s.objs[i];
            
            	Double3D otherCliPos = otherCli.GetPosition();
            	
            	double otherCliDist = otherCliPos.distance(pos);
            	otherCliDist = 1 / otherCliDist;
            	
            	double xDir2 = (otherCliPos.x - pos.x) * otherCliDist * -1;
                double yDir2 = (otherCliPos.y - pos.y) * otherCliDist * -1;
                double zDir2 = (otherCliPos.z - pos.z) * otherCliDist * -1;
                
                newDir = new Double3D(xDir2 + newDir.x, yDir2 + newDir.y, zDir2 + newDir.z);
            }

        }
  
        return newDir;
        
    }


    //Aligns its direction with The starling's neighbours
    public Double3D Alignment(Bag s, Continuous3D birdField)
    {
        //Alignment:
        //  Adds up the direction vectors of all neighboring birds and rerturns it
        //  This result is normalized and added into the steering vector.

        if (s==null || s.numObjs == 0 || s.size() == 0) return new Double3D(0,0,0);

        Double3D newDir = new Double3D(0,0,0);
        for (int i = 0; i < s.size(); i++)
        {
            //Only looks for starlings
            if (s.objs[i] instanceof Starling) {
                Starling otherBird = (Starling) s.objs[i];

                if (s.objs[i] != this) {
                    //Adds together all of the neighbor's current movement direction
                    Double3D otherDir = otherBird.GetLastDirection();
                    newDir = new Double3D(newDir.x + otherDir.x, newDir.y + otherDir.y, newDir.z + otherDir.z);
                    
                }
            }
        }

        return newDir;

    }

    //Generally tries to steer towards the center of the flock
    public Double3D Cohesion(Bag s, Continuous3D birdField)
    {
        //Cohesion rule
        //  Averages the positions of all neighbors
        //  Calculates the vector between the current position and the average neighbor position

        if (s==null || s.numObjs == 0 || s.size() == 0) return new Double3D(0,0,0);

        Double3D nPos = new Double3D(0,0,0);
        double count = 0;
        for (int i = 0; i < s.size(); i++)
        {
            //Only looks for starlings
            if (s.objs[i] instanceof Starling) {
                Starling otherBird = (Starling) s.objs[i];

                if (s.objs[i] != this) {
                    //Averages the position of all neghboring birds
                    Double3D otherPos = otherBird.GetPosition();
                    nPos = new Double3D(nPos.x + otherPos.x, nPos.y + otherPos.y, nPos.z + otherPos.z);
                    count++;
                }
            }
        }

        //calculates direction to the average neighbor position
        if (count == 0) {return new Double3D(0.0, 0.0, 0.0);}
        nPos = new Double3D(nPos.x/count, nPos.y/count, nPos.z/count);
        Double3D newDirVect = new Double3D(nPos.x - pos.x, nPos.y - pos.y, nPos.z - pos.z);

        return newDirVect;

    }

    //Generally attempts to steer towards its nest
    public Double3D Nesting(Continuous3D bridField)
    {
        //Nesting rule:
        //  Calc Direction to nest
        //  Add a constant direction component towards the nesting coord

        //Simply calculates the vector towards the nesting location
        Double3D nestDir = new Double3D(nestLoc.x - pos.x, nestLoc.y - pos.y, nestLoc.z - pos.z);
        

        return nestDir.normalize();

    }

}