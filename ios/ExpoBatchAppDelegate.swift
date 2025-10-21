import ExpoModulesCore

public class ExpoBatchAppDelegate: ExpoAppDelegateSubscriber {

  public func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        RNBatch.start()
        print("hello from expo delegate")
        return true
  }
}
