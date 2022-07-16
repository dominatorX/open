public interface Tree {

    Object getElem();

    Object setElem(int[] obj);

    RouteNode getParent();

    RouteNode getFirstChild();

    RouteNode getNextSibling();

}