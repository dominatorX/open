public class RouteNode implements Tree {

    public int[] element;

    public RouteNode parent, firstChild, nextSibling;

    public RouteNode() {
        this(null, null, null, null);
    }

    /**
     *
     *
     * @param object      root
     * @param parent      parent
     * @param firstChild  first child
     * @param nextSibling sibling
     */
    public RouteNode(int[] object, RouteNode parent, RouteNode firstChild, RouteNode nextSibling) {
        this.element = object;
        this.parent = parent;
        this.firstChild = firstChild;
        this.nextSibling = nextSibling;
    }

    public void removeHead(){
        this.element = this.firstChild.element;
        this.firstChild = this.firstChild.firstChild;
    }

    @Override
    public int[] getElem() {
        return element;
    }

    @Override
    public int[] setElem(int[] obj) {
        int[] bak = element;
        element = obj;
        return bak;
    }

    @Override
    public RouteNode getParent() {
        return parent;
    }

    @Override
    public RouteNode getFirstChild() {
        return firstChild;
    }

    @Override
    public RouteNode getNextSibling() {
        return nextSibling;
    }
}