package GPL; 

import java.util.LinkedList; 

// Vertex class

 // *************************************************************************

public  class  Neighbor  implements EdgeIfc, NeighborIfc {
	
    public  Vertex neighbor;

	

    // This constructor has to be present here so that the default one
    // Called on Weighted can call it, i.e. it is not longer implicit
    //__feature_mapping__ [UndirectedWithNeighbors] [15:17]
	public Neighbor()  {
        neighbor = null;
    }

	

    //__feature_mapping__ [UndirectedWithNeighbors] [19:22]
	public Neighbor( Vertex theNeighbor )
   {
        NeighborConstructor( theNeighbor );
    }

	

    //__feature_mapping__ [UndirectedWithNeighbors] [24:24]
	public void setWeight(int weight) {}

	
    //__feature_mapping__ [UndirectedWithNeighbors] [25:25]
	public int getWeight() { return 0; }

	

    //__feature_mapping__ [UndirectedWithNeighbors] [27:29]
	public void NeighborConstructor( Vertex theNeighbor ) {
        neighbor = theNeighbor;
    }

	

    //__feature_mapping__ [UndirectedWithNeighbors] [31:34]
	public void display()
    {
        System.out.print( neighbor.name + " ," );
    }

	

    //__feature_mapping__ [UndirectedWithNeighbors] [36:36]
	public Vertex getStart( ) { return null; }

	
    //__feature_mapping__ [UndirectedWithNeighbors] [37:37]
	public Vertex getEnd( ) { return null; }

	

    //__feature_mapping__ [UndirectedWithNeighbors] [39:42]
	public Vertex getOtherVertex( Vertex vertex )
    {
        return neighbor;
    }

	

    //__feature_mapping__ [UndirectedWithNeighbors] [44:46]
	public void adjustAdorns( EdgeIfc the_edge )
    {
    }


}
