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

const DID_FINISH_LAUNCHING_WITH_OPTIONS_SWIFT_DECLARATION = 'return super.application(application, didFinishLaunchingWithOptions: launchOptions)';
const IMPORT_SWIFT_BATCH = '\n\nimport RNBatchPush\n';
const REGISTER_SWIFT_BATCH = '\n    RNBatch.start()\n';

export const modifySwiftDelegate = (contents: string): string => {
  return modifyDelegate(contents, IMPORT_SWIFT_BATCH, DID_FINISH_LAUNCHING_WITH_OPTIONS_SWIFT_DECLARATION, REGISTER_SWIFT_BATCH);
};

// MARK : - Common

export const modifyDelegate = (contents: string, importBatch: string, declaration: string, register: string): string => {
  contents = contents.replace('\n', importBatch);

  if (contents.includes(declaration) && !contents.includes(register)) {
    contents = contents.replace(declaration, `${register}    ${declaration}`);
  }

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
