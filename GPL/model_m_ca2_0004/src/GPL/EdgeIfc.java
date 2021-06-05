//created on: Sun Jul 13 23:04:15 CDT 2003

package GPL; 

public   interface  EdgeIfc {
	
    //public void setWeight( int weight );
    //__feature_mapping__ [WeightedOnlyVertices] [8:8]
	public int getWeight();

	
    //__feature_mapping__ [Base] [7:7]
	public Vertex getStart( );

	
    //__feature_mapping__ [Base] [8:8]
	public Vertex getEnd( );

	
    //__feature_mapping__ [Base] [9:9]
	public void display( );

	


    //__feature_mapping__ [Base] [12:12]
	public void setWeight( int weight );

	
 //   public int getWeight();

    //__feature_mapping__ [Base] [15:15]
	public Vertex getOtherVertex( Vertex vertex );

	

    //__feature_mapping__ [Base] [17:17]
	public void adjustAdorns( EdgeIfc the_edge );


}
