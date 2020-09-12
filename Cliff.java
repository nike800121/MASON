package sim.app.murmur;


import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous3D;
import sim.portrayal3d.*;
import sim.util.Double3D;

import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.geometry.*;

public class Cliff extends SimplePortrayal3D implements Steppable
{
	private static final long serialVersionUID = 1;
    private Double3D pos =  new Double3D(0.0, 0.0, 0.0);
    public Double3D GetPosition() { return pos; }
	
	double diameter;
	protected Color3f obstacleColor = new Color3f(165f/255,113f/255,78f/255);
	
	public Cliff( double diam )
    {
		this.diameter = diam;
    }
	
	public Cliff( Double3D p )
    {
		pos = p;
    }
	
	public TransformGroup getModel(Object obj, TransformGroup j3dModel)
    {
		if(j3dModel==null)
		{
			j3dModel = new TransformGroup();
			//Cylinder s = new Cylinder((float)diameter / 2, (float)diameter / 2);
			Box s1 = new Box((float)diameter / 2, (float)diameter / 2, (float)diameter / 2, 2, null);
			Appearance appearance = new Appearance();
			appearance.setColoringAttributes(new ColoringAttributes(obstacleColor, ColoringAttributes.SHADE_GOURAUD));          
			Material m= new Material();
			m.setAmbientColor(obstacleColor);
			m.setEmissiveColor(0f,0f,0f);
			m.setDiffuseColor(obstacleColor);
			m.setSpecularColor(1f,1f,1f);
        	m.setShininess(128f);
        	appearance.setMaterial(m);

        	s1.setAppearance(appearance);
        	j3dModel.addChild(s1);
        	clearPickableFlags(j3dModel);
        }
		return j3dModel;
    }

	
	public void step(SimState state) {
		
		Murmur mur = (Murmur) state;
		pos = mur.birds.getObjectLocation(this);
		
	}
}
