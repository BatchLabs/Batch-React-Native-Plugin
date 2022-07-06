import { ConfigPlugin, withAppBuildGradle } from '@expo/config-plugins';
import { Props } from './withReactNativeBatch';

export const pushDependencies = (contents: string, props: Props): string => {
  let newContents = contents;

  const versionNameLine = newContents.match(
    /versionName "([^"]*)"/
  );

  if (versionNameLine) {
    const endOfVersionNameIndex = newContents.indexOf(
      versionNameLine[0]
    );

    const start = newContents.substring(0, endOfVersionNameIndex);
    const end = newContents.substring(
      endOfVersionNameIndex + versionNameLine[0].length
    );
    newContents =
      start 
      + versionNameLine[0]
      + `\n        resValue "string", "BATCH_API_KEY", "${props.androidApiKey}"`
      + end;
  }

  const dependenciesString = 'dependencies {';
  const dependenciesIndex = newContents.indexOf('dependencies {');

  if (dependenciesIndex > -1) {
    const index = dependenciesIndex + dependenciesString.length;
    const start = newContents.substring(0, index);
    const end = newContents.substring(index);

    newContents =
      start +
      `\n    implementation platform('com.google.firebase:firebase-bom:25.12.0')\n    implementation "com.google.firebase:firebase-messaging"\n    ${'implementation "com.batch.android:batch-sdk:${rootProject.ext.batchSdkVersion}"'}` +
      end;
  }
  return newContents;
};

export const withReactNativeBatchAppBuildGradle: ConfigPlugin<Props> = (
  config,
  props
) => {
  return withAppBuildGradle(config, async conf => {
    const content = conf.modResults.contents;
    const newContents = pushDependencies(content, props);

    return {
      ...conf,
      modResults: {
        ...conf.modResults,
        contents: newContents,
      },
    };
  });
};
