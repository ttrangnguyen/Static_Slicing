package GPL; 

// *****************************************************************
   
public  class  RegionWorkSpace  extends  WorkSpace {
	
    int counter;

	

    //__feature_mapping__ [Connected] [9:12]
	public RegionWorkSpace( ) 
    {
        counter = 0;
    }

	

    //__feature_mapping__ [Connected] [14:17]
	public void init_vertex( Vertex v ) 
    {
        v.componentNumber = -1;
    }

	
      
    //__feature_mapping__ [Connected] [19:22]
	public void postVisitAction( Vertex v ) 
    {
        v.componentNumber = counter;
    }

	

    //__feature_mapping__ [Connected] [24:27]
	public void nextRegionAction( Vertex v ) 
    {
        counter ++;
    }


}
