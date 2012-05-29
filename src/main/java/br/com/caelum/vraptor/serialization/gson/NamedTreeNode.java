package br.com.caelum.vraptor.serialization.gson;


import java.util.LinkedList;
import java.util.List;

/**
 * Implements a simple tree node hierarchical class.
 * 
 * @author francofabio
 * @version 1.0
 */
public class NamedTreeNode {

    private String name;
    private List<NamedTreeNode> childs;
    private NamedTreeNode parent;

    public NamedTreeNode() {
        this.childs = new LinkedList<NamedTreeNode>();
    }

    public NamedTreeNode(String name, NamedTreeNode parent) {
        this();
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NamedTreeNode> getChilds() {
        return childs;
    }

    public void setChilds(List<NamedTreeNode> childs) {
        this.childs = childs;
    }

    public NamedTreeNode getParent() {
        return parent;
    }

    public void setParent(NamedTreeNode parent) {
        this.parent = parent;
    }

    public void addChild(String name) {
        String[] path = name.split("\\.");
        NamedTreeNode parent = this;
        for (String p : path) {
            int index = parent.childs.indexOf(new NamedTreeNode(p, null));
            if (index == -1) {
                NamedTreeNode n = new NamedTreeNode(p, parent);
                parent.childs.add(n);
                parent = n;
            } else {
                parent = parent.childs.get(index);
            }
        }
    }

    private String joinPath(String[] path, int begin, int end) {
        StringBuilder b = new StringBuilder();

        for (int i = begin; i < end; i++) {
            b.append(path[i]).append(".");
        }

        b.delete(b.length() - 1, b.length());

        return b.toString();
    }

    public NamedTreeNode getChild(String name) {
        String[] path = name.split("\\.");
        int index = childs.indexOf(new NamedTreeNode(path[0], null));
        if (index == -1) {
            return null;
        } else {
            NamedTreeNode child = childs.get(index);
            if (path.length > 1) {
                return child.getChild(joinPath(path, 1, path.length));
            } else {
                return child;
            }
        }
    }

    public void removeChild(String name) {
        String[] path = name.split("\\.");
        int index = childs.indexOf(new NamedTreeNode(path[0], null));
        if (index > -1) {
            if (path.length > 1) {
                NamedTreeNode child = childs.get(index);
                child.removeChild(joinPath(path, 1, path.length));
            } else {
                childs.remove(index);
            }
        }
    }

    
    public boolean containsChild(String name) {
        return (childs.indexOf(new NamedTreeNode(name, null)) > -1);
    }

    public boolean containsChilds() {
        return !childs.isEmpty();
    }

    public String getPath() {
        StringBuilder b = new StringBuilder();
        NamedTreeNode node = this;
        b.append(node.getName());
        while (node.getParent() != null) {
            node = node.getParent();
            b.insert(0, node.getName() + ".");
        }

        return b.toString();
    }
    
    public String getPathWithoutRoot() {
        StringBuilder b = new StringBuilder();
        NamedTreeNode node = this;
        while (node.getParent() != null) {
            b.insert(0, node.getName() + ".");
            node = node.getParent();
        }
        if (b.length() > 0) {
            b.delete(b.length()-1, b.length());
        }

        return b.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NamedTreeNode other = (NamedTreeNode) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + getName() + "]";
    }

}
