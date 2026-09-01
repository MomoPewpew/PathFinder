package de.cubbossa.disposables;

public interface Disposer extends Disposable {

  static Disposer disposer() {
    return new DisposerImpl();
  }

  void register(Disposable parent, Disposable disposable);

  void unregister(Disposable disposable);

  void dispose(Disposable disposable);
}
