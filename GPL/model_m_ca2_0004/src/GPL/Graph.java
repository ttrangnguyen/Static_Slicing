package GPL; 

import java.util.Iterator; 

import java.util.LinkedList; 

//dja: add for performance reasons
import java.util.HashMap; 
import java.util.Map; 

import java.lang.Integer; 
import java.util.Collections; 
import java.util.Comparator; 
import java.util.Set; 
import java.util.HashSet; 

// ************************************************************
 
public   class  Graph {
	
    public LinkedList vertices;

	
    public static final boolean isDirected = false;

	
    //dja: add for performance reasons
    private Map verticesMap;

	


    //__feature_mapping__ [UndirectedOnlyVertices] [20:26]
	public Graph( )
    {
        vertices = new LinkedList();
	  //dja: add for performance reasons
        verticesMap = new HashMap( );

    }

	

    // Fall back method that stops the execution of programs
     //__feature_mapping__ [UndirectedOnlyVertices] [29:31]
	private void  run__wrappee__UndirectedOnlyVertices( Vertex s )
    {
    }

	
    // Executes Number Vertices
     //__feature_mapping__ [Number] [8:13]
	private void  run__wrappee__Number( Vertex s )
     {
       	System.out.println("Number");
        NumberVertices( );
        run__wrappee__UndirectedOnlyVertices( s );
    }

	
    // Executes Connected Components
     //__feature_mapping__ [Connected] [8:13]
	private void  run__wrappee__Connected( Vertex s )
    {
	     	System.out.println("Connected");
        ConnectedComponents( );
        run__wrappee__Number( s );
    }

	

    // Executes MSTPrim
    //__feature_mapping__ [MSTPrim] [15:23]
	public void run( Vertex s )
     {
          	System.out.println("MSTPrim");
        Graph gaux = Prim( s );
//        Graph.stopProfile();
        gaux.display();
//        Graph.resumeProfile();
        run__wrappee__Connected( s );
    }

	
    // Adds an edge with weights
    //__feature_mapping__ [WeightedOnlyVertices] [9:12]
	public void addAnEdge( Vertex start,  Vertex end, int weight )
   {
        addEdge( start,end, weight );
    }

	

    // Adds and edge by setting start as adjacent to end and
    // viceversa
    //__feature_mapping__ [UndirectedOnlyVertices] [41:46]
	public EdgeIfc addEdge( Vertex start,  Vertex end )
    {
        start.addAdjacent( end );
        end.addAdjacent( start );
        return ( EdgeIfc ) start;
    }

	

     // Adds an edge without weights if Weighted layer is not present
 //   public void addEdge( Vertex start,   NeighborIfc theNeighbor )
   // {
     //   addEdge( Vertex start,  ( Vertex ) theNeighbor )
   // }



    //__feature_mapping__ [UndirectedOnlyVertices] [56:62]
	public void addVertex( Vertex v )
    {
        vertices.add( v );

	  //dja: add for performance reasons
	  verticesMap.put( v.name, v );
    }

	

    // Finds a vertex given its name in the vertices list
    //__feature_mapping__ [UndirectedOnlyVertices] [65:85]
	public  Vertex findsVertex( String theName )
      {
        int i=0;
        Vertex theVertex;

        // if we are dealing with the root
        if ( theName == null )
            return null;

	  //dja: removed for performance reasons
//        for( i=0; i<vertices.size(); i++ )
//        {
//            theVertex = ( Vertex )vertices.get( i );
//            if ( theName.equals( theVertex.name ) )
//                return theVertex;
//        }
//        return null;

	  //dja: add for performance reasons
	  return ( Vertex ) verticesMap.get( theName );
    }

	

    //__feature_mapping__ [UndirectedOnlyVertices] [87:101]
	public VertexIter getVertices( )
    {
        return new VertexIter( )
        {
                private Iterator iter = vertices.iterator( );
                public Vertex next( )
                {
                    return ( Vertex )iter.next( );
                }
                public boolean hasNext( )
                {
                    return iter.hasNext( );
                }
        };
    }

	

     //__feature_mapping__ [UndirectedOnlyVertices] [103:113]
	private void  display__wrappee__UndirectedOnlyVertices() {
        int s = vertices.size();
        int i;

        System.out.println( "******************************************" );
        System.out.println( "Vertices " );
        for ( i=0; i<s; i++ )
            ( ( Vertex ) vertices.get( i ) ).display();
        System.out.println( "******************************************" );

    }

	
    
