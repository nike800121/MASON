package sim.app.murmur;
import sim.engine.*;
import sim.field.continuous.*;
import sim.portrayal3d.SimplePortrayal3D;
import sim.util.*;

public class Falcon implements Steppable
{

    //TODO Basically EVERYTHING regarding the falcon
    private Double3D pos =  new Double3D(0.0, 0.0, 0.0);
    private Double3D lastD =  new Double3D(0.0, 0.0, 0.0);
    private double vel;
    private Bag starlingFlock;

    private Starling target;
    public double targetDistance;

    public Continuous3D birds;

    //getters
    public Double3D GetPosition() { return pos; }
    public Double3D GetLastDirection() { return lastD; }
    public double GetVelocity(){ return vel; }
    public Starling GetTarget() { return target; }

    //setters
    public void SetPosition(Double3D p) { pos = p; }
    public void SetLastDirection(Double3D l) { lastD = l; }
    public void SetVelocity(double v) { vel = v; }
    public void SetTarget(Starling s) { target = s;}

    public Falcon(Double3D location, double velocity, Bag flock, double targetDist)
    {
        pos = location;
        vel = velocity;
        starlingFlock = flock;
        targetDistance = targetDist;
    }

    public Falcon(double locX, double locY, double locZ, double velocity, Bag flock, double detarget)
    {
        pos = new Double3D(locX, locY, locZ);
        vel = velocity;
        starlingFlock = flock;
        targetDistance = detarget;
    }

    //Called at every step of the simulation
    public void step(SimState state)
    {
        Murmur mur = (Murmur) state;
        pos = mur.birds.getObjectLocation(this);

        boolean caught = CatchStarling(starlingFlock, birds, mur.collideDistance);
        Bag neighborStarlings = getNeighbors(mur);

        //Calls chase rule
        //SafeNormalize is a function defined in the Murmur class
        Double3D chase = Chase(starlingFlock, birds);
        chase = mur.SafeNormalize(chase);
        Double3D avd = AvoidObstacle(neighborStarlings, birds);
        avd = mur.SafeNormalize(avd);

        //Calculates direction X, Y, and Z components
        double newMoveDirX = avd.x * mur.predAvoid + mur.chaseRate * chase.x + mur.turningRateFalcon * lastD.x;
        double newMoveDirY = avd.y * mur.predAvoid + mur.chaseRate * chase.y + mur.turningRateFalcon * lastD.y;
        double newMoveDirZ = avd.z * mur.predAvoid + mur.chaseRate * chase.z +  mur.turningRateFalcon * lastD.z;

        Double3D newMoveVec = (new Double3D(newMoveDirX, newMoveDirY, newMoveDirZ)).normalize();

        //distance = speed * time
        double moveDist = vel * mur.deltaT;

        //moves the agent
        Double3D newPos = new Double3D(newMoveVec.x * moveDist + pos.x, newMoveVec.y * moveDist + pos.y, newMoveVec.z * moveDist + pos.z);
        lastD = newMoveVec;

        birds.setObjectLocation(this, newPos);
    }

    //Just a visualization Method for testing
    private void tempMove()
    {
        double newY = pos.getY() + vel;
        pos = new Double3D(pos.getX(), newY, pos.getZ());
    }
    
    public Bag getNeighbors(Murmur mur)
    {

        Bag n = mur.birds.getNeighborsExactlyWithinDistance(pos, mur.neighborDist);

        //Makes sure all of the agents are alive
        Bag aliveNeighbors = new Bag();
        for (int i = 0; i < n.size(); i++)
        {
            if (n.objs[i] instanceof Obstacle)
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


    //Flies towards the centre of all of the starlings
    //Or fly towards a single starling that the predator is chasing
    public Double3D Chase(Bag s, Continuous3D birdField)
    {

        if (s.size() == 0) { return new Double3D(0.0, 0.0, 0.0); }
        Double3D newDir = new Double3D(0.0, 0.0, 0.0);

        if (target == null)
        {
            //Isn't tageting a specific agent
            //Flies towards the center of the flock of birds

            Double3D avgDir = new Double3D(0,0,0);
            int count = 0;

            for (int i = 0; i < s.size(); i++)
            {
                //Calculates center position of the flock
                Starling other = (Starling) s.objs[i];
                Double3D otherPos = other.GetPosition();

                avgDir = new Double3D(avgDir.x + otherPos.x, avgDir.y + otherPos.y, avgDir.z + otherPos.z);
                count++;

                if (otherPos.distance(pos) < targetDistance) { target = other; }
            }

            avgDir = new Double3D(avgDir.x / count, avgDir.y / count, avgDir.z / count);

            newDir = new Double3D(avgDir.x - pos.x, avgDir.y - pos.y, avgDir.z - pos.z);

        }
        else
        {
            //Is targeting a starling
            //Flies towards targeted bird
            Double3D otherPos = target.GetPosition();
            newDir  = new Double3D(otherPos.x - pos.x, otherPos.y - pos.y, otherPos.z - pos.z);

            //checks to see if the targeted birds is still in range
            if (otherPos.distance(pos) > targetDistance) { target = null; }
        }

        return newDir;

    }
    
    
    public Double3D AvoidObstacle(Bag s, Continuous3D birdField)
    {

        if (s==null || s.numObjs == 0 || s.size() == 1) return new Double3D(0,0,0);

        Double3D newDir = new Double3D(0,0,0);
        for (int i = 0; i < s.size(); i++) {
        	
        	if (s.objs[i] instanceof Obstacle) {
            	
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

    //Code for catching a starling
    //Dead starlings will stay motionless in the model
    //Dead starlings don't affect the motion of the rest of the birds
    public boolean CatchStarling(Bag s, Continuous3D birdField, double catchDist)
    {
        for (int i = 0; i < s.size(); i++)
        {
            //Can only catch starlings
            if (s.objs[i] instanceof Starling)
            {
                Starling other = (Starling) s.objs[i];

                double distOther = other.GetPosition().distance(pos);
                if (distOther <= catchDist)
                {
                    other.SetDead(true);
                    System.out.println("!!STARLING-CAUGHT!!");

                    return true;
                }

            }
        }

        return false;

    }

}