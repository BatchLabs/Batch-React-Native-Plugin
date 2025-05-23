import { ConfigPlugin, withAppDelegate } from '@expo/config-plugins';

// MARK : - Objectif-c

const DID_FINISH_LAUNCHING_WITH_OPTIONS_OBJC_DECLARATION =
  '- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions\n{';
const IMPORT_OBJC_BATCH = '\n\n#import <RNBatchPush/RNBatch.h>\n';
const REGISTER_OBJC_BATCH = '\n  [RNBatch start];\n';

export const modifyObjCDelegate = (contents: string): string => {
  return modifyDelegate(contents, IMPORT_OBJC_BATCH, DID_FINISH_LAUNCHING_WITH_OPTIONS_OBJC_DECLARATION, REGISTER_OBJC_BATCH);
};

// MARK : - Swift

const DID_FINISH_LAUNCHING_WITH_OPTIONS_SWIFT_DECLARATION = `@UIApplicationMain
public class AppDelegate: ExpoAppDelegate {
  public override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
  ) -> Bool {`;
const IMPORT_SWIFT_BATCH = '\n\nimport RNBatchPush\n';
const REGISTER_SWIFT_BATCH = '\n    RNBatch.start()\n';

export const modifySwiftDelegate = (contents: string): string => {
  return modifyDelegate(contents, IMPORT_SWIFT_BATCH, DID_FINISH_LAUNCHING_WITH_OPTIONS_SWIFT_DECLARATION, REGISTER_SWIFT_BATCH);
};

// MARK : - Common

export const modifyDelegate = (contents: string, importBatch: string, declaration: string, register: string): string => {
  contents = contents.replace('\n', importBatch);

  const [beforeDeclaration, afterDeclaration] = contents.split(declaration);

  const newAfterDeclaration = declaration.concat(register).concat(afterDeclaration);

  contents = beforeDeclaration.concat(newAfterDeclaration);
  return contents;
};

export const withReactNativeBatchAppDelegate: ConfigPlugin<object | void> = config => {
  return withAppDelegate(config, config => {
    config.modResults.contents = modifyAppDelegate(config.modResults.contents);
    return config;
  });
};

export const modifyAppDelegate = (content: string): string => {
  return isObjCDelegate(content) ? modifyObjCDelegate(content) : modifySwiftDelegate(content);
};

const isObjCDelegate = (content: string): boolean => {
  return content.includes('@implementation AppDelegate');
};