    //__feature_mapping__ [WeightedOnlyVertices] [25:28]
	public void display() 
   {
        display__wrappee__UndirectedOnlyVertices();
    }

	
   //__feature_mapping__ [UndirectedOnlyVertices] [114:144]
	public  EdgeIfc findsEdge( Vertex theSource,
                    Vertex theTarget )
       {
        //dja: performance improvement
//        for( VertexIter vertexiter = getVertices(); vertexiter.hasNext(); )
//         {
//        Vertex v1 = vertexiter.next( );
//        for( EdgeIter edgeiter = v1.getEdges(); edgeiter.hasNext(); )
//            {
//                EdgeIfc theEdge = edgeiter.next();
//            Vertex v2 = theEdge.getOtherVertex( v1 );
//              if ( ( v1.getName().equals( theSource.getName() ) &&
//                       v2.getName().equals( theTarget.getName() ) ) ||
//                         ( v1.getName().equals( theTarget.getName() ) &&
//                     v2.getName().equals( theSource.getName() ) ) )
//                    return theEdge;
//            }
//        }
        Vertex v1 = theSource;
        for( EdgeIter edgeiter = v1.getEdges(); edgeiter.hasNext(); )
            {
                EdgeIfc theEdge = edgeiter.next();
            Vertex v2 = theEdge.getOtherVertex( v1 );
              if ( ( v1.getName().equals( theSource.getName() ) &&
                       v2.getName().equals( theTarget.getName() ) ) ||
                         ( v1.getName().equals( theTarget.getName() ) &&
                     v2.getName().equals( theSource.getName() ) ) )
                    return theEdge;
            }
        return null;
    }

	

    //__feature_mapping__ [Number] [15:18]
	public void NumberVertices( ) 
    {
        GraphSearch( new NumberWorkSpace( ) );
    }

	

    //__feature_mapping__ [Connected] [15:18]
	public void ConnectedComponents( ) 
    {
        GraphSearch( new RegionWorkSpace( ) );
    }

	

