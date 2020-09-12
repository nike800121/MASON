package sim.app.murmur;

import sim.portrayal3d.continuous.*;
import sim.portrayal3d.simple.*;
import sim.engine.*;
import sim.display.*;
import sim.display3d.*;
import javax.swing.*;
import java.awt.*;
import sim.util.*;


//NOTE!!
//   - A lot of this code is based off of the mason tutorials provided in the Mason instalation.
//   - Please refer to this documentation for a detailed expnalation of how the model system works
//      inside Mason.
public class MurmurWithUI extends GUIState {

    public Display3D display;
    public JFrame displayFrame;
    

    ContinuousPortrayal3D birdPortrayal = new ContinuousPortrayal3D();
    //WireFrameBoxPortrayal3D wireFrameP;

    public static void main(String[] args) {
        new MurmurWithUI().createController();
    }

    public MurmurWithUI() {
        this(new Murmur(System.currentTimeMillis()));
    }

    public MurmurWithUI(SimState state) {
        super(state);
        
        Murmur mur = (Murmur) state;
        birdPortrayal.setField(mur.birds);
        }

    public static String getName() {
        return "A Murmur of Starlings + Predator";
    }

    public static Object getInfo() {
        return "<H2>Murmurs</H2> Birds of a feather flock together...";
    }

    public void start()
    {
        super.start();
        setupPortrayals();
    }
    public void load(SimState state)
    {
        super.load(state);
        setupPortrayals();
    }
    public void quit()
    {
        super.quit();
        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

    //Custom Portrayals setup for 3D
    public void setupPortrayals()
    {

    	Murmur mur = (Murmur) state;
        birdPortrayal.setField(((Murmur) state).birds);

        Color starlingColor = Color.blue;
        Color falconColor = Color.red;
        Color nestLocColor = Color.green;
        


        Bag agents = mur.birds.getAllObjects();

        //loops through each agent in the field and assigns it the correct portrayal
        for (int i = 0; i < agents.size(); i++)
        {

        	if (agents.objs[i] instanceof Starling) {
                birdPortrayal.setPortrayalForObject(agents.objs[i], new ConePortrayal3D(starlingColor, 1));
            }
            else if (agents.objs[i] instanceof Falcon) {
                birdPortrayal.setPortrayalForObject(agents.objs[i], new ConePortrayal3D(falconColor, 1.5));
            }
            else if (agents.objs[i] instanceof  Double3D || agents.objs[i] instanceof Nest) {
                birdPortrayal.setPortrayalForObject(agents.objs[i], new SpherePortrayal3D(nestLocColor, 1));
            }
        }

        
        display.createSceneGraph();
        display.reset();

    }
   

    //Class for setting up the 3d Model visualization
    public void init(Controller c)
    {
        super.init(c);

        Murmur mur = (Murmur) state;
        birdPortrayal.setField(mur.birds);
        
        //Change this to change the size of the simulation window
        display = new Display3D(800,800,this);
        display.attach(birdPortrayal, "A Flock of Birds");

        //Scale the display to view the entire flock of birds in the initial spawn
        //display.scale(1.0/(Murmur.fieldSize*1.05));
        //display.scale(0.2);

        double width = mur.fieldSize;
        display.translate(-width / 2, -width / 2, 0);
        //display.translate(0, 0, 0);
        display.scale(2.0/width);

        //axes
        //display.setShowsAxes(true);
        //display.setAxesLineMinMaxWidth(5.0, 10.0);

        displayFrame = display.createFrame();
        c.registerFrame(displayFrame);
        displayFrame.setVisible(true);
    }

}
