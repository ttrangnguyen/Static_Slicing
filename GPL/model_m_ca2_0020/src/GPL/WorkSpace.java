package GPL; 

import java.util.LinkedList; 

// *************************************************************************
   
public  class  WorkSpace {
	 // supply template actions
    //__feature_mapping__ [BFS] [9:9]
	public void init_vertex( Vertex v ) {}

	
    //__feature_mapping__ [BFS] [10:10]
	public void preVisitAction( Vertex v ) {}

	
    //__feature_mapping__ [BFS] [11:11]
	public void postVisitAction( Vertex v ) {}

	
    //__feature_mapping__ [BFS] [12:12]
	public void nextRegionAction( Vertex v ) {}

	
    //__feature_mapping__ [BFS] [13:14]
	public void checkNeighborAction( Vertex vsource, 
     Vertex vtarget ) {}


}
