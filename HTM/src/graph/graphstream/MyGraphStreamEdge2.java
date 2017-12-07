package graph.graphstream;

import org.graphstream.graph.Node;


public class MyGraphStreamEdge2 extends MyGraphStreamEdge {

    public MyGraphStreamEdge2(String id, Node source, Node target, boolean directed) {
        super(id, source, target, directed);
    }

    @Override
    public void setState(State s) {
        switch (s) {
            case ACTIVATED :
                addAttribute("ui.style", "visibility-mode: normal; fill-color: red;");
                break;
            case DESACTIVATED :
                addAttribute("ui.style", "visibility-mode: normal; fill-color: black;");
                break;
        }
    }
}
