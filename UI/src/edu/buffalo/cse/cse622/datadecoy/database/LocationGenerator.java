package edu.buffalo.cse.cse622.datadecoy.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class LocationGenerator {
	static File file = new File("data/data/edu.buffalo.cse.cse622.datadecoy/databases/auto-mock.txt");
	static String coordinates[] = new String[]{
			"42.90237				-78.868535",
			"42.94778210024827		-78.82947385311127",
			"42.99310362543842		-78.80440056324005",	// AMC Maple Ridge 8
			"43.00174110404659		-78.78562644124031",
			"42.916963677585706		-78.87690469622612",
			"42.92665964457279		-78.87682557106018",
			"42.952112				-78.825121",
			"42.9229948				-78.8770024",
			"42.9220049				-78.8772661",
			"42.9206216				-78.8769963",
			"42.9227459				-78.878956",
			"42.897948				-78.873533",	// Fat Bob's Smokehouse 		- 41 Virginia Pl (Virginia St)
			"42.909043				-78.877164",	// Coffee Culture 448			- 448 Elmwood Ave (at Bryant St)
			"42.908974				-78.878479",	// Trattoria Aroma				- 307 Bryant Street (Ashland Ave)
			"43.020214				-78.878246",	// PJ Hooligan's Bar & Grill	- 11 Main St (at Young St), Tonawanda
			"42.992453				-78.804901"		// Tea Leaf Cafe				- 4224 Maple Rd (at Sweet Home Rd), Amherst
			};
	
	public static void genAutoMock() {
		
		BufferedWriter out = null;
		try {
			out 				= new BufferedWriter(new FileWriter(file));
			int len 			= coordinates.length;
			while(len > 0) {
				out.write(coordinates[coordinates.length - len] + System.getProperty("line.separator")); 
				len--;
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