    //__feature_mapping__ [MSTPrim] [25:241]
	public  Graph Prim( Vertex r ) {
        Vertex root;

        root = r;
        Vertex x;

        // 2. and 3. Initializes the vertices
        for ( VertexIter vxiter = getVertices(); vxiter.hasNext(); )
        {
            x = vxiter.next();
            x.pred = null;
            x.key = Integer.MAX_VALUE;
        }

        // 4. and 5.
        root.key = 0;
        root.pred = null;

        // 2. S <- empty set

        // 1. Queue <- V[G], copy the vertex in the graph in the priority queue
        LinkedList Queue = new LinkedList();
	  //dja: added for performance reasons
        Set indx = new HashSet( );

        // Inserts the root at the head of the queue
        //dja: Moved from below the for loop for performance reasons
        Queue.add( root );
     
        //dja: added for performance reasons
        indx.add( root.getName( ) );
        for ( VertexIter vxiter = getVertices(); vxiter.hasNext(); )
        {
            x = vxiter.next();
            if ( x.key != 0 ) // this means, if this is not the root
            {
                Queue.add( x );
	          //dja: added for performance reasons
                indx.add( x.getName( ) );
            }
        }

        // Inserts the root at the head of the queue
        //dja: moved above for loop for perf. reasons
        // Queue.addFirst( root );

        // 6. while Q!=0
        Vertex ucurrent;
        int j,k,l;
        int pos;
        LinkedList Uneighbors;
        Vertex u,v;
        EdgeIfc en;
        NeighborIfc vn;

        int wuv;
        boolean isNeighborInQueue = false;

        // Queue is a list ordered by key values.
        // At the beginning all key values are INFINITUM except
        // for the root whose value is 0.
        while ( Queue.size()!=0 )
        {
            // 7. u <- Extract-Min(Q);
            // Since this is an ordered queue the first element is the min
            u = ( Vertex )Queue.removeFirst();

		//dja: added code below for per. reasons
            indx.remove( u.getName( ) );
            

            // 8. for each vertex v adjacent to u
            Uneighbors = u.getNeighborsObj( );

            // dja - fix compile erros
            // for( EdgeIter edgeiter = u.getEdges(); edgeiter.hasNext(); )
            k = 0;
            for( EdgeIter edgeiter = u.getEdges(); edgeiter.hasNext(); k++ )
            {
                vn = ( NeighborIfc )Uneighbors.get( k );
                // dja - fix compile bug
                // en = edgeiter.next();
//                en = ( Edge ) edgeiter.next();
                en = edgeiter.next();

                v = en.getOtherVertex(u);

                // Check to see if the neighbor is in the queue
                isNeighborInQueue = false;

                // if the Neighor is in the queue
		    //dja: removed 2 lines for performance reasons and added 3rd line.  left 4th line alone
                //int indexNeighbor = Queue.indexOf( v );
                //if ( indexNeighbor>=0 )
                if ( indx.contains( v.getName( ) ) )
                    isNeighborInQueue = true;
                wuv = en.getWeight();

                // 9. Relax (u,v w)
                if ( isNeighborInQueue && ( wuv < v.key ) )
                    {
                    v.key = wuv;
                    v.pred = u.getName();
                    Uneighbors.set( k,vn ); // adjust values in the neighbors

                    // update the values of v in the queue
                    // Remove v from the Queue so that we can reinsert it
                    // in a new place according to its new value to keep
                    // the Linked List ordered
		        //dja: added for perf. reasons
			  Object residue = ( Object ) v;
                    Queue.remove( residue );
	              // Object residue = Queue.remove( indexNeighbor );

			  //dja: added for performance reasons
                    indx.remove( v.getName( ) );

                    // Get the new position for v
                    int position = Collections.binarySearch( Queue,v,
                                              new Comparator() {
                        public int compare( Object o1, Object o2 )
                                                 {
                            Vertex v1 = ( Vertex )o1;
                            Vertex v2 = ( Vertex )o2;

                            if ( v1.key < v2.key )
                                return -1;
                            if ( v1.key == v2.key )
                                return 0;
                            return 1;
                        }
                    } );

                    // Adds v in its new position in Queue
                    if ( position < 0 )  // means it is not there
                                         {
                        Queue.add( - ( position+1 ),v );
                    }
                    else      // means it is there
                                         {
                        Queue.add( position,v );
                    }
                
 			  //dja: added for perf. reasons
                    indx.add( v.getName( ) );

                } // if 8-9.
            } // for all neighbors
        } // of while

        // Creates the new Graph that contains the SSSP
        String theName;
        Graph newGraph = new  Graph();

        // Creates and adds the vertices with the same name
        // dja - fixed compile bug
        // for ( i=0; i<numvertices; i++ )
        for ( VertexIter vxiter = getVertices( ); vxiter.hasNext( ); )
        {
            // dja - fixing compile bugs
            // theName = ( ( Vertex )vertices.get( i ) ).name;
            Vertex vtx = vxiter.next( );
            theName = vtx.name;

            newGraph.addVertex( new  Vertex().assignName( theName ) );
        }

        // Creates the edges from the NewGraph
        Vertex theVertex, thePred;
        Vertex theNewVertex, theNewPred;
        EdgeIfc   e;

        // Creates and adds the vertices with the same name
        // dja - fixed compile bug
        // for ( i=0; i<numvertices; i++ )
        for ( VertexIter vxiter = getVertices( ); vxiter.hasNext( ); )
        {
            // theVertex and its Predecessor
            // dja - fixing compile bugs
            // theVertex = ( Vertex )vertices.get( i );
            theVertex = vxiter.next( );

            thePred = findsVertex( theVertex.pred );

            // if theVertex is the source then continue we dont need
            // to create a new edge at all
            if ( thePred==null )
                continue;

            // Find the references in the new Graph
            theNewVertex = newGraph.findsVertex( theVertex.name );
            theNewPred = newGraph.findsVertex( thePred.name );

            // Creates the new edge from predecessor -> vertex in the newGraph
            // and ajusts the adorns based on the old edge
//            EdgeIfc theNewEdge = new  Edge();

            // dja - fix compile errors
            // theNewEdge.EdgeConstructor( theNewPred, theNewVertex );
            EdgeIfc theNewEdge = newGraph.addEdge( theNewPred, theNewVertex );

            // dja - fix compile errors
            // e = findsEdge( thePred,theVertex );
            e = findsEdge( thePred,theVertex );

            // dja - fix comple errors
            // theNewEdge.adjustAdorns( e );
            theNewEdge.adjustAdorns( e );


            // Adds the new edge to the newGraph
            // dja - fix compile errors
            // newGraph.addEdge( theNewEdge );
        }
        return newGraph;

    }

	

    //__feature_mapping__ [BFS] [13:30]
	public  void GraphSearch( WorkSpace w )
    {
        VertexIter vxiter = getVertices();
        if (vxiter.hasNext() == false) {
            return;
        }
        while (vxiter.hasNext()) {
            Vertex v = vxiter.next();
            v.init_vertex( w );
        }
        for (vxiter = getVertices(); vxiter.hasNext();) {
            Vertex v = vxiter.next();
            if (v.visited) {
                w.nextRegionAction( v );
                v.nodeSearch( w );
            }
        }
    }

	
 
    //__feature_mapping__ [WeightedOnlyVertices] [14:23]
	public void addEdge( Vertex start,  Vertex end, int weight )
   {
        addEdge( start,end ); // adds the start and end as adjacent
        start.addWeight( weight ); // the direction layer takes care of that
                
        // if the graph is undirected you have to include 
        // the weight of the edge coming back
        if ( isDirected==false )
            end.addWeight( weight );
    }


}
