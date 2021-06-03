package GPL; 

import java.util.LinkedList; 
import java.util.Iterator; 

// *************************************************************************

public   class  Vertex {
	
    public LinkedList adjacentNeighbors;

	
    public String name;

	

    //__feature_mapping__ [UndirectedWithNeighbors] [13:16]
	public Vertex() 
    {
        VertexConstructor();
    }

	
      
     //__feature_mapping__ [UndirectedWithNeighbors] [18:22]
	private void  VertexConstructor__wrappee__UndirectedWithNeighbors() 
    {
        name      = null;
        adjacentNeighbors = new LinkedList();
    }

	

    //__feature_mapping__ [BFS] [11:15]
	public void VertexConstructor( ) 
    {
        VertexConstructor__wrappee__UndirectedWithNeighbors();
        visited = false;
    }

	

    //__feature_mapping__ [UndirectedWithNeighbors] [24:28]
	public  Vertex assignName( String name ) 
    {
        this.name = name;
        return ( Vertex ) this;
    }

	

    //__feature_mapping__ [UndirectedWithNeighbors] [30:33]
	public String getName( )
    {
        return this.name;
    }

	
    
    //__feature_mapping__ [UndirectedWithNeighbors] [35:38]
	public LinkedList getNeighborsObj( )
    {
 	  return adjacentNeighbors;
    }

	

    //__feature_mapping__ [UndirectedWithNeighbors] [40:54]
	public VertexIter getNeighbors( )
    {
        return new VertexIter( )
        {
            private Iterator iter = adjacentNeighbors.iterator( );
            public Vertex next( ) 
            { 
                return ( ( Neighbor )iter.next( ) ).neighbor; 
            }
            public boolean hasNext( ) 
            { 
                return iter.hasNext( ); 
            }
        };
    }

	

     //__feature_mapping__ [UndirectedWithNeighbors] [56:66]
	private void  display__wrappee__UndirectedWithNeighbors( ) 
    {
        System.out.print( "Node " + name + " connected to: " );

        for ( VertexIter vxiter = getNeighbors( ); vxiter.hasNext( ); )
        {
            System.out.print( vxiter.next( ).getName( ) + ", " );
        }

        System.out.println();
    }

	

     //__feature_mapping__ [Connected] [9:13]
	private void  display__wrappee__Connected( ) 
    {
        System.out.print( " comp# "+ componentNumber + " " );
        display__wrappee__UndirectedWithNeighbors( );
    }

	 // of bfsNodeSearch

    //__feature_mapping__ [BFS] [69:76]
	public void display( ) 
    {
        if ( visited )
            System.out.print( "  visited " );
        else
            System.out.println( " !visited " );
        display__wrappee__Connected( );
    }

	
//--------------------
// differences
//--------------------

    //__feature_mapping__ [UndirectedWithNeighbors] [71:74]
	public void addEdge( Neighbor n ) 
    {
        adjacentNeighbors.add( n );
    }

	

    //__feature_mapping__ [UndirectedWithNeighbors] [76:78]
	public void adjustAdorns( Neighbor sourceNeighbor )
    {
    }

	

    //__feature_mapping__ [UndirectedWithNeighbors] [80:96]
	public EdgeIter getEdges( )
    {
        return new EdgeIter( )
        {
            private Iterator iter = adjacentNeighbors.iterator( );
            public EdgeIfc next( ) 
            { 
                return ( Neighbor ) iter.next( ); 

//              return ( ( EdgeIfc ) ( ( Neighbor )iter.next( ) ).edge );
            }
            public boolean hasNext( ) 
            { 
              return iter.hasNext( ); 
            }
        };
    }

	
    public int componentNumber;

	
    public boolean visited;

	

    //__feature_mapping__ [BFS] [17:21]
	public void init_vertex( WorkSpace w ) 
    {
        visited = false;
        w.init_vertex( ( Vertex ) this );
    }

	

    //__feature_mapping__ [BFS] [23:67]
	public void nodeSearch( WorkSpace w ) 
    {
        int     s, c;
        Vertex  v;
        Vertex  header;

        // Step 1: if preVisitAction is true or if we've already
        //         visited this node
        w.preVisitAction( ( Vertex ) this );

        if ( visited )
        {
            return;
        }

        // Step 2: Mark as visited, put the unvisited neighbors in the queue
        //     and make the recursive call on the first element of the queue
        //     if there is such if not you are done
        visited = true;

        // Step 3: do postVisitAction now, you are no longer going through the
        // node again, mark it as black
        w.postVisitAction( ( Vertex ) this );

        // enqueues the vertices not visited
        for ( VertexIter vxiter = getNeighbors( ); vxiter.hasNext( ); )
        {
            v = vxiter.next( );

            // if your neighbor has not been visited then enqueue
            if ( !v.visited ) 
            {
                GlobalVarsWrapper.Queue.add( v );
            }

        } // end of for

        // while there is something in the queue
        while( GlobalVarsWrapper.Queue.size( )!= 0 )
        {
            header = ( Vertex ) GlobalVarsWrapper.Queue.get( 0 );
            GlobalVarsWrapper.Queue.remove( 0 );
            header.nodeSearch( w );
        }
    }


}
