# MASON
This project is based on the existing model to enhance the more features in order to model a pattern of collective escape for starlings.

![GitHub Logo](/images/logo.png)

    INSTALATION:
**************************************************************
**************************************************************

This Project was written in Java, so the Java development kit will need to be installed in order to compile this project's code.
        https://www.oracle.com/uk/java/technologies/javase-downloads.html

This starling model was created using the multi-agent simulation library "Mason". This will need to be installed first for the code to run. Mason version 20 was used in this project.
        https://cs.gmu.edu/~eclab/projects/mason/#Download

This starling model is in 3D, and so the JoGL graphics library must also be downloaded and added to your computer's %PATH% and %CLASSPATH%.
        https://jogamp.org/deployment/jogamp-current/archive/

------------

    1. Add the JoGL installation to you computers %PATH% Global Variables

    2. Navigate to your Mason installation folder. Here, navigate to "target/classes/sim/app/" and create a folder called "murmur". Place all of the source code files in this folder.

    3. Open a command prompt and navigate to the mason installation's "target/classes/" directory.

    4. With JoGL added to the path, type in the following command:
            javac sim/app/murmur/*.java
        This will compile the simulation code.

    RUNNING:
**************************************************************
**************************************************************

To run the starling murmur model, there are two options:

    1. Running the command "java sim.app.murmur.MurmurWithUI" will run the model with a full 3D view using the default simulation parameters.

        (Parameters will need to be changes by modifying the Murmur.java source code and re-compiling)

    2. Running the command "java sim.app.murmur.Murmur args[]" will run the model without the visulization. The args[] will be replaces with simulation parameters. Useful for running batch simulations

        Arguments:
            - Number of Itterations (int) [default = (UNDEFINED)]
            - Number of Starlings (int)   [default = 1000]
            - Number of Falcons (int)     [default = 1]
            - Starling Speed (double)     [default = 1]
            - Falcon Speed (double)       [default = 4.875]
            - Time Delta (double)         [default = 0.4]
            - Results File (String)       [deafult = "results.txt"]

        (All arguments must be present to run the simulation without visualization)
