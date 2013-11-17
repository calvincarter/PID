package gps;

public class GPS {

	
	/*************************************************************************
	 * //Function to calculate the course between two waypoints
	 * //I'm using the real formulas--no lookup table fakes!
	 *************************************************************************/
	public int get_gps_course(float flat1, float flon1, float flat2, float flon2)
	{
	  float calc;
	  float bear_calc;

	  float x = (float) (69.1 * (flat2 - flat1)); 
	  float y = (float) (69.1 * (flon2 - flon1) * Math.cos(flat1/57.3));

	  calc=(float) Math.atan2(y,x);

	  // Convert to degrees added code by: Calvin Carter	
	  // bear_calc= degrees(calc);
    
	  Double degrees = (calc * 180) / Math.PI;
      bear_calc = degrees.floatValue();
      
      // end of added code convert to degrees
	  
	  if(bear_calc<=1){
	    bear_calc=360+bear_calc; 
	  }
	  
	  return (int) bear_calc;
	}


	/*************************************************************************
	 * //Function to calculate the distance between two waypoints
	 * //I'm using the real formulas
	 *************************************************************************/
	
	// changed it to return float instead of int for better accuracy
	// by Calvin Carter 11/16/13
	public float get_gps_dist(float flat1, float flon1, float flat2, float flon2)
	{
	  float x = (float) (69.1 * (flat2 - flat1)); 
	  float y = (float) (69.1 * (flon2 - flon1) * Math.cos(flat1/57.3));

	  return (float) ((float) Math.sqrt((float)(x*x) + (float)(y*y))*1609.344); 
	}
}
