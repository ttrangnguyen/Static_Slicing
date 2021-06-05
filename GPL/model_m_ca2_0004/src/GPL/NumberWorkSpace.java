package GPL; 

// **********************************************************************
   
public  class  NumberWorkSpace  extends  WorkSpace {
	
    int vertexCounter;

	

    //__feature_mapping__ [Number] [9:12]
	public NumberWorkSpace( ) 
    {
        vertexCounter = 0;
    }

	

    //__feature_mapping__ [Number] [14:21]
	public void preVisitAction( Vertex v )
    {
        // This assigns the values on the way in
        if ( v.visited != true )
        {
            v.VertexNumber = vertexCounter++;
        }
    }


}
