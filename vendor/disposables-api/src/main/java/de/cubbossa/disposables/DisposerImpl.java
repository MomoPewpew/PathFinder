package de.cubbossa.disposables;

import java.util.*;

class DisposerImpl implements Disposer {

  private final Map<Disposable, Node> nodeTree;

  protected DisposerImpl() {
    this.nodeTree = Collections.synchronizedMap(new WeakHashMap<>());
  }

  private Node node(Disposable disposable) {
    Node present = nodeTree.get(disposable);
    if (present != null) {
      return present;
    }
    Node newNode = new Node(disposable);
    nodeTree.put(disposable, newNode);
    return newNode;
  }

  @Override
  public void register(Disposable parent, Disposable disposable) {
    Objects.requireNonNull(parent, "parent");
    Objects.requireNonNull(disposable, "disposable");

    Node parentNode = node(parent);
    Node childNode = node(disposable);

    parentNode.children.put(disposable, childNode);
  }

  @Override
  public void unregister(Disposable disposable) {
    if (disposable == null) {
      return;
    }
    nodeTree.remove(disposable);
    nodeTree.values().forEach(e -> e.children.remove(disposable));
  }

  @Override
  public void dispose(Disposable disposable) {
    if (disposable == null) {
      return;
    }
    if (!nodeTree.containsKey(disposable)) {
      disposable.dispose();
      return;
    }
    Node node = node(disposable);
    disposeRecursively(node);
  }

  private void disposeRecursively(Node node) {
    nodeTree.remove(node.disposable);
    if (!node.children.isEmpty()) {
      for (Node child : node.children.values()) {
        if (!nodeTree.containsKey(child.disposable)) {
          continue;
        }
        disposeRecursively(child);
      }
    }
    node.disposable.dispose();
  }

  @Override
  public void dispose() {
    while (!nodeTree.isEmpty()) {
      disposeRecursively(nodeTree.values().iterator().next());
    }
  }

  private static class Node {
    final Disposable disposable;
    final Map<Disposable, Node> children;

    public Node(Disposable disposable) {
      this.disposable = disposable;
      this.children = Collections.synchronizedMap(new WeakHashMap<>());
    }
  }
}
